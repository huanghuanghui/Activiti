package com.hhh.listener;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;

public class MyTaskListener implements TaskListener {
    @Override
    public void notify(DelegateTask delegateTask) {
        //跟Spring的afterPropertiesSet一样，所有的流程都会调用这个方法，所以需要判断，特定的条件才触发
        if("创建请假单".equals(delegateTask.getName()) && "create".equals(delegateTask.getEventName())){
            // 指定任务的负责人
            delegateTask.setAssignee("张三-Listener");
        }

    }
}
