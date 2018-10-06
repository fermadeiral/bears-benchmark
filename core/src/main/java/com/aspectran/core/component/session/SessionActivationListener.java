/*
 * Copyright (c) 2008-2018 The Aspectran Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.component.session;

import java.util.EventListener;

/**
 * Objects that are bound to a session may listen to container events notifying
 * them that sessions will be passivated and that session will be activated.
 * A container that migrates session between VMs or persists sessions is required
 * to notify all attributes bound to sessions implementing SessionActivationListener.
 *
 * <p>Created: 2017. 9. 9.</p>
 */
public interface SessionActivationListener extends EventListener {

    /**
     * Notification that the session is about to be passivated.
     *
     * @param session the session to which the object is bound or unbound
     */
    void sessionWillPassivate(Session session);

    /**
     * Notification that the session has just been activated.
     *
     * @param session the session to which the object is bound or unbound
     */
    void sessionDidActivate(Session session);

}

