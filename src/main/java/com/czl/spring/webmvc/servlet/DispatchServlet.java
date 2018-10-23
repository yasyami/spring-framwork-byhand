package com.czl.spring.webmvc.servlet;

import com.czl.demo.mvc.action.DemoAction;
import com.czl.spring.annotation.Autowired;
import com.czl.spring.annotation.Controller;
import com.czl.spring.annotation.Service;
import com.czl.spring.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class DispatchServlet extends HttpServlet {

    private final String LOCATION ="contextConfigLocation";

    //配置文件的properties
    private Properties contextConfig = new Properties();
    //存储实例 ioc容器
    private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();
    //存储beanName
    private List<String> classNames = new ArrayList<String>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("----------------调用doPost-----------------");
        DemoAction action = (DemoAction)beanMap.get("demoAction");
        resp.getWriter().write( action.query(null,null,"czl"));

        //super.doPost(req, resp);
    }
    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化IOC容器  spring中 ioc和mvc容器是单独初始化的，tom老师说的是因为 mvc有j2ee的依赖
        //我个人觉得是不是因为 spring ioc容器有单独使用的场景 所以 spring设计成可以单独初始化ioc容器
        ApplicationContext context = new ApplicationContext(config.getInitParameter(LOCATION));

        //初始化mvc
        initStrategies(context);

//        //定位
//        doLoadConfig(config.getInitParameter("contextConfigLocation"));
//
//        //加载
//        doScanner(contextConfig.getProperty("scanPackage"));
//
//        //注册
//        doRegister();
//
//        //自动依赖注入
//        doAutowire();

//        super.init(config);
    }

    private void initStrategies(ApplicationContext context) {
        initMultipartResolver(context);
        initLocaleResolver(context);
        initThemeResolver(context);
        initHandlerMappings(context);
        initHandlerAdapters(context);
        initHandlerExceptionResolvers(context);
        initRequestToViewNameTranslator(context);
        initViewResolvers(context);
        initFlashMapManager(context);

    }

    private void initFlashMapManager(ApplicationContext context) {
    }

    private void initViewResolvers(ApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
    }

    private void initHandlerAdapters(ApplicationContext context) {
    }

    private void initHandlerMappings(ApplicationContext context) {
    }

    private void initThemeResolver(ApplicationContext context) {
    }

    private void initLocaleResolver(ApplicationContext context) {
    }

    private void initMultipartResolver(ApplicationContext context) {
    }

    private void doAutowire() {

        if (beanMap.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for (Field field : fields) {
                if (!field.isAnnotationPresent(Autowired.class)) {
                    continue;
                }

                Autowired autowired = field.getAnnotation(Autowired.class);

                boolean required = autowired.required();
                if (required) {
                    String name = toLowerCase(field.getType().getSimpleName());
                    Object o = beanMap.get(name);
                    field.setAccessible(true);
                    try {
                        field.set(entry.getValue(),o);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    }

    private void doRegister() {
        if (classNames.isEmpty()) {
            return;
        }
        for (String className : classNames) {
            try {
                Class<?> clazz = Class.forName(className);
                if (clazz.isAnnotationPresent(Controller.class)) {
                    String beanName = toLowerCase(clazz.getSimpleName());
                    try {
                        beanMap.put(beanName, clazz.newInstance());
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                } else if (clazz.isAnnotationPresent(Service.class)) {
                    Service service = clazz.getAnnotation(Service.class);
                    String beanName = service.value();
                    if ("".equals(beanName)) {
                        beanName = toLowerCase(clazz.getSimpleName());
                    }
                    Object instance = null;

                    try {
                        instance = clazz.newInstance();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    beanMap.put(beanName, instance);
                    Class<?>[] interfaces = clazz.getInterfaces();
                    for (Class<?> c : interfaces) {
                        beanMap.put(toLowerCase(c.getSimpleName()), instance);
                    }
                } else {
                    continue;
                }
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private void doScanner(String pakageName) {

        URL resource = this.getClass().getResource("/" + pakageName.replace(".", "/"));
        File file = new File(resource.getFile());

        for (File file1 : file.listFiles()) {
            if (file1.isDirectory()) {
                doScanner(pakageName + "." + file1.getName());
            } else {
                classNames.add(pakageName + "." + file1.getName().replace(".class", ""));
            }
        }


    }

    private void doLoadConfig(String location) {
        InputStream inputStream = this.getClass().getClassLoader().
                getResourceAsStream(location.replace("classpath:", ""));

        try {
            contextConfig.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String toLowerCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

}
