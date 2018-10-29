package com.czl.spring.context;

public abstract class AbstactApplicationContext {


    //提供给子类重写
    protected void onRefresh(){
        // For subclasses: do nothing by default.
    }

    protected abstract void refreshBeanFactory();
}
