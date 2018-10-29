package com.czl.spring.beans;

public class BeanWrapper {

    private Object wrapperInstance;

    private Object originalInstance;

    private BeanPostProcessor beanPostProcessor;


    public BeanWrapper(Object instance) {
        this.originalInstance = instance;
        this.wrapperInstance = instance;
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
}
