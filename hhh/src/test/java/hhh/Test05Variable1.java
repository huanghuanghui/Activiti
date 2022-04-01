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
 * 任务办理时设置变量（全局变量）
 * 在完成任务时，设置流程变量，该流程变量只有在该任务完成后，其他节点才可以使用该变量，他的作用域是整个流程实例，如果设置的
 * 流程变量key，在流程实例中已存在相同的名字，则后设置的变量替换前面设置的变量
 *
 * 局部变量设置：
 * - runtimeService.setVariableLocal
 * - taskService.setVariableLocal
 */
public class Test05Variable1 {

    /**
     * 部署流程
     */
    @Test
    public void test01() {
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
    public void test02() {
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RuntimeService runtimeService = engine.getRuntimeService();
        // 流程定义key
        String key = "evection-variable";
        // 创建变量集合
        Map<String, Object> variables = new HashMap<>();

        // 设置assignee的取值
        variables.put("assignee0", "张三1");
        variables.put("assignee1", "李四1");
        variables.put("assignee2", "王五1");
        variables.put("assignee3", "赵财务1");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(key, variables);
        // 输出信息
        System.out.println("获取流程实例名称：" + processInstance.getName());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
    }

    /**
     * 完成任务
     * 在完成任务绑定流程变量
     */
    @Test
    public void test03() {
        String key = "evection-variable";
        String assignee = "李四1";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey(key)
                .taskAssignee(assignee)
                .singleResult();

        Map<String, Object> variables = new HashMap<>();
        // 创建出差对象 POJO
        Evection evection = new Evection();
        // 设置出差天数
        evection.setNum(4d);
        // 定义流程变量到集合中
        variables.put("evection", evection);

        if (task != null) {
            taskService.complete(task.getId(), variables);
            System.out.println("任务执行完成...");
        }
    }

    /**
     * 通过当前流程实例设置变量
     * 通过流程实例ID，设置全局变量，该流程实例必须为执行完成
     * <p>
     * 这种设置是流程已经在运行中了，我们动态的给运行中的流程动态去插入变量
     */
    @Test
    public void setGlobalVariableByExecutionId() {
        //当前流程实例执行 id，通常设置为当前执行的流程实例
        String executionId = "60009";
        //获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        //获取RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        //创建出差pojo对象
        Evection evection = new Evection();
        //设置天数
        evection.setNum(3d);
        //通过流程实例 id设置流程变量
        runtimeService.setVariable(executionId, "evection", evection);
        //一次设置多个值
        //runtimeService.setVariables(executionId, variables)
        //runtimeService.setVariableLocal(executionId, variables);
    }


    /**
     * 通过任务实例
     * 任务id必须是当前待办任务id，act_ru_task中存在。
     * 如果该任务已结束，会报错 也可以通过taskService.getVariable()获取流程变量。
     */
    @Test
    public void setGlobalVariableByTaskId() {
        //当前待办任务id
        String taskId = "1404";
        //获取processEngine
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = processEngine.getTaskService();
        Evection evection = new Evection();
        evection.setNum(3);
        //通过任务设置流程变量
        taskService.setVariable(taskId, "evection", evection); //一次设置多个值
        //设置局部变量
        //taskService.setVariableLocal(taskId, variables);
    }


}
