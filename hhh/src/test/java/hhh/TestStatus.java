package hhh;

import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 *Activiti工作流-任务的办理、回退和拒绝
 * - completeTask(String taskId)：办理任务，任务的办理即将当前任务审核通过，流转到下一个节点
 * - taskRollBack(String taskId);：回退任务，任务的回退即将当前任务退回到上一次审批级别。
 * - rejectTask(String taskId, String processInstanceId)：任务的拒绝，即无论当前审批到哪一个节点，都回到待提交审批的状态。
 */
public class TestStatus {



    /**
     * 实现文件的单个部署
     */
    @Test
    public void test01(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        // 3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("bpmn/evection.bpmn") // 添加bpmn资源
                .addClasspathResource("bpmn/evection.png") // 添加png资源
                .name("出差申请流程")
                .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }


    /**
     * 启动一个流程实例
     */
    @Test
    public void test02(){
        // 1.创建ProcessEngine对象 StandaloneProcessEngineConfiguration，将创建的对象存入processEngines，取出key为default的vaule
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService对象
        RuntimeService runtimeService = engine.getRuntimeService();
        // 3.根据流程定义的id启动流程
        String id= "evection";
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(id);
        // 4.输出相关的流程实例信息
        System.out.println("流程定义的ID：" + processInstance.getProcessDefinitionId());
        System.out.println("流程实例的ID：" + processInstance.getId());
        System.out.println("当前活动的ID：" + processInstance.getActivityId());
    }

    /**
     * 流程任务的处理
     */
    @Test
    public void test03(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("zhansan")
                .singleResult();
        // 完成任务
        taskService.complete(task.getId());
    }

    /**
     * 流程任务的回退
     */
    @Test
    public void test04(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("zhansan")
                .singleResult();
        // 完成任务
        taskService.complete(task.getId());
    }

}
