package com.sevenfish.util;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Component
public class SpringContextUtils implements ApplicationContextAware, BeanFactoryAware {

    private ApplicationContext applicationContext;

    private BeanFactory beanFactory;

    public SpringContextUtils(){
        System.out.println("==============SpringContextUtils instance==============");
    }

    @PostConstruct
    private void init(){
        System.out.println("==============SpringContextUtils init==============");
    }

    @PreDestroy
    private void destroy(){
        System.out.println("==============SpringContextUtils destroy==============");
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("==============SpringContextUtils injects applicationContext==============");
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        System.out.println("==============SpringContextUtils injects beanFactory==============");
        this.beanFactory = beanFactory;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public <T> T getBean(Class<T> clazz){
        return applicationContext.getBean(clazz);
    }

    public Object getBean(String name){
        return applicationContext.getBean(name);
    }
}
