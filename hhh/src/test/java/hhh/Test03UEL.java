package hhh;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * activiti变量之UEL表达式
 *
 * JAVA EE6中规定的 Unified Expression Language 统一表达式语言
 * Activiti支持两种UEL表达式
 * - UEL-value
 * - UEL-method：整合Spring之后，可以写成${UserBean.getUserId()}，会从IOC容器中获取用户对象
 * 在定义bpmn的时候，将assignee作为变量填入， activiti:assignee="${assignee0}"
 *
 *
 * UEL-method 与 UEL-value 结合
 * 再比如:
 * ${ldapService.findManagerForEmployee(emp)}
 * ldapService 是 spring 容器的一个 bean，findManagerForEmployee 是该 bean 的一个方法，emp 是 activiti
 * 流程变量， emp 作为参数传到 ldapService.findManagerForEmployee 方法中。
 * 其它
 * 表达式支持解析基础类型、 bean、 list、 array 和 map，也可作为条件判断。 如下:
 * ${order.price > 100 && order.price < 250}
 */

public class Test03UEL {

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
                .addClasspathResource("bpmn/evection-uel.bpmn") // 添加bpmn资源
                .addClasspathResource("bpmn/evection-uel.png") // 添加png资源
                .name("出差申请流程-UEL")
                .deploy();// 部署流程
        // 4.输出流程部署的信息
        System.out.println("流程部署的id:" + deploy.getId());
        System.out.println("流程部署的名称：" + deploy.getName());
    }

    /**
     * 创建一个流程实例
     *    给流程定义中的 UEL表达式赋值
     *    声明的变量会存入值到act_ru_variable中，UEL表达式对应的信息值
     */
    @Test
    public void test02(){
      // 获取流程引擎
        ProcessEngine processEngine = ProcessEngines.getDefaultProcessEngine();
        // 获取RuntimeService对象
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 设置 assignee 的取值，
        Map<String,Object> map = new HashMap<>();
        map.put("assignee0","张三");
        map.put("assignee1","李四");
        map.put("assignee2","王五");
        map.put("assignee3","赵财务");
        // 创建流程实例
        runtimeService.startProcessInstanceByKey("evection-uel",map);
    }
}
