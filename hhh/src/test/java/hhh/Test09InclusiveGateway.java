package hhh;


import com.hhh.pojo.Evection;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * 包含网关
 * 包含网关可以看做是排他网关和并行网关的结合体。
 * 和排他网关一样，你可以在外出顺序流上定义条件，包含网关会解析它们。
 * 但是主要的区别是包含网关 可以选择多于一条顺序流，这和并行网关一样。
 * 包含网关的功能是基于进入和外出顺序流的: l 分支:
 * 所有外出顺序流的条件都会被解析，结果为true的顺序流会以并行方式继续执行， 会为每个顺序流创建 一个分支。
 * l 汇聚:
 * 所有并行分支到达包含网关，会进入等待状态， 直到每个包含流程token的进入顺序流的分支都到达。
 * 这是与并行网关的最大不同。换句话说，包含网关只会等待被选中执行了的进入顺序流。 在汇聚之后， 流程会穿过包含网关继续执行。
 *
 *
 * 业务解释：
 * 对于财务往来，财务一定要审核，然后选择金额大小，在决定是总经理审核还是部门经理审核。就可以使用包含网关
 */
public class Test09InclusiveGateway {

    /**
     * 流程部署
     */
    @Test
    public void test01(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("bpmn/evection-gateway3.bpmn")
                .addClasspathResource("bpmn/InclusiveGateway.png")
                .name("出差申请单-包含网关")
                .deploy();
        System.out.println("流程名称：" + deploy.getName());
        System.out.println("流程定义ID：" + deploy.getId());
    }

    /**
     * 启动流程实例
     */
    @Test
    public void test02(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        Evection evection = new Evection();
        evection.setNum(4d);
        Map<String,Object> map = new HashMap<>();
        map.put("evection",evection);
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("gateway3",map);
        // 4.输出流程部署的信息
        System.out.println("获取流程实例名称："+processInstance.getName());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
    }


    /**
     * 完成任务
     */
    @Test
    public void test07(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String userId= "wangwu";
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("gateway3")
                .taskAssignee(userId)
                .singleResult();

        if(task != null){
            taskService.complete(task.getId());
        }
    }
}
