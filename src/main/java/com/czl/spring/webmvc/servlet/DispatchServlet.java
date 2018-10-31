package com.czl.spring.webmvc.servlet;

import com.czl.demo.mvc.action.DemoAction;
import com.czl.spring.annotation.*;
import com.czl.spring.context.ApplicationContext;
import com.czl.spring.webmvc.HandlerAdapter;
import com.czl.spring.webmvc.HandlerMapping;
import com.czl.spring.webmvc.ModelAndView;
import com.czl.spring.webmvc.ViewResolver;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DispatchServlet extends HttpServlet {

    private final String LOCATION ="contextConfigLocation";

    //配置文件的properties
    private Properties contextConfig = new Properties();
    //存储实例 ioc容器
    private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();
    //存储beanName
    private List<String> classNames = new ArrayList<String>();


    //存储handlerMappings
    private List<HandlerMapping> handlerMappings= new ArrayList<HandlerMapping>();

    //存储handlerMapping和handlerAdapter的映射关系
    private Map<HandlerMapping,HandlerAdapter> handlerAdapters = new HashMap<HandlerMapping, HandlerAdapter>();

    //存储本地视图文件
    private List<ViewResolver> viewResolvers = new ArrayList<ViewResolver>();


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("----------------调用doPost-----------------");
        //调用路由机制
        try {
            doDispatch(req,resp);
        }  catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //首先根据用户的请求地址获取一个handlerMapping
        HandlerMapping handler = getHandler(req);

        //根据这个handler获取其方法上的参数的 handlerAdapter

        HandlerAdapter adapter=getHandlerAdapter(handler);

        //获取到adapter之后 相当于 有了 handlerMapping中的  方法所在的对象(controller)
        //方法的名称，以及其对应handlerAdapter中方法的参数
        //下面开始调用这个方法
        //在adapter中处理方法的调用以及返回
        ModelAndView modelAndView =adapter.handle(req,resp,handler);

        //将方法的返回值  modelAndView 交给视图解析器解析 并用resp写回
        processDispatchResult(resp, modelAndView);

    }

    private void processDispatchResult(HttpServletResponse resp, ModelAndView modelAndView) throws Exception {

        if(modelAndView==null) return;

        if(this.viewResolvers.isEmpty()) return;

        //循环本地视图缓存 找到视图 并交给视图解析方法解析 返回的String结果 交给 response 写回的页面

        for (ViewResolver viewResolver:viewResolvers){
            if(!modelAndView.getViewName().equals(viewResolver.getViewName())){continue; }

            String out = viewResolver.resolver(modelAndView);

            if(out!=null){
                resp.getWriter().write(out);
                break;
            }
        }

    }

    private HandlerAdapter getHandlerAdapter(HandlerMapping handler) {
        if(this.handlerAdapters.isEmpty()){
            return null;
        }
        return handlerAdapters.get(handler);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        if(this.handlerMappings.isEmpty()){
            return null;
        }

        String requestURI = req.getRequestURI();
        String contextPath = req.getContextPath();
        requestURI= requestURI.replace(contextPath,"").replaceAll("/+","/");
        for(HandlerMapping handlerMapping:this.handlerMappings){
            Matcher matcher =handlerMapping.getPattern().matcher(requestURI);
            if(matcher.matches()){
                return handlerMapping;
            }
        }
        return null;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //初始化IOC容器  spring中 ioc和mvc容器是单独初始化的，tom老师说的是因为 mvc有j2ee的依赖
        //我个人觉得是不是因为 spring ioc容器有单独使用的场景 所以 spring设计成可以单独初始化ioc容器
        ApplicationContext context = new ApplicationContext(config.getInitParameter(LOCATION));
        //初始化mvc
        initStrategies(context);

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

    //这里初始化本地的视图文件，将本地视图文件放入缓存
    private void initViewResolvers(ApplicationContext context) {
        //获取配置文件信息
        String templateRoot = context.getConfig().getProperty("templateRoot");
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File viewFolder =new File(templateRootPath);
        for(File file:viewFolder.listFiles()){
            viewResolvers.add(new ViewResolver(file.getName(),file));
        }
    }

    //初始化handlerMapping和其方法参数的映射关系
    private void initHandlerAdapters(ApplicationContext context) {
        for (HandlerMapping handlerMapping:handlerMappings){
            Method method = handlerMapping.getMethod();
            Map<String,Integer> paramsMaps = new HashMap<String, Integer>();
            //这里循环出命名参数
            Annotation[][] parameterAnnotations = method.getParameterAnnotations();
            for (int i =0 ;i<parameterAnnotations.length;i++){
                for (Annotation a :parameterAnnotations[i]){
                    if(a instanceof RequestParam){
                        String value = ((RequestParam) a).value();
                        if(!"".equals(value)){
                              paramsMaps.put(value,i);
                        }
                    }
                }
            }
            //还需要取出非命名参数,这里只处理了 HttpServletRequest 和HttpServletResponse
            Class<?>[] parameterTypes = method.getParameterTypes();
            for (int i = 0; i < parameterTypes.length; i++) {
                Class<?> clazz = parameterTypes[i];
                if(clazz==HttpServletRequest.class||
                        clazz==HttpServletResponse.class){
                    paramsMaps.put(clazz.getName(),i);
                }

            }

            //最后存储 handlerMapping和handlerAdapter的映射关系
            handlerAdapters.put(handlerMapping,new HandlerAdapter(paramsMaps));
        }

    }

    //初始化handlerMappings，保存地址与方法的映射关系
    private void initHandlerMappings(ApplicationContext context) {

        //这里获取BeanDefinitionMap中的key 用于获取bean的实例
        String[] definitionNames = context.getBeanDefinitionNames();
        for(String beanName:definitionNames){
            Object bean = context.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            //这里只获取controller
            if(!beanClass.isAnnotationPresent(Controller.class)){
                continue;
            }
            //获取controller上requestMapping注解上的值
            String baseUrl="";
            if(beanClass.isAnnotationPresent(RequestMapping.class)){

                RequestMapping requestMapping = beanClass.getAnnotation(RequestMapping.class);
                baseUrl= requestMapping.value();
            }
            //获取方法上RequestMapping的值
            Method[] methods = beanClass.getMethods();
            for(Method method:methods){
                if(method.isAnnotationPresent(RequestMapping.class)){
                    RequestMapping annotation = method.getAnnotation(RequestMapping.class);
                    //这里用正则的方式将controller路径和method路径组合
                    String regex = ("/"+baseUrl+annotation.value().replaceAll("\\*",".*"))
                            .replaceAll("/+","/");
                    Pattern pattern =Pattern.compile(regex);
                    //这里实例化一个handlerMapping
                    handlerMappings.add(new HandlerMapping(bean,method,pattern));
                    System.out.println(regex);
                }
            }


        }

    }

    private void initFlashMapManager(ApplicationContext context) {
    }

    private void initRequestToViewNameTranslator(ApplicationContext context) {
    }

    private void initHandlerExceptionResolvers(ApplicationContext context) {
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

//    private void doRegister() {
//        if (classNames.isEmpty()) {
//            return;
//        }
//        for (String className : classNames) {
//            try {
//                Class<?> clazz = Class.forName(className);
//                if (clazz.isAnnotationPresent(Controller.class)) {
//                    String beanName = toLowerCase(clazz.getSimpleName());
//                    try {
//                        beanMap.put(beanName, clazz.newInstance());
//                    } catch (InstantiationException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                } else if (clazz.isAnnotationPresent(Service.class)) {
//                    Service service = clazz.getAnnotation(Service.class);
//                    String beanName =service.value();
//                    if ("".equals(beanName)) {
//                        beanName = toLowerCase(clazz.getSimpleName());
//                    }
//                    Object instance = null;
//
//                    try {
//                        instance = clazz.newInstance();
//                    } catch (InstantiationException e) {
//                        e.printStackTrace();
//                    } catch (IllegalAccessException e) {
//                        e.printStackTrace();
//                    }
//                    beanMap.put(beanName, instance);
//                    Class<?>[] interfaces = clazz.getInterfaces();
//                    for (Class<?> c : interfaces) {
//                        beanMap.put(toLowerCase(c.getSimpleName()), instance);
//                    }
//                } else {
//                    continue;
//                }
//            } catch (ClassNotFoundException e) {
//                e.printStackTrace();
//            }
//        }
//    }

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
