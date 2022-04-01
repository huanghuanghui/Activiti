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
 * 任务部署的时候设置变量（全局变量，一整个实例任务共享）
 *
 * 在activiti中，变量分为全局变量与局部变量
 * - 全局变量：全流程实例共享的变量
 * - 局部变量：当前任务的变量，设置的变量，只能在当前流程任务结束前使用，任务结束，变量无法在当前实例使用，可以通过历史任务查询
 *
 */
public class Test05Variable {

    /**
     * 部署流程
     */
    @Test
    public void test01(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        // 3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("bpmn/evection-variable.bpmn") // 添加bpmn资源
                .addClasspathResource("bpmn/evection-variable.png") // 添加png资源
                .name("出差申请流程-流程变量")
                .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }

    /**
     * 启动流程实例，设置流程变量
     */
    @Test
    public void test02(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        // 流程定义key
        String key = "evection-variable";
        // 创建变量集合
        Map<String,Object> variables = new HashMap<>();
        // 创建出差对象 POJO，需要实现序列化接口，对象相关会被序列化后存到表act_ge_bytearray
        Evection evection = new Evection();
        // 设置出差天数
        evection.setNum(4d);
        // 定义流程变量到集合中
        variables.put("evection",evection);
        // 设置assignee的取值
        variables.put("assignee0","张三1");
        variables.put("assignee1","李四1");
        variables.put("assignee2","王五1");
        variables.put("assignee3","赵财务1");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
        // 输出信息
        System.out.println("获取流程实例名称："+processInstance.getName());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
    }

    /**
     * 完成任务
     */
    @Test
    public void test03(){
        String key = "evection-variable";
        String assignee = "赵财务1";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(key)
                .taskAssignee(assignee)
                .singleResult();
        if(task != null){
            taskService.complete(task.getId());
            System.out.println("任务执行完成...");
        }
    }
}
