package com.czl.spring.aop;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class AopConfig {

    //以目标对象需要增强的Method作为key，需要增强的代码内容作为value
    private Map<Method,Aspect> points =new HashMap<Method, Aspect>();


    public void put (Method target,Object aspect,Method[] points){
        this.points.put(target,new Aspect(aspect,points));
    }

    public Aspect get(Method m){
        return this.points.get(m);
    }

    public Boolean contains(Method m){
        return this.points.containsKey(m);
    }

    public class Aspect{

        private Object aspect ; //需要织入的对象

        private Method[] points; //需要织入的方法

        public Aspect(Object aspect, Method[] points) {
            this.aspect = aspect;
            this.points = points;
        }

        public Object getAspect() {
            return aspect;
        }

        public Method[] getPoints() {
            return points;
        }
    }



}
