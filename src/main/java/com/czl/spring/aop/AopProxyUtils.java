package com.czl.spring.aop;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

public class AopProxyUtils {



    public static Object getTargetObject(Object proxy) throws Exception {
        if(!isProxy(proxy)) return proxy;
        return  getProxyTargetObject(proxy);
    }

    private static boolean isProxy(Object o){
        return Proxy.isProxyClass(o.getClass());
    }

    private   static Object getProxyTargetObject(Object proxy) throws Exception {
        Field h = proxy.getClass().getSuperclass().getDeclaredField("h");
        h.setAccessible(true);
        AopProxy aopProxy=(AopProxy) h.get(proxy);
        Field target = aopProxy.getClass().getDeclaredField("target");
        target.setAccessible(true);
        return target.get(aopProxy);

    }


}
