package com.example.didemo;

import com.example.didemo.anno.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiDemoApplication {
    private static String pathRegex = "/|\\\\";  //路径分隔符
    private static String javaPathSeparator = ".";  //包分隔符
    private static String classPath = "com/example/didemo";  //包路径
    private static String classJavaPath = classPath.replaceAll(pathRegex,".");  //把路径分隔符替换成java的包分隔符 "."

    //String 是绝对path；list中，0是controller实例，1是method对象
    private static Map<String, List<Object>> methodMap = new HashMap<String, List<Object>>();

    //这里所有依赖都是单例，若需配置模式，比如改成每个依赖都创建单独的实例，修改代码，加个模式字段即可
    private static Map<Class, Object> diMap = new HashMap<Class, Object>();


    public static void main(String[] args) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        //扫描包下所有java文件，初始化service和controller
        File classPathFile = new File(".");
        ArrayList<String> classes = new ArrayList<String>();
        getAllClassJavaPath(classPathFile,classes);

        //遍历路径下的所有类，根据注解创建Controller、Service实例、和注入依赖
        List<Object> mayNeedInject = new ArrayList<Object>(); //存储需要注入依赖的类
        for(String c:classes) {
            Class<?> clazz = Class.forName(c);
            Controller controller = clazz.getAnnotation(Controller.class);
            if(controller!=null) {
                Object o = clazz.getConstructor().newInstance();
                mayNeedInject.add(o);
                //扫描方法，取出方法Method对象和对应的路径名 TODO
                for (Method method : clazz.getMethods()) {
                    Path p =method.getAnnotation(Path.class);
                    if(p!=null) {
                        methodMap.put(controller.value()+p.value(), new ArrayList<Object>(){
                            {
                                add(o);
                                add(method);
                                //TODO 解析方法参数
                            }
                        });
                    }
                }
                continue;
            }
            Service service = clazz.getAnnotation(Service.class);
            if(service!=null) {
                Object o = clazz.getConstructor().newInstance();
                diMap.put(clazz,o);  //添加到依赖集合
                mayNeedInject.add(o);
                continue;
            }
            Configure conf = clazz.getAnnotation(Configure.class);
            if(conf!=null) {
                Object o = clazz.getConstructor().newInstance();
                //遍历方法，储存bean
                for (Method method : o.getClass().getMethods()) {
                    if(method.getAnnotation(Bean.class)!=null) {
                        Class<?> returnType = method.getReturnType();
                        Object ret = method.invoke(o);
                        diMap.put(returnType, ret);
                    }
                }
            }
            /*            //另一种方式取出注解，遍历注解，检查类型，然后强转
            for (Annotation clazzAnnotation : clazz.getAnnotations()) {
//                System.out.println(clazzAnnotation.annotationType().toString());
                if(clazzAnnotation instanceof Controller) {
                    Controller cc = (Controller) clazzAnnotation;
                    System.out.println(cc.value());
                }
            }*/

        }
        for(Object o:mayNeedInject) {
            for (Field field : o.getClass().getDeclaredFields()) {
                if(field.getAnnotation(Autowired.class) != null) {
                    Class<?> fieldType = field.getType();
                    Object di = diMap.get(fieldType);
                    //如果获取依赖失败，尝试获取其父类和接口的依赖
                    if(di==null) {
                        //检查依赖中是否有字段类型的子类，如果有，将子类赋值给父类
                        for(Object diObjKey:diMap.keySet()) {
                            Object diObjVal = diMap.get(diObjKey);
                            //取出实例的所有父类，检查是否和所需的依赖类型相同
                            for(Class parentType:diObjVal.getClass().getInterfaces()) {
                                if(fieldType == parentType) {
                                    di = diObjVal;
                                    if(di!=null) {
                                        break;
                                    }
                                }
                            }
                        }
                    }

                    if(di == null) {
                        throw new RuntimeException("错误：类"+o.getClass()+"缺少依赖");
                    }else {
                        //注入依赖，这里全是单例，若想每次注入都创建不同实例，可添加一个代表注入模式的选项，并利用反射创建实例，若每个实例都有相同的默认状态，可以创建一个模版，其他的直接拷贝它的属性。
                        //Object newDi = di.getClass().getConstructor().newInstance();
                        // set new Di fields

                        //设置字段值
                        field.setAccessible(true);
                        field.set(o, di);
                    }
                }
            }

        }
        System.out.println(1);

        //根据路径调用某一controller下的方法
        String urlPath = "/demo/method1";
        List<Object> controllerAndMethod = methodMap.get(urlPath);
        if(controllerAndMethod==null) {
            System.out.println("urlPath:"+urlPath+" no matched controller");
        }else {
            //调用url对应的方法
            Object controller = controllerAndMethod.get(0);
            Object method = controllerAndMethod.get(1);
            Object ret = ((Method)method).invoke(controller);
            System.out.println(ret);
        }

    }

    /**
     * 返回包名集合，例如 {com.abc.Class, com.abc.Class2}
     * @param classPathFile 要扫描的路径
     * @param allClassPath 存储路径的集合
     * @return 返回路径下java类集合字符串
     */
    public static void getAllClassJavaPath(File classPathFile, List<String> allClassPath) {
        if(classPathFile==null){
            return;
        }
        for(File f:classPathFile.listFiles()) {
            if (f.isDirectory()) {
                getAllClassJavaPath(f, allClassPath);
            }else {
                if(f.getName().endsWith(".java")) {
                    try {
                        String canonicalPath = f.getCanonicalPath();
                        String path = canonicalPath.replaceAll(pathRegex, ".");
                        allClassPath.add(path.substring(path.indexOf(classJavaPath), path.length()-5));  //去掉末尾的 ".java"
                    }catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
