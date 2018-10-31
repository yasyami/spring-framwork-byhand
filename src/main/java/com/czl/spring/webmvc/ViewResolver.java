package com.czl.spring.webmvc;

import java.io.File;

public class ViewResolver {

    private String viewName;
    private File templateFile;

    public ViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }
}
