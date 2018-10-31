package com.czl.spring.webmvc;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class HandlerMapping {

    private Object controller;//url对应的Controller
    private Method method;//url对应的method
    private Pattern pattern;//url的封装

    public HandlerMapping(Object controller, Method method, Pattern pattern) {
        this.controller = controller;
        this.method = method;
        this.pattern = pattern;
    }

    public Object getController() {
        return controller;
    }

    public void setController(Object controller) {
        this.controller = controller;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public void setPattern(Pattern pattern) {
        this.pattern = pattern;
    }
}
