package com.czl.spring.webmvc;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViewResolver {

    private String viewName;
    private File templateFile;

    public ViewResolver(String viewName, File templateFile) {
        this.viewName = viewName;
        this.templateFile = templateFile;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public File getTemplateFile() {
        return templateFile;
    }

    public void setTemplateFile(File templateFile) {
        this.templateFile = templateFile;
    }

    public String resolver(ModelAndView mv) throws Exception {
        StringBuffer sb = new StringBuffer();

        RandomAccessFile ra = new RandomAccessFile(this.templateFile,"r");

        try {
            String line = null;
            while (null != (line = ra.readLine())) {
                line = new String(line.getBytes("ISO-8859-1"), "utf-8");
                Matcher m = matcher(line);
                while (m.find()) {
                    for (int i = 1; i <= m.groupCount(); i++) {

                        //要把￥{}中间的这个字符串给取出来
                        String paramName = m.group(i);
                        Object paramValue = mv.getModel().get(paramName);
                        if (null == paramValue) {
                            continue;
                        }
                        line = line.replaceAll("￥\\{" + paramName + "\\}", paramValue.toString());
                        line = new String(line.getBytes("utf-8"), "ISO-8859-1");
                    }
                }
                sb.append(line);
            }
        }finally {
            ra.close();
        }

        return sb.toString();

    }

    private Matcher matcher(String str){
        Pattern pattern = Pattern.compile("￥\\{(.+?)\\}",Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(str);
        return  matcher;
    }
}
