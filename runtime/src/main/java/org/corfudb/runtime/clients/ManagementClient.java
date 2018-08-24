package org.corfudb.runtime.clients;

import io.netty.channel.ChannelHandlerContext;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import lombok.Getter;
import lombok.Setter;
import org.corfudb.protocols.wireprotocol.CorfuMsg;
import org.corfudb.protocols.wireprotocol.CorfuMsgType;
import org.corfudb.protocols.wireprotocol.CorfuPayloadMsg;
import org.corfudb.protocols.wireprotocol.DetectorMsg;
import org.corfudb.protocols.wireprotocol.orchestrator.AddNodeRequest;
import org.corfudb.protocols.wireprotocol.orchestrator.CreateWorkflowResponse;
import org.corfudb.protocols.wireprotocol.orchestrator.ForceRemoveNodeRequest;
import org.corfudb.protocols.wireprotocol.orchestrator.HealNodeRequest;
import org.corfudb.protocols.wireprotocol.orchestrator.OrchestratorMsg;
import org.corfudb.protocols.wireprotocol.orchestrator.OrchestratorResponse;
import org.corfudb.protocols.wireprotocol.orchestrator.QueryRequest;
import org.corfudb.protocols.wireprotocol.orchestrator.QueryResponse;
import org.corfudb.protocols.wireprotocol.orchestrator.RemoveNodeRequest;

import org.corfudb.runtime.exceptions.AlreadyBootstrappedException;
import org.corfudb.runtime.exceptions.NoBootstrapException;
import org.corfudb.runtime.view.Layout;
import org.corfudb.util.CFUtils;

import javax.annotation.Nonnull;

/**
 * A client to the Management Server.
 *
 * <p>Failure Detection:
 * This client allows a client to trigger failures handlers with relevant failures.
 *
 * <p>Created by zlokhandwala on 11/4/16.
 */
public class ManagementClient implements IClient {

    @Setter
    @Getter
    IClientRouter router;

    /**
     * The handler and handlers which implement this client.
     */
    @Getter
    public ClientMsgHandler msgHandler = new ClientMsgHandler(this)
            .generateHandlers(MethodHandles.lookup(), this);


    @ClientHandler(type = CorfuMsgType.ORCHESTRATOR_RESPONSE)
    private static Object handleOrchestratorResponse(CorfuPayloadMsg<OrchestratorResponse> msg,
                                                     ChannelHandlerContext ctx, IClientRouter r) {
        return msg.getPayload();
    }

    @ClientHandler(type = CorfuMsgType.HEARTBEAT_RESPONSE)
    private static Object handleHeartbeatResponse(CorfuPayloadMsg<byte[]> msg,
                                                  ChannelHandlerContext ctx, IClientRouter r) {
        return msg.getPayload();
    }

    @ClientHandler(type = CorfuMsgType.MANAGEMENT_NOBOOTSTRAP_ERROR)
    private static Object handleNoBootstrapError(CorfuMsg msg,
                                                 ChannelHandlerContext ctx, IClientRouter r)
            throws Exception {
        throw new NoBootstrapException();
    }

    @ClientHandler(type = CorfuMsgType.MANAGEMENT_ALREADY_BOOTSTRAP_ERROR)
    private static Object handleAlreadyBootstrappedError(CorfuMsg msg,
                                                         ChannelHandlerContext ctx, IClientRouter r)
            throws Exception {
        throw new AlreadyBootstrappedException();
    }

    /**
     * Bootstraps a management server.
     *
     * @param l The layout to bootstrap with.
     * @return A completable future which will return TRUE if the
     * bootstrap was successful, false otherwise.
     */
    public CompletableFuture<Boolean> bootstrapManagement(Layout l) {
        return router.sendMessageAndGetCompletable(CorfuMsgType.MANAGEMENT_BOOTSTRAP_REQUEST
                .payloadMsg(l));
    }

    /**
     * Sends the failure detected to the relevant management server.
     *
     * @param failedNodes The failed nodes set to be handled.
     * @return A future which will be return TRUE if completed successfully else returns FALSE.
     */
    public CompletableFuture<Boolean> handleFailure(long detectorEpoch, Set<String> failedNodes) {
        return router.sendMessageAndGetCompletable(CorfuMsgType.MANAGEMENT_FAILURE_DETECTED
                .payloadMsg(new DetectorMsg(detectorEpoch, failedNodes, Collections.emptySet())));
    }

