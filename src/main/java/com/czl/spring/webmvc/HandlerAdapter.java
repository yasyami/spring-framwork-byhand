package com.czl.spring.webmvc;

import java.util.Map;

public class HandlerAdapter {

    private Map<String ,Integer> paramMapping ;

    public HandlerAdapter(Map<String, Integer> paramMapping) {
        this.paramMapping = paramMapping;
    }
}
