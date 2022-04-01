package hhh;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;

/**
 * 可以使用监听器来完成很多Activiti的流程业务。
 * 我们在此处使用监听器来完成负责人的指定，那么我们在流程设计的时候就不需要指定assignee
 * 具体玩法，参考MyTaskListener类，需要实现TaskListener
 * Event选项
 * - create:任务创建后触发
 * - assignment:任务分配后触发
 * - Delete:任务完成后触发
 * - All:所有事件都触发
 */
public class Test04Listener {

    /**
     * 先将新定义的流程部署到Activiti中数据库中
     */
    @Test
    public void test01(){
        // 1.获取ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService进行部署操作
        RepositoryService service = engine.getRepositoryService();
        // 3.使用RepositoryService进行部署操作
        Deployment deploy = service.createDeployment()
                .addClasspathResource("bpmn/evection-listener.bpmn") // 添加bpmn资源
                .addClasspathResource("bpmn/evection-listener.png") // 添加png资源
                .name("出差申请流程-UEL")
                .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }

    /**
     * 创建一个流程实例
     *    给流程定义中的 UEL表达式赋值
     *    启动的时候，会调用到MyTaskListener.notify方法，是通过
     *    <activiti:taskListener event="create" class="com.hhh.listener.MyTaskListener"/>
     *    进行配置的
     */
    @Test
    public void test02(){
      // 获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();

        // 创建流程实例
        runtimeService.startProcessInstanceByKey("evection-listener");
    }
}
