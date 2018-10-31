package com.czl.spring.aop;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class AopProxy implements InvocationHandler{
    private AopConfig config;

    private Object target;

    public Object getProxy(Object instance){
        this.target=instance;
        Class<?> aClass = instance.getClass();
        return Proxy.newProxyInstance(aClass.getClassLoader(),aClass.getInterfaces(),this);
    }


    public void setConfig(AopConfig config) {
        this.config = config;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Method m = this.target.getClass().getMethod(method.getName(), method.getParameterTypes());

        //这里调用before方法
        if(this.config.contains(m)){
            AopConfig.Aspect aspect = this.config.get(m);
            aspect.getPoints()[0].invoke(aspect.getAspect());
        }

        //这里调用的是原生方法
        Object invoke = m.invoke(this.target, args);

        System.out.println(args);

        //这里调用的after方法
        if(this.config.contains(m)){
            AopConfig.Aspect aspect = this.config.get(m);
            aspect.getPoints()[1].invoke(aspect.getAspect());
        }

        return invoke;


    }
}
