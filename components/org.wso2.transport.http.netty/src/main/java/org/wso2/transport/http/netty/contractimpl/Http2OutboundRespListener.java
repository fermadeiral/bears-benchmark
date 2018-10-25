/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.transport.http.netty.contractimpl;

import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.LastHttpContent;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2ConnectionEncoder;
import io.netty.handler.codec.http2.Http2Error;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.HttpConversionUtil;
import io.netty.util.internal.logging.InternalLogLevel;
import io.netty.util.internal.logging.InternalLogger;
import io.netty.util.internal.logging.InternalLoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.transport.http.netty.contract.Constants;
import org.wso2.transport.http.netty.contract.HttpConnectorListener;
import org.wso2.transport.http.netty.contract.HttpResponseFuture;
import org.wso2.transport.http.netty.contract.ServerConnectorException;
import org.wso2.transport.http.netty.contractimpl.common.Util;
import org.wso2.transport.http.netty.contractimpl.listener.HttpServerChannelInitializer;
import org.wso2.transport.http.netty.message.Http2PushPromise;
import org.wso2.transport.http.netty.message.HttpCarbonMessage;

import java.util.Calendar;
import java.util.Locale;

import static org.wso2.transport.http.netty.contract.Constants.PROMISED_STREAM_REJECTED_ERROR;

/**
 * {@code Http2OutboundRespListener} is responsible for listening for outbound response messages
 * and delivering them to the client.
 */
public class Http2OutboundRespListener implements HttpConnectorListener {

    private static final Logger LOG = LoggerFactory.getLogger(Http2OutboundRespListener.class);
    private static final InternalLogger accessLogger = InternalLoggerFactory.getInstance(Constants.ACCESS_LOG);

    private HttpCarbonMessage inboundRequestMsg;
    private ChannelHandlerContext ctx;
    private Http2ConnectionEncoder encoder;
    private int originalStreamId;   // stream id of the request received from the client
    private Http2Connection conn;
    private String serverName;
    private HttpResponseFuture outboundRespStatusFuture;
    private HttpServerChannelInitializer serverChannelInitializer;
    private Calendar inboundRequestArrivalTime;
    private String remoteAddress = "-";

    public Http2OutboundRespListener(HttpServerChannelInitializer serverChannelInitializer,
                                     HttpCarbonMessage inboundRequestMsg, ChannelHandlerContext ctx,
                                     Http2Connection conn, Http2ConnectionEncoder encoder, int streamId,
                                     String serverName, String remoteAddress) {
        this.serverChannelInitializer = serverChannelInitializer;
        this.inboundRequestMsg = inboundRequestMsg;
        this.ctx = ctx;
        this.conn = conn;
        this.encoder = encoder;
        this.originalStreamId = streamId;
        this.serverName = serverName;
        if (remoteAddress != null) {
            this.remoteAddress = remoteAddress;
        }
        this.outboundRespStatusFuture = inboundRequestMsg.getHttpOutboundRespStatusFuture();
        inboundRequestArrivalTime = Calendar.getInstance();
    }

    @Override
    public void onMessage(HttpCarbonMessage outboundResponseMsg) {
        writeMessage(outboundResponseMsg, originalStreamId);
    }

    @Override
    public void onError(Throwable throwable) {
        LOG.error("Couldn't send the outbound response", throwable);
    }

