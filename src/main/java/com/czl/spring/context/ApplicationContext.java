package com.czl.spring.context;

import com.czl.spring.annotation.Autowired;
import com.czl.spring.annotation.Controller;
import com.czl.spring.annotation.Service;
import com.czl.spring.aop.AopConfig;
import com.czl.spring.aop.AopProxyUtils;
import com.czl.spring.beans.BeanDefinition;
import com.czl.spring.beans.BeanPostProcessor;
import com.czl.spring.beans.BeanWrapper;
import com.czl.spring.context.support.BeanDefinitionReader;
import com.czl.spring.core.BeanFactory;
import com.czl.spring.util.Assert;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ApplicationContext extends DefaultListableBeanFactory implements BeanFactory {

    private String[] configLocations;

    private BeanDefinitionReader reader;

    private Map<String, Object> beanCacheMap = new HashMap<String, Object>();

    private Map<String, BeanWrapper> beanWrapperMap = new HashMap<String, BeanWrapper>();

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
        doAutoWired();

    }

    private void doAutoWired() {
        for (Map.Entry<String, BeanDefinition> entry : beanDefinitionMap.entrySet()) {
            Object wrapperBean = getBean(entry.getKey());
            try {
                populateBean(AopProxyUtils.getTargetObject(wrapperBean));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void populateBean(Object wrapperBean) {
        try {
            Class clazz = wrapperBean.getClass();
            if (!(clazz.isAnnotationPresent(Controller.class)
                    || clazz.isAnnotationPresent(Service.class))) {
                return;
            }
            Field[] fields = clazz.getDeclaredFields();

            for (Field field : fields) {
                Autowired annotation = field.getAnnotation(Autowired.class);
                boolean required = annotation.required();
                if (required) {
                    String name = field.getType().getName();
                    BeanWrapper beanWrapper = beanWrapperMap.get(name);
                    field.setAccessible(true);
                    field.set(wrapperBean, beanWrapper == null ? null : beanWrapper.getWrapperInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doRegister(List<String> beanDefinitions) {

        if (null == beanDefinitions || beanDefinitions.isEmpty()) {
            return;
        }
        try {
            for (String className : beanDefinitions) {
                Class<?> beanClass = Class.forName(className);
                if (beanClass.isInterface()) continue;
                BeanDefinition beanDefinition = reader.registerBean(beanClass.getName());
                if (null != beanDefinition)
                    beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
                Class<?>[] interfaces = beanClass.getInterfaces();
                for (Class<?> clazz : interfaces) {
                    beanDefinitionMap.put(clazz.getName(), beanDefinition);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public Object getBean(String beanName) {
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        try {
            String beanClassName = beanDefinition.getBeanClassName();
            Class<?> clazz = Class.forName(beanClassName);
            Field[] fields = clazz.getDeclaredFields();
            for (Field field:fields){
                if (!field.isAnnotationPresent(Autowired.class)){
                    continue;
                }
                boolean anInterface = field.getType().isInterface();

                String fieldName;
                if(anInterface){
                    fieldName =field.getType().getName();
                }else {
                    fieldName =firstToLowerCase(field.getType().getSimpleName());
                }
                getBean(fieldName);
            }
            BeanPostProcessor beanPostProcessor = new BeanPostProcessor();
            Object instance = instanceBean(beanDefinition);
            if (null == instance) return null;
            beanPostProcessor.postProcessAfterInitialization(instance, beanName);
            BeanWrapper beanWrapper = new BeanWrapper(instance);
            beanWrapper.setBeanPostProcessor(beanPostProcessor);
            beanWrapper.setAopConfig(instanceAopConfig(beanDefinition));
            this.beanWrapperMap.put(beanName, beanWrapper);
            beanPostProcessor.postProcessBeforeInitialization(instance, beanName);
            return this.beanWrapperMap.get(beanName).getWrapperInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private AopConfig instanceAopConfig(BeanDefinition beanDefinition) throws Exception {
        AopConfig config = new AopConfig();
        String expression = reader.getConfig().getProperty("pointCut");
        String[] before = reader.getConfig().getProperty("aspectBefore").split("\\s");
        String[] after = reader.getConfig().getProperty("aspectAfter").split("\\s");
        String beanClassName = beanDefinition.getBeanClassName();
        Class<?> aClass = Class.forName(beanClassName);
        Class<?> aspectClass = Class.forName(before[0]);
        Pattern pattern =Pattern.compile(expression);
        for (Method method:aClass.getMethods()){
            Matcher matcher =pattern.matcher(method.toString());
            if (matcher.matches()) {
               config.put(method,aspectClass.newInstance(),
                       new Method[]{aspectClass.getMethod(before[1]),aspectClass.getMethod(after[1])});
            }
        }

        return config;
    }

    private Object instanceBean(BeanDefinition beanDefinition) {
        String beanClassName = beanDefinition.getBeanClassName();
        Object instance = null;
        try {
            if (beanCacheMap.containsKey(beanClassName)) {
                instance = this.beanCacheMap.get(beanClassName);
            } else {
                Class<?> clazz = Class.forName(beanClassName);
                instance = clazz.newInstance();
                this.beanCacheMap.put(clazz.getName(), instance);
            }
            return instance;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //将beanDefinitions 注册值beanDefinitionMaps

    private String firstToLowerCase(String str){
        char[] chars = str.toCharArray();
        chars[0]+=32;
        return new String(chars);
    }

    public String[] getBeanDefinitionNames(){
        return  this.beanDefinitionMap.keySet().toArray(new String[this.beanDefinitionMap.size()]);
    }

    public Properties getConfig(){
      return this.reader.getConfig();
    }

}
