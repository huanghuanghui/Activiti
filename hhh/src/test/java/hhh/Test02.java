package hhh;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;

/**
 * businessKey
 * 流程挂起与激活
 */
public class Test02 {

    /**
     * 启动流程实例，添加businessKey（与业务数据关联）
     */
    @Test
    public void test01(){
        // 1.获取ProcessEngine对象
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 3.启动流程实例
        ProcessInstance instance = runtimeService
                .startProcessInstanceByKey("evection", "SP2022033000001");
        // 4.输出processInstance相关属性 使用业务组件，新增一个商品申请单
        System.out.println("businessKey = "+instance.getBusinessKey());
    }

    /**
     * act_ru_task.suspension_state_ :1:激活，2：暂停
     * 全部流程挂起实例与激活
     * 例如管理员需要修改审批流，就可以挂起，避免新的流程在被创建
     * 操作流程定义为挂起状态，该流程定义下的所有流程实例全部暂停，不能执行，比如审核的话，就会报错（org.activiti.engine.ActivitiException: Cannot complete a suspended task）
     * 流程定义为挂起状态，该流程定义将不允许启动新的流程实例，同时该流程定义下的所有的流程实例都将全部挂起，暂停执行。
     */
    @Test
    public void test02(){
       // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService对象
        RepositoryService repositoryService = engine.getRepositoryService();
        // 3.查询流程定义的对象
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("evection")
                .singleResult();
        // 4.获取当前流程定义的状态
        boolean suspended = processDefinition.isSuspended();
        String id = processDefinition.getId();
        // 5.如果挂起就激活，如果激活就挂起
        if(suspended){
            // 表示当前定义的流程状态是 挂起的
            repositoryService.activateProcessDefinitionById(
                    id // 流程定义的id
            ,true // 是否激活
            ,null // 激活时间
            );
            System.out.println("流程定义：" + id + ",已激活");
        }else{
            // 非挂起状态，激活状态 那么需要挂起流程定义
            repositoryService.suspendProcessDefinitionById(
                    id // 流程id
                    ,true // 是否挂起
                    ,null // 挂起时间
            );
            System.out.println("流程定义：" + id + ",已挂起");
        }
    }

    /**
     * 单个流程实例挂起与激活
     * - 比如流程在执行过程中需要变更流程，需要将当前流程暂停，而不是删除，流程暂停后，将不能继续执行
     * - 比如报销流程，财务月底需要封帐，不允许再去做报销与请款流程，这个时候就要将流程实例挂起
     * （如果不挂起，比如小张发起了一个请款流程，经理看到了，就审核通过了，就给财务造成困扰了，财务在业务上就冲突了）
     */
    @Test
    public void test03(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RuntimeService
        RuntimeService runtimeService = engine.getRuntimeService();
        // 3.获取流程实例对象
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId("2501")//act_ru_task.proc_inst_id_
                .singleResult();
        // 4.获取相关的状态操作
        boolean suspended = processInstance.isSuspended();
        String id = processInstance.getId();
        if(suspended){
            // 挂起--》激活
            runtimeService.activateProcessInstanceById(id);
            System.out.println("流程定义：" + id + "，已激活");
        }else{
            // 激活--》挂起
            runtimeService.suspendProcessInstanceById(id);
            System.out.println("流程定义：" + id + "，已挂起");
        }

    }

    /**
     * 流程任务的处理
     */
    @Test
    public void test04(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("wangwu")
                .singleResult();
        // 完成任务
        taskService.complete(task.getId());
    }
}
