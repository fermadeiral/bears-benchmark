/**
 * AET
 *
 * Copyright (C) 2013 Cognifide Limited
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.cognifide.aet.executor.xmlparser.xml;

import com.cognifide.aet.executor.model.CollectorStep;
import com.cognifide.aet.executor.xmlparser.xml.models.Collect;
import com.cognifide.aet.executor.xmlparser.xml.utils.ValidationUtils;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.OutputNode;

public class CollectConverter extends BasicPhaseConverter<Collect> {

  @Override
  public Collect read(InputNode node) throws Exception {
    List<CollectorStep> collectorSteps = Lists.newArrayList();

    InputNode inputNode;
    while ((inputNode = node.getNext()) != null) {
      Map<String, String> parameters = getParameters(inputNode);
      String name = StringUtils.defaultString(parameters.get("name"), inputNode.getName());
      CollectorStep collectorStep = new CollectorStep(inputNode.getName(), name, parameters);
      collectorSteps.add(collectorStep);
    }
    ValidationUtils.validateSleep(collectorSteps);
    return new Collect(collectorSteps);
  }

  @Override
  public void write(OutputNode node, Collect value) throws Exception {
    // no write capability needed
  }

}
