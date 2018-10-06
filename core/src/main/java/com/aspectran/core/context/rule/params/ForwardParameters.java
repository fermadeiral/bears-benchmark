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
package com.aspectran.core.context.rule.params;

import com.aspectran.core.util.apon.AbstractParameters;
import com.aspectran.core.util.apon.ParameterDefinition;
import com.aspectran.core.util.apon.ParameterValueType;

public class ForwardParameters extends AbstractParameters {

    public static final ParameterDefinition action;
    public static final ParameterDefinition contentType;
    public static final ParameterDefinition translet;
    public static final ParameterDefinition defaultResponse;
    public static final ParameterDefinition attributes;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        action = new ParameterDefinition("action", ActionParameters.class, true, true);
        contentType = new ParameterDefinition("contentType", ParameterValueType.STRING);
        translet = new ParameterDefinition("translet", ParameterValueType.STRING);
        defaultResponse = new ParameterDefinition("default", ParameterValueType.BOOLEAN);
        attributes = new ParameterDefinition("attributes", ItemHolderParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                action,
                contentType,
                translet,
                defaultResponse,
                attributes
        };
    }

    public ForwardParameters() {
        super(parameterDefinitions);
    }

}
