package com.czl.spring.webmvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Map;

public class HandlerAdapter {

    private Map<String ,Integer> paramMapping ;

    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }

    public ModelAndView handle(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handler) throws InvocationTargetException, IllegalAccessException {
        //获得当前handler形参的构成及顺序
        Class<?>[] parameterTypes = handler.getMethod().getParameterTypes();

        //首先要获得用户传过来的参数
        Map<String,String[]> parameterMaps = req.getParameterMap();

        //构造实参列表
        Object[] paramValues =new Object[parameterTypes.length];

        for (Map.Entry<String,String[]> paramEntry:parameterMaps.entrySet() ){
            String value = Arrays.toString(paramEntry.getValue()).replaceAll("\\[|\\]","").replaceAll("\\s","");
            if(!paramMapping.containsKey(paramEntry.getKey())){
                continue;
            }
            //如果包含这个key的话就拿到这个key的索引
            Integer index = paramMapping.get(paramEntry.getKey());
            paramValues[index]=caseStringValue(value,parameterTypes[index]);
        }

        if(this.paramMapping.containsKey(HttpServletRequest.class.getName())) {
            int reqIndex = this.paramMapping.get(HttpServletRequest.class.getName());
            paramValues[reqIndex] = req;
        }

        if(this.paramMapping.containsKey(HttpServletResponse.class.getName())) {
            int respIndex = this.paramMapping.get(HttpServletResponse.class.getName());
            paramValues[respIndex] = resp;
        }
        //利用反射机制调用方法
        Object invoke = handler.getMethod().invoke(handler.getController(), paramValues);
        if(invoke==null) return null;
        boolean isModelAndView = handler.getMethod().getReturnType()==ModelAndView.class;
        if(isModelAndView){
            return (ModelAndView)invoke;
        }else {
            return null;
        }
    }

    private Object caseStringValue(String value,Class<?> clazz){
        if(clazz == String.class){
            return value;
        }else if(clazz == Integer.class){
            return  Integer.valueOf(value);
        }else if(clazz == int.class){
            return Integer.valueOf(value).intValue();
        }else {
            return null;
        }
    }
}
