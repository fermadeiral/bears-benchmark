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

public class FilterParameters extends AbstractParameters {

    public static final ParameterDefinition filterClass;
    public static final ParameterDefinition exclude;

    private static final ParameterDefinition[] parameterDefinitions;

    static {
        filterClass = new ParameterDefinition("class", ParameterValueType.STRING);
        exclude = new ParameterDefinition("exclude", ParameterValueType.STRING, true);

        parameterDefinitions = new ParameterDefinition[] {
                filterClass,
                exclude
        };
    }

    public FilterParameters() {
        super(parameterDefinitions);
    }

}
