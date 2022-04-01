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
 * 并行网关其实就是会签任务，代表多个执行人需要共同执行的任务
 *
 * 并行网关允许将流程分成多条分支，也可以把多条分支汇聚到一起，并行网关的功能是基于进入和外出
 * 顺序流的:
 * l fork分支: 并行后的所有外出顺序流，为每个顺序流都创建一个并发分支。
 * l join汇聚:
 * 所有到达并行网关，在此等待的进入分支， 直到所有进入顺序流的分支都到达以后， 流程就会通过汇聚
 * 网关。
 * 注意，如果同一个并行网关有多个进入和多个外出顺序流， 它就同时具有分支和汇聚功能。
 * 这时，网关 会先汇聚所有进入的顺序流，然后再切分成多个并行分支。
 * 与其他网关的主要区别是，并行网关不会解析条件。 即使顺序流中定义了条件，也会被忽略。
 */
public class Test08ParallelGateway {

    /**
     * 流程部署
     */
    @Test
    public void test01(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("bpmn/evection-gateway2.bpmn")
                .addClasspathResource("bpmn/parallel.png")
                .name("出差申请单-并行网关")
                .deploy();
        System.out.println("流程名称：" + deploy.getName());
        System.out.println("流程定义ID：" + deploy.getId());
    }

    /**
     * 启动流程实例
     * 技术经理和项目经理是两个execution分支，在act_ru_execution表有两条记录分别是技术经理和项目经
     * 理，act_ru_execution还有一条记录表示该流程实例。 待技术经理和项目经理任务全部完成，在汇聚点汇聚，
     * 通过parallelGateway并行网关。 并行网关在业务应用中常用于会签任务，会签任务即多个参与者共同办理的任务。
     * 可以查看parallel.png流程图
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
                .startProcessInstanceByKey("myProcess",map);
        // 4.输出流程部署的信息
        System.out.println("获取流程实例名称："+processInstance.getName());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
    }


    /**
     * 完成任务
     * 并行网关，相当于会签的概念，需要等到多个会签任务全部完成，流程才会继续向下，会生成多条act_ru_task
     * 审核完成后，需要重新聚合
     */
    @Test
    public void test07(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        //String taskId = "75002";
        String userId= "zhao";
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("myProcess")
                .taskAssignee(userId)
                .singleResult();

        if(task != null){
            taskService.complete(task.getId());
        }
    }
}
