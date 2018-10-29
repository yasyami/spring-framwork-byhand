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

        //定位
//        doLoadConfig(config.getInitParameter("contextConfigLocation"));
//
        //加载
//        doScanner(contextConfig.getProperty("scanPackage"));
//
        //注册
//        doRegister();
//
        //自动依赖注入
//        doAutowire();

//        super.init(config);
    }

    private void initStrategies(ApplicationContext context) {
        //有九种策略
        // 针对于每个用户请求，都会经过一些处理的策略之后，最终才能有结果输出
        // 每种策略可以自定义干预，但是最终的结果都是一致
        // ModelAndView

        // =============  这里说的就是传说中的九大组件 ================
        initMultipartResolver(context);//文件上传解析，如果请求类型是multipart将通过MultipartResolver进行文件上传解析
        initLocaleResolver(context);//本地化解析
        initThemeResolver(context);//主题解析

        /** 我们自己会实现 */
        //GPHandlerMapping 用来保存Controller中配置的RequestMapping和Method的一个对应关系
        initHandlerMappings(context);//通过HandlerMapping，将请求映射到处理器
        /** 我们自己会实现 */
        //HandlerAdapters 用来动态匹配Method参数，包括类转换，动态赋值
        initHandlerAdapters(context);//通过HandlerAdapter进行多类型的参数动态匹配

        initHandlerExceptionResolvers(context);//如果执行过程中遇到异常，将交给HandlerExceptionResolver来解析
        initRequestToViewNameTranslator(context);//直接解析请求到视图名

        /** 我们自己会实现 */
        //通过ViewResolvers实现动态模板的解析
        //自己解析一套模板语言
        initViewResolvers(context);//通过viewResolver解析逻辑视图到具体视图实现

        initFlashMapManager(context);//flash映射管理器

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
