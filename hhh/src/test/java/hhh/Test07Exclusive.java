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
 * 排他网关：
 * 排他网关，用来在流程中实现决策。 当流程执行到这个网关，所有分支都会判断条件是否为true，如果 为true则执行该分支，
 * 注意:排他网关只会选择一个为true的分支执行。如果有两个分支条件都为true，排他网关会选择id值 较小的一条分支去执行。
 *
 * 为什么要用排他网关? 不用排他网关也可以实现分支，如:在连线的condition条件上设置分支条件。
 * 在连线设置condition条件的缺点:如果条件都不满足，流程就结束了(是异常结束，就是流程断了)。
 *
 * 如果 使用排他网关决定分支的走向，网关出去的线所有条件都不满足则系统抛出异常。
 */
public class Test07Exclusive {

    /**
     * 流程部署
     */
    @Test
    public void test01(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("bpmn/evection-gateway1.bpmn")
                .addClasspathResource("bpmn/evection-exclusive.png")
                .name("出差申请单-排他网关")
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
        evection.setNum(2d);
        Map<String,Object> map = new HashMap<>();
        map.put("evection",evection);
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceByKey("gateway1",map);
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
        String userId= "zhao";
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("gateway1")
                .taskAssignee(userId)
                .singleResult();

        if(task != null){
            taskService.complete(task.getId());
        }
    }
}