    @Override
    public void onPushPromise(Http2PushPromise pushPromise) {
        ctx.channel().eventLoop().execute(() -> {
            try {
                int promisedStreamId = getNextStreamId();
                // Update streamIds
                pushPromise.setPromisedStreamId(promisedStreamId);
                pushPromise.setStreamId(originalStreamId);
                // Construct http request
                HttpRequest httpRequest = pushPromise.getHttpRequest();
                httpRequest.headers().add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
                // A push promise is a server initiated request, hence it should contain request headers
                Http2Headers http2Headers =
                        HttpConversionUtil.toHttp2Headers(httpRequest, true);
                // Write the push promise to the wire
                ChannelFuture channelFuture = encoder.writePushPromise(
                        ctx, originalStreamId, promisedStreamId, http2Headers, 0, ctx.newPromise());
                encoder.flowController().writePendingBytes();
                ctx.flush();
                Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, channelFuture);
            } catch (Exception ex) {
                String errorMsg = "Failed to send push promise : " + ex.getMessage().toLowerCase(Locale.ENGLISH);
                LOG.error(errorMsg, ex);
                inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
            }
        });
    }

    @Override
    public void onPushResponse(int promiseId, HttpCarbonMessage outboundResponseMsg) {
        if (isValidStreamId(promiseId)) {
            writeMessage(outboundResponseMsg, promiseId);
        } else {
            inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(
                    new ServerConnectorException(PROMISED_STREAM_REJECTED_ERROR));
        }
    }

    private void writeMessage(HttpCarbonMessage outboundResponseMsg, int streamId) {
        ResponseWriter writer = new ResponseWriter(streamId);
        ctx.channel().eventLoop().execute(() -> outboundResponseMsg.getHttpContentAsync().setMessageListener(
                httpContent -> ctx.channel().eventLoop().execute(() -> {
                    try {
                        writer.writeOutboundResponse(outboundResponseMsg, httpContent);
                    } catch (Http2Exception ex) {
                        String errorMsg = "Failed to send the outbound response : " +
                                          ex.getMessage().toLowerCase(Locale.ENGLISH);
                        LOG.error(errorMsg, ex);
                        inboundRequestMsg.getHttpOutboundRespStatusFuture().notifyHttpListener(ex);
                    }
                })));
    }

    private class ResponseWriter {

        private boolean isHeaderWritten = false;
        private int streamId;
        private Long contentLength = 0L;

        public ResponseWriter(int streamId) {
            this.streamId = streamId;
        }

        private void writeOutboundResponse(HttpCarbonMessage outboundResponseMsg, HttpContent httpContent)
                throws Http2Exception {
            if (!isHeaderWritten) {
                writeHeaders(outboundResponseMsg);
            }
            if (Util.isLastHttpContent(httpContent)) {
                final LastHttpContent lastContent = (LastHttpContent) httpContent;
                HttpHeaders trailers = lastContent.trailingHeaders();
                if (serverChannelInitializer.isHttpAccessLogEnabled()) {
                    logAccessInfo(outboundResponseMsg);
                }
                boolean endStream = trailers.isEmpty();
                writeData(lastContent, endStream);
                if (!trailers.isEmpty()) {
                    Http2Headers http2Trailers = HttpConversionUtil.toHttp2Headers(trailers, true);
                    // Write trailing headers.
                    writeHttp2Headers(ctx, streamId, http2Trailers, true);
                }
            } else {
                writeData(httpContent, false);
            }
        }

        private void writeHttp2Headers(ChannelHandlerContext ctx, int streamId, Http2Headers http2Headers, boolean
                endStream) throws Http2Exception {

            ChannelFuture channelFuture = encoder.writeHeaders(
                    ctx, streamId, http2Headers, 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
            Util.addResponseWriteFailureListener(outboundRespStatusFuture, channelFuture);
        }

        private void writeHeaders(HttpCarbonMessage outboundResponseMsg) throws Http2Exception {
            outboundResponseMsg.getHeaders().
                    add(HttpConversionUtil.ExtensionHeaderNames.SCHEME.text(), Constants.HTTP_SCHEME);
            HttpMessage httpMessage =
                    Util.createHttpResponse(outboundResponseMsg, Constants.HTTP2_VERSION, serverName, true);
            // Construct Http2 headers
            Http2Headers http2Headers = HttpConversionUtil.toHttp2Headers(httpMessage, true);
            validatePromisedStreamState();
            isHeaderWritten = true;
            writeHttp2Headers(ctx, streamId, http2Headers, false);
        }

        private void writeData(HttpContent httpContent, boolean endStream) throws Http2Exception {
            contentLength += httpContent.content().readableBytes();
            validatePromisedStreamState();
            ChannelFuture channelFuture = encoder.writeData(
                    ctx, streamId, httpContent.content(), 0, endStream, ctx.newPromise());
            encoder.flowController().writePendingBytes();
            ctx.flush();
            if (endStream) {
                Util.checkForResponseWriteStatus(inboundRequestMsg, outboundRespStatusFuture, channelFuture);
            } else {
                Util.addResponseWriteFailureListener(outboundRespStatusFuture, channelFuture);
            }
        }

        private void logAccessInfo(HttpCarbonMessage outboundResponseMsg) {

            if (!accessLogger.isEnabled(InternalLogLevel.INFO)) {
                return;
            }

            if (originalStreamId != streamId) { // Skip access logs for server push messages
                LOG.debug("Access logging skipped for server push response");
                return;
            }

            HttpHeaders headers = inboundRequestMsg.getHeaders();
            if (headers.contains(Constants.HTTP_X_FORWARDED_FOR)) {
                String forwardedHops = headers.get(Constants.HTTP_X_FORWARDED_FOR);
                // If multiple IPs available, the first ip is the client
                int firstCommaIndex = forwardedHops.indexOf(',');
                remoteAddress = firstCommaIndex != -1 ? forwardedHops.substring(0, firstCommaIndex) : forwardedHops;
            }

            // Populate request parameters
            String userAgent = "-";
            if (headers.contains(HttpHeaderNames.USER_AGENT)) {
                userAgent = headers.get(HttpHeaderNames.USER_AGENT);
            }
            String referrer = "-";
            if (headers.contains(HttpHeaderNames.REFERER)) {
                referrer = headers.get(HttpHeaderNames.REFERER);
            }
            String method = (String) inboundRequestMsg.getProperty(Constants.HTTP_METHOD);
            String uri = (String) inboundRequestMsg.getProperty(Constants.TO);
            HttpMessage request = inboundRequestMsg.getNettyHttpRequest();
            String protocol;
            if (request != null) {
                protocol = request.protocolVersion().toString();
            } else {
                protocol = (String) inboundRequestMsg.getProperty(Constants.HTTP_VERSION);
            }

            // Populate response parameters
            int statusCode = Util.getHttpResponseStatus(outboundResponseMsg).code();

            accessLogger.log(InternalLogLevel.INFO, String.format(
                    Constants.ACCESS_LOG_FORMAT, remoteAddress, inboundRequestArrivalTime, method, uri, protocol,
                    statusCode, contentLength, referrer, userAgent));
        }

        private void validatePromisedStreamState() throws Http2Exception {
            if (streamId == originalStreamId) { // Not a promised stream, no need to validate
                return;
            }
            if (!isValidStreamId(streamId)) {
                inboundRequestMsg.getHttpOutboundRespStatusFuture().
                        notifyHttpListener(new ServerConnectorException(PROMISED_STREAM_REJECTED_ERROR));
                throw new Http2Exception(Http2Error.REFUSED_STREAM, PROMISED_STREAM_REJECTED_ERROR);
            }
        }
    }

    private synchronized int getNextStreamId() {
        return conn.local().incrementAndGetNextStreamId();
    }

    private boolean isValidStreamId(int streamId) {
        return conn.stream(streamId) != null;
    }
}

