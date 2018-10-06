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

public class AdviceActionParameters extends AbstractParameters {

    public static final ParameterDefinition thrown;

    public static final ParameterDefinition action;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        thrown = new ParameterDefinition("thrown", ExceptionThrownParameters.class);
        action = new ParameterDefinition("action", ActionParameters.class);

        parameterDefinitions = new ParameterDefinition[] {
                thrown,
                action
        };
    }

    public AdviceActionParameters() {
        super(parameterDefinitions);
    }

}
