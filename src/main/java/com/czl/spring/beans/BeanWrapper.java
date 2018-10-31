package com.czl.spring.beans;

import com.czl.spring.aop.AopConfig;
import com.czl.spring.aop.AopProxy;
import com.czl.spring.core.FactoryBean;

public class BeanWrapper extends FactoryBean{

    private AopProxy aopProxy=new AopProxy();

    private Object wrapperInstance;

    private Object originalInstance;

    private BeanPostProcessor beanPostProcessor;


    public BeanWrapper(Object instance) {
        this.originalInstance =instance;
        this.wrapperInstance = aopProxy.getProxy(instance) ;
    }

    public Object getWrapperInstance() {
        return wrapperInstance;
    }

    public Object getOriginalInstance() {
        return originalInstance;
    }

    public BeanPostProcessor getBeanPostProcessor() {
        return beanPostProcessor;
    }

    public void setBeanPostProcessor(BeanPostProcessor beanPostProcessor) {
        this.beanPostProcessor = beanPostProcessor;
    }

   public void setAopConfig(AopConfig aopConfig){
        aopProxy.setConfig(aopConfig);
   }
}
