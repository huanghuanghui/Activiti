package hhh;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

import java.util.List;

/**
 * 组任务
 * 在流程定义中在任务结点的 assignee 固定设置任务负责人，在流程定义时将参与者固定设置在.bpmn
 * 文件中，如果临时任务负责人变更则需要修改流程定义，系统可扩展性差。
 * 针对这种情况可以给任务设置多个候选人，可以从候选人中选择参与者来完成任务。
 *
 *
 *
 * a、查询组任务
 * 指定候选人，查询该候选人当前的待办任务。
 * 候选人不能立即办理任务。
 * b、拾取(claim)任务
 * 该组任务的所有候选人都能拾取。 将候选人的组任务，变成个人任务。原来候选人就变成了该任务的负责人。 如果拾取后不想办理该任务?
 *  需要将已经拾取的个人任务归还到组里边，将个人任务变成了组任务。
 * c、查询个人任务 查询方式同个人任务部分，根据assignee查询用户负责的个人任务。
 *
 * d、办理任务
 *
 */
public class Test06Group {

    /**
     * 流程部署
     */
    @Test
    public void test01(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("bpmn/evection1.bpmn")
                .addClasspathResource("bpmn/evection1.png")
                .name("出差申请单-组任务")
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
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("evection1");
        // 4.输出流程部署的信息
        System.out.println("获取流程实例名称："+processInstance.getName());
        System.out.println("流程定义ID：" + processInstance.getProcessDefinitionId());
    }

    /**
     * 查询组任务（查询个人代办）
     * 在表act_ru_identitylink中查询候选人信息
     */
    @Test
    public void test03(){
        String key = "evection1";
        String candidateUser = "lisi";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey(key)
                //根据候选者查询
                .taskCandidateUser(candidateUser)
                //根据任务候选者或者执行人查询
                //.taskCandidateOrAssigned(candidateUser)
                //使用执行人查询
                //.taskAssignee(candidateUser)
                .list();
        for (Task task : list) {
            System.out.println("流程实例Id：" + task.getProcessInstanceId());
            System.out.println("任务ID：" + task.getId());
            System.out.println("负责人:" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    /**
     * 候选人 拾取任务
     * 判断单据中的assignee是否为空
     * 拾取任务后，该任务就会成为当前人的特定任务，只有他自己可以看了
     */
    @Test
    public void test04(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String taskId = "7505";
        // 候选人
        String userId = "wangwu";
        // 拾取任务
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskCandidateUser(userId) // 根据候选人查询
                .singleResult();
        if(task != null){
            // 可以拾取任务
            taskService.claim(taskId,userId);
            System.out.println("拾取成功");
        }
    }

    /**
     * 完成个人任务
     */
    @Test
    public void test05(){
        String  taskId = "7505";
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        taskService.complete(taskId);
        System.out.println("完成任务：" + taskId);
    }

    /**
     * 归还任务
     *
     * 用户在任务池中，拾取任务后，又不想做了，可以归还任务
     */
    @Test
    public void test06(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String taskId = "7505";
        String userId= "wangwu";
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if(task != null){
            // 如果设置为null，归还组任务，任务没有负责人
            taskService.setAssignee(taskId,null);
        }
    }

    /**
     * 任务交接
     * 比如某个人辞职，那么就可以将他手上的任务转交给其他人处理
     * 任务负责人将任务交给其他负责人来处理
     *
     * 直接设置assignee为指定的负责人就行了
     */
    @Test
    public void test07(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        String taskId = "75002";
        String userId= "zhangsan";
        Task task = taskService.createTaskQuery()
                .taskId(taskId)
                .taskAssignee(userId)
                .singleResult();
        if(task != null){
            // 设置该任务的新的负责人
            taskService.setAssignee(taskId,"赵六");
        }
    }
}
