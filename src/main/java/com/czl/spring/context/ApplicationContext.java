package com.czl.spring.context;

import com.czl.spring.context.support.BeanDefinitionReader;
import com.czl.spring.core.BeanFactory;

import java.util.List;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory{

    private String[] configLocations;

    private BeanDefinitionReader reader;

    public ApplicationContext(String... configLocations) {
        this.configLocations = configLocations;
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

        if (null == beanDefinitions || beanDefinitions.isEmpty()) {
            return;
        }
        try {
            for (String className : beanDefinitions) {
                Class.forName(className);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object getBean(String beanName) {
        return null;
    }

    //将beanDefinitions 注册值beanDefinitionMaps

}
