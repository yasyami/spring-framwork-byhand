package com.czl.spring.context;

import com.czl.spring.context.support.BeanDefinitionReader;

public class ApplicationContext {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    public ApplicationContext(String ... configLocations) {
    }
}