    /**
     * Sends the healed nodes detected to the relevant management server.
     *
     * @param healedNodes The healed nodes set to be handled.
     * @return A future which will be return TRUE if completed successfully else returns FALSE.
     */
    public CompletableFuture<Boolean> handleHealing(long detectorEpoch, Set<String> healedNodes) {
        return router.sendMessageAndGetCompletable(CorfuMsgType.MANAGEMENT_HEALING_DETECTED
                .payloadMsg(new DetectorMsg(detectorEpoch, Collections.emptySet(), healedNodes)));
    }

    /**
     * Requests for a heartbeat message containing the node status.
     *
     * @return A future which will return the node health metrics of
     * the node which was requested for the heartbeat.
     */
    public CompletableFuture<byte[]> sendHeartbeatRequest() {
        return router.sendMessageAndGetCompletable(CorfuMsgType.HEARTBEAT_REQUEST.msg());
    }

    /**
     * Send a add node request to an orchestrator
     * @param endpoint the endpoint to add to the cluster
     * @return create workflow response that contains the uuid of the workflow
     */
    public CreateWorkflowResponse addNodeRequest(@Nonnull String endpoint) {
        OrchestratorMsg req = new OrchestratorMsg(new AddNodeRequest(endpoint));
        CompletableFuture<OrchestratorResponse> resp = router.sendMessageAndGetCompletable(CorfuMsgType
                .ORCHESTRATOR_REQUEST
                .payloadMsg(req));
        return (CreateWorkflowResponse) CFUtils.getUninterruptibly(resp).getResponse();
    }

    /**
     * Creates a workflow request to heal a node.
     *
     * @param endpoint          Endpoint of the node to be healed.
     * @param isLayoutServer    True if the node to be healed is a layout server.
     * @param isSequencerServer True if the node to be healed is a sequencer server.
     * @param isLogUnitServer   True if the node to be healed is a logunit server.
     * @param stripeIndex       Stripe index of the node if it is a logunit server.
     * @return CreateWorkflowResponse which gives the workflowId.
     */
    public CreateWorkflowResponse healNodeRequest(@Nonnull String endpoint,
                                                  boolean isLayoutServer,
                                                  boolean isSequencerServer,
                                                  boolean isLogUnitServer,
                                                  int stripeIndex) {
        OrchestratorMsg req = new OrchestratorMsg(
                new HealNodeRequest(endpoint,
                        isLayoutServer,
                        isSequencerServer,
                        isLogUnitServer,
                        stripeIndex));
        CompletableFuture<OrchestratorResponse> resp =
                router.sendMessageAndGetCompletable(CorfuMsgType
                        .ORCHESTRATOR_REQUEST
                        .payloadMsg(req));
        return (CreateWorkflowResponse) CFUtils.getUninterruptibly(resp).getResponse();
    }

    /**
     * Query the state of a workflow on a particular orchestrator.
     * @param workflowId the workflow to query
     * @return Query response that contains whether the workflow is running or not
     */
    public QueryResponse queryRequest(@Nonnull UUID workflowId) {
        OrchestratorMsg req = new OrchestratorMsg(new QueryRequest(workflowId));
        CompletableFuture<OrchestratorResponse> resp = router.sendMessageAndGetCompletable(CorfuMsgType
                .ORCHESTRATOR_REQUEST
                .payloadMsg(req));
        return (QueryResponse) CFUtils.getUninterruptibly(resp).getResponse();
    }

    /**
     * Return a workflow id for this remove operation request.
     * @param endpoint the endpoint to remove
     * @return uuid of the remove workflow
     */
    public CreateWorkflowResponse removeNode(@Nonnull String endpoint) {
        OrchestratorMsg req = new OrchestratorMsg(new RemoveNodeRequest(endpoint));
        CompletableFuture<OrchestratorResponse> resp = router.sendMessageAndGetCompletable(CorfuMsgType
                .ORCHESTRATOR_REQUEST
                .payloadMsg(req));
        return (CreateWorkflowResponse) CFUtils.getUninterruptibly(resp).getResponse();
    }

    /**
     *
     * Send a force remove node request to an orchestrator service node.
     *
     * @param endpoint the endpoint to force remove
     * @return CreateWorkflowResponse
     */
    public CreateWorkflowResponse forceRemoveNode(@Nonnull String endpoint) {
        OrchestratorMsg req = new OrchestratorMsg(new ForceRemoveNodeRequest(endpoint));
        CompletableFuture<OrchestratorResponse> resp = router.sendMessageAndGetCompletable(CorfuMsgType
                .ORCHESTRATOR_REQUEST
                .payloadMsg(req));
        return (CreateWorkflowResponse) CFUtils.getUninterruptibly(resp).getResponse();
    }
}
