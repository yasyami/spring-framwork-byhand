package com.czl.spring.context.support;

import com.czl.spring.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BeanDefinitionReader {

    //读取properties配置文件信息，这里读取 application.properties中配置的包扫描路径
    private  Properties config = new Properties();

    //读取的需注册的class路径存放在这个map中
    private  List<String> registerBeanClasses = new ArrayList<String>();

    //包扫描路径的KEY信息
    private final String PACKAGE_KEY = "scanPackage";



    public List<String> loadBeanDefinitions(){
        return this.registerBeanClasses;
    }

    public Properties getConfig(){
        return this.config;
    }


    //注册bean信息  注册成为 BeanDefinition
    public BeanDefinition registerBean(String className){
        if(registerBeanClasses.contains(className)){
            BeanDefinition beanDefinition =new BeanDefinition();
            beanDefinition.setBeanClassName(className);
            beanDefinition.setFactoryBeanName(className.substring(className.lastIndexOf(".")+1));
            return beanDefinition;
        }
        return null;
    }


    //构造器初始化时扫描配置信息中的class文件名
    public BeanDefinitionReader(String... configLocations) {
        for (String configLocation : configLocations) {
            //初始化config信息
            InputStream is = this.getClass().getClassLoader().getResourceAsStream(configLocation);
            try {
                config.load(is);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        String packages = config.getProperty(PACKAGE_KEY);

        doScanner(packages);

    }

    //根据配置的包信息 扫描配置路径下的包文件，并将包文件存储在 registerBeanClasses
    private void doScanner(String packages) {
        Assert.notNull(packages);
        URL url = this.getClass().getClassLoader().getResource(packages.replace(".", "/"));

        File files = new File(url.getFile());

        for (File file : files.listFiles()) {
            if (file.isDirectory()) {
                doScanner(packages + "." + file.getName());
            } else {
                registerBeanClasses.add(packages + "." + file.getName().replace(".class", ""));
            }

        }

    }
}
