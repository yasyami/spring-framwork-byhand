package com.czl.spring.context;

import com.czl.spring.context.support.BeanDefinitionReader;

import java.util.List;

public class ApplicationContext {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    public ApplicationContext(String ... configLocations) {
        this.configLocations=configLocations;
        //ioc容器的入口方法
        refresh();
    }

    private void refresh() {

        //定位
        reader = new BeanDefinitionReader(configLocations);
        //加载
        List<String> beanDefinitions = reader.loadBeanDefinitions();
        //注册
        doRegister(beanDefinitions);
        //依赖注入

    }

    private void doRegister(List<String> beanDefinitions) {



    }

    //将beanDefinitions 注册值beanDefinitionMaps

}
