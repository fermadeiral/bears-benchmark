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
/*
 * Copyright 2008-2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aspectran.core.context;

/**
 * Class for constructing checked Exceptions with a given root cause.
 * 
 * @since 2.0.0
 */
public class AspectranCheckedException extends Exception {

    /** @serial */
    private static final long serialVersionUID = 3365225829643047037L;

    /**
     * Constructs a AspectranCheckedException.
     */
    public AspectranCheckedException() {
        super();
    }

    /**
     * Constructs a AspectranCheckedException with the specified message.
     *
     * @param msg the specific message
     */
    public AspectranCheckedException(String msg) {
        super(msg);
    }

    /**
     * Constructs a AspectranCheckedException with the wrapped exception.
     *
     * @param cause the real cause of the exception
     */
    public AspectranCheckedException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructs a AspectranCheckedException with the specified message and wrapped exception.
     *
     * @param msg the specific message
     * @param cause the real cause of the exception
     */
    public AspectranCheckedException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
