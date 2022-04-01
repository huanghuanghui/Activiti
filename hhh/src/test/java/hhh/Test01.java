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
 * 创建简单实例，并完成
 */
public class Test01 {

    /**
     * 生成Activiti的相关的表结构
     * 存数据到act_ge_property，系统相关属性表中
     */
    @Test
    public void test01(){
        // 使用classpath下的activiti.cfg.xml中的配置来创建 ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        System.out.println(engine);

    }

    /**
     * 自定义的方式来加载配置文件
     */
    @Test
    public void test02(){
        // 首先创建ProcessEngineConfiguration对象
        ProcessEngineConfiguration configuration =
                ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml");
        // 通过ProcessEngineConfiguration对象来创建 ProcessEngine对象
        ProcessEngine processEngine = configuration.buildProcessEngine();
    }

    /**
     * 实现文件的单个部署
     *
     * 下3个表是流程定义表：
     * act_ge_bytearray：存入bpmn相关资源与png
     * act_re_deployment：生成部署的任务信息
     * act_re_procdef：存入已部署的流程定义的详细信息
     *
     */
    @Test
    public void test03(){
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
     * 通过一个zip文件来部署操作（文件比较多的时候，可以选择打zip的方式，进行部署）
     */
    @Test
    public void test04(){
        // 定义zip文件的输入流
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("bpmn/evection.zip");
        // 对 inputStream 做装饰
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        Deployment deploy = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .name("出差申请流程")
                .deploy();
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }

    /**
     * 启动一个流程实例
     * 流程定义部署在Activiti后就可以通过工作流管理业务流程，也就是说上边部署的出差申请流程可以使用了。
     * 针对该流程，启动一个流程表示发起一个新的出差申请单，这就相当于Java类和Java对象的关系，
     * 类定 义好了后需要new创建一个对象使用，当然可以new出多个对象来，对于出差申请流程，张三可以发起
     * 一个出差申请单需要启动一个流程实例。
     * 启动流程实例涉及到的表结构
     *
     * -- 主运行数据
     * act_ru_execution 流程执行信息
     * act_ru_identitylink 流程的参与用户信息
     * act_ru_task 任务信息
     * act_ru_variable 任务变量信息
     *
     *
     *-- 历史信息数据
     * act_hi_actinst 流程实例执行历史
     * act_hi_identitylink 流程的参与用户的历史信息
     * act_hi_procinst 流程实例历史信息
     * act_hi_taskinst 流程任务历史信息
     */
    @Test
    public void test05(){
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
     * 任务查询
     */
    @Test
    public void test06(){
//        String assignee ="lisi";//如果不是拥有审批流的用户，查询出来没有权限
        String assignee ="zhansan";//查询出来是ACT_RU_TASK ASSIGNEE_中的用户是谁，谁就可以审批
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 任务查询 需要获取一个 TaskService 对象
        TaskService taskService = engine.getTaskService();
        // 根据流程的key和任务负责人 查询任务
        List<Task> list = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee(assignee)
                .list();
        // 输出当前用户具有的任务
        for (Task task : list) {
            //对应的是 evection.bpmn
            //<userTask id="usertask1" name="创建请假单" activiti:assignee="zhansan"></userTask>
            System.out.println("流程实例id：" + task.getProcessInstanceId());
            System.out.println("任务id:" + task.getId());
            System.out.println("任务负责人：" + task.getAssignee());
            System.out.println("任务名称：" + task.getName());
        }
    }

    /**
     * 流程任务的处理
     * 任务负责人查询出来了待办的人，选择任务进行处理，完成任务
     * 不同的用户，处理任务，直到任务处理完成
     */
    @Test
    public void test07(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        /**
         * select distinct RES.*
         * from ACT_RU_TASK RES
         *          inner join ACT_RE_PROCDEF D on RES.PROC_DEF_ID_ = D.ID_
         * WHERE RES.ASSIGNEE_ = 'zhansan'
         *   and D.KEY_ = 'evection'
         * order by RES.ID_ asc
         * LIMIT 2147483647 OFFSET 0
         */
        TaskService taskService = engine.getTaskService();
        Task task = taskService.createTaskQuery()
                .processDefinitionKey("evection")
                .taskAssignee("zhansan")
                .singleResult();
        // 完成任务
        taskService.complete(task.getId());
    }

    /**
     * 查询流程的定义
     */
    @Test
    public void test08(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        // 获取一个 ProcessDefinitionQuery对象 用来查询操作
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> list = processDefinitionQuery.processDefinitionKey("evection")
                .orderByProcessDefinitionVersion() // 安装版本排序
                .desc() // 倒序
                .list();
        // 输出流程定义的信息
        for (ProcessDefinition processDefinition : list) {
            System.out.println("流程定义的ID：" + processDefinition.getId());
            System.out.println("流程定义的name：" + processDefinition.getName());
            System.out.println("流程定义的key:" + processDefinition.getKey());
            System.out.println("流程定义的version:" + processDefinition.getVersion());
            System.out.println("流程部署的id:" + processDefinition.getDeploymentId());
        }
    }

    /**
     * 删除流程
     */
    @Test
    public void test09(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        RepositoryService repositoryService = engine.getRepositoryService();
        // 删除流程定义，如果该流程定义已经有了流程实例启动则删除时报错
        repositoryService.deleteDeployment("20001");
        // 设置为TRUE 级联删除流程定义，及时流程有实例启动，也可以删除，设置为false 非级联删除操作。
        //repositoryService.deleteDeployment("12501",true);
    }

    /**
     * 读取数据库中的资源文件
     * 流程已经上传到数据库，我们可以获取数据库中的流程资源
     */
    @Test
    public void test10() throws Exception{
        // 1.得到ProcessEngine对象
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 2.获取RepositoryService对象
        RepositoryService repositoryService = engine.getRepositoryService();
        // 3.得到查询器
        ProcessDefinition definition = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey("evection")
                .singleResult();
        // 4.获取流程部署的id
        String deploymentId = definition.getDeploymentId();
        // 5.通过repositoryService对象的相关方法 来获取图片信息和bpmn信息
        // png图片
        InputStream pngInput = repositoryService
                .getResourceAsStream(deploymentId, definition.getDiagramResourceName());
        // bpmn 文件的流
        InputStream bpmnInput = repositoryService
                .getResourceAsStream(deploymentId, definition.getResourceName());
        // 6.文件的保存
        File filePng = new File("/Users/hhh/workspace/flow/log/evection.png");
        File fileBpmn = new File("/Users/hhh/workspace/flow/log/evection.bpmn");
        OutputStream pngOut = new FileOutputStream(filePng);
        OutputStream bpmnOut = new FileOutputStream(fileBpmn);

        IOUtils.copy(pngInput,pngOut);
        IOUtils.copy(bpmnInput,bpmnOut);

        pngInput.close();
        pngOut.close();
        bpmnInput.close();
        bpmnOut.close();
    }

    /**
     * 流程历史信息查看
     * 就算审批流通过delete api被删除，我们也能通过数据库中HistoryService相关的表中查询历史信息
     */
    @Test
    public void test11(){
        ProcessEngine engine = ProcessEngines.getDefaultProcessEngine();
        // 查看历史信息我们需要通过 HistoryService来实现
        HistoryService historyService = engine.getHistoryService();
        // 获取 actinst 表的查询对象
        HistoricActivityInstanceQuery instanceQuery = historyService.createHistoricActivityInstanceQuery();
        instanceQuery.processDefinitionId("evection:1:4");
        instanceQuery.orderByHistoricActivityInstanceStartTime().desc();
        List<HistoricActivityInstance> list = instanceQuery.list();
        // 输出查询的结果
        for (HistoricActivityInstance hi : list) {
            System.out.println(hi.getActivityId());
            System.out.println(hi.getActivityName());
            System.out.println(hi.getActivityType());
            System.out.println(hi.getAssignee());
            System.out.println(hi.getProcessDefinitionId());
            System.out.println(hi.getProcessInstanceId());
            System.out.println("-----------------------");
        }
    }
}
