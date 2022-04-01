/* Licensed under the Apache License, Version 2.0 (the "License");
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
package org.activiti.engine.impl.bpmn.parser.handler;

import org.activiti.bpmn.model.BaseElement;
import org.activiti.bpmn.model.ParallelGateway;
import org.activiti.engine.impl.bpmn.parser.BpmnParse;

/**
 *顶级父类是用了设计模式之模版方法模式定义了抽象方法executeParse用于获取并设置其行为类,发现处理器主要是用于给元素节点设置行为类。
 */
public class ParallelGatewayParseHandler extends AbstractActivityBpmnParseHandler<ParallelGateway> {

  public Class<? extends BaseElement> getHandledType() {
    return ParallelGateway.class;
  }

  protected void executeParse(BpmnParse bpmnParse, ParallelGateway gateway) {
    gateway.setBehavior(bpmnParse.getActivityBehaviorFactory().createParallelGatewayActivityBehavior(gateway));
  }

}
