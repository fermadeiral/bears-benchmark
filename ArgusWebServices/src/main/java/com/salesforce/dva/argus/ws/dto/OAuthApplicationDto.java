/*
 * Copyright (c) 2018, Salesforce.com, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * 3. Neither the name of Salesforce.com nor the names of its contributors may
 * be used to endorse or promote products derived from this software without
 * specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.salesforce.dva.argus.ws.dto;

import java.io.Serializable;

/**
 * OAuth Application DTO
 *
 * @author gaurav.kumar (gaurav.kumar@salesforce.com)
 */
public final class OAuthApplicationDto implements Serializable {

    //~ Instance fields ******************************************************************************************************************************

    String name;
    String clientId;
    String clientSecret;
    String redirectUri;

    //~ Methods **************************************************************************************************************************************


    public OAuthApplicationDto(String name, String clientId, String clientSecret, String redirectUri) {
        this.name = name;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    /**
     * Gets the OAuth Application Name
     * @return Returns OAuth Application Name
     */
    public String getName() {
        return name;
    }


    /**
     * Gets the OAuth client Id
     * @return  Returns OAuth Client Id
     */
    public String getClientId() {
        return clientId;
    }

    /**
     * Gets the OAuth client secret
     * @return  Returns OAuth client secret
     */
    public String getClientSecret() {
        return clientSecret;
    }


    /**
     * Gets the RedirectURI
     * @return  Returns Redirect URI
     */
    public String getRedirectUri() {
        return redirectUri;
    }
}

/* Copyright (c) 2018, Salesforce.com, Inc.  All rights reserved. */
