package com.lagou.edu.factory;

import com.alibaba.druid.util.StringUtils;
import com.lagou.edu.factory.annnotation.Autowired;
import com.lagou.edu.factory.annnotation.Service;
import com.lagou.edu.factory.annnotation.Transactional;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.reflections.Reflections;

import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import static org.reflections.scanners.Scanners.*;

/**
 * @author 应癫
 * <p>
 * 工厂类，生产对象（使用反射技术）
 */

public class BeanFactory {

    /**
     * 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
     * 任务二：对外提供获取实例对象的接口（根据id获取）
     */

    private static Map<String, Object> map = new HashMap<>();
    private static List<AOPBeanInfo> beansWaitForAop = new ArrayList<>();

    private static ProxyFactoryInterface proxyFactory;
    private static ProxyInfo proxyInfo;

    public void setProxyFactory(ProxyFactoryInterface proxyFactory) {
        this.proxyFactory = proxyFactory;
    }

    private static final List<InjectionInfo> injectList = new ArrayList<>();

    static {
        // 任务一：读取解析xml，通过反射技术实例化对象并且存储待用（map集合）
        // 加载xml
        InputStream resourceAsStream = BeanFactory.class.getClassLoader().getResourceAsStream("beans.xml");
        // 解析xml
        SAXReader saxReader = new SAXReader();
        try {
            Document document = saxReader.read(resourceAsStream);
            Element rootElement = document.getRootElement();

            List<Element> list = rootElement.selectNodes("//componentScan");
            if(list.size() > 0) {
                Element scanPackageList = list.get(0);
                String scanPackage = scanPackageList.attributeValue("value");
                Reflections reflections = new Reflections(scanPackage);

                Set<Class<?>> serviceAnnotated =
                        reflections.get(SubTypes.of(TypesAnnotated.with(Service.class)).asClass());
//                beansWaitForAop = reflections.get(SubTypes.of(TypesAnnotated.with(Transactional.class)).asClass());
                for (Class<?> aClass : serviceAnnotated) {
                    Service annotation = aClass.getAnnotation(Service.class);

                    String id = annotation.value();
                    if(StringUtils.isEmpty(id)){
                        id = generateID(aClass.getCanonicalName());
                    }

                    System.out.println(aClass.getCanonicalName());
                    if(aClass.isAnnotationPresent(Transactional.class)){
                        beansWaitForAop.add(new AOPBeanInfo(id));
                    }
                    Field[] declaredFields = aClass.getDeclaredFields();
                    for (Field declaredField : declaredFields) {
                        System.out.println(declaredField.getName());
                        if(containAnnotation(declaredField,Autowired.class)){
                            injectList.add(new InjectionInfo(id,declaredField,declaredField.getAnnotation(Autowired.class)));
                        }
                    }
                    Object o = aClass.newInstance();
                    map.put(id, o);
                }
//                System.out.println(injectList);
                System.out.println(map);

            }

            for (InjectionInfo injectionInfo : injectList) {
                Autowired autowired = (Autowired) injectionInfo.getAnnotation();
                String value = autowired.value();
                Object ref = null;
                if(StringUtils.isEmpty(value))
                    ref = getBean(injectionInfo.getField().getType());
                else
                    ref = getBean(value);
                Object parentObject = getBean(injectionInfo.getId());
                String fieldName = injectionInfo.getField().getName();
                Method[] methods = parentObject.getClass().getMethods();
                for (int j = 0; j < methods.length; j++) {
                    Method method = methods[j];
                    if (method.getName().equalsIgnoreCase("set" + fieldName)) {
                        method.invoke(parentObject, ref);
                    }
                }
                map.put(injectionInfo.getId(), parentObject);
            }
            // 实例化完成之后维护对象的依赖关系，检查哪些对象需要传值进入，根据它的配置，我们传入相应的值
            // 有property子元素的bean就有传值需求
            List<Element> annotationList = rootElement.selectNodes("//annotation-transcationManager");
            if(annotationList.size() > 0) {
                Element aopInfo = annotationList.get(0);
                String factory = aopInfo.attributeValue("factory");
                String property = aopInfo.attributeValue("property");
                String ref = aopInfo.attributeValue("ref");
                Class f = Class.forName(factory);
                proxyInfo = new ProxyInfo(factory, f, property, ref);
                buildProxyFactory();
                if(beansWaitForAop.size() > 0) {
                    for (AOPBeanInfo aopBeanInfo : beansWaitForAop) {
                        Object o = getBean(aopBeanInfo.getId());
                        Object proxyO = proxyFactory.getProxy(o);
                        map.put(aopBeanInfo.getId(),proxyO);
                    }
                }
            }

        } catch (DocumentException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

    }

    private static void buildProxyFactory() throws InstantiationException, IllegalAccessException, InvocationTargetException {
        proxyFactory = (ProxyFactoryInterface) proxyInfo.getProxyClass().newInstance();
        Object o  = getBean(proxyInfo.getRef());
        Method[] methods = proxyFactory.getClass().getMethods();
        for (int j = 0; j < methods.length; j++) {
            Method method = methods[j];
            if (method.getName().equalsIgnoreCase("set" + proxyInfo.getPropertyName())) {
                method.invoke(proxyFactory, o);
            }
        }

    }

    private static String generateID(String canonicalName) {
        int i = canonicalName.lastIndexOf('.');
        return canonicalName.substring(i+1);
    }

    private static boolean containAnnotation(Field declaredField, Class<Autowired> autowiredClass) {
       return declaredField.isAnnotationPresent(autowiredClass);
    }

    // 任务二：对外提供获取实例对象的接口（根据id获取）
    public static Object getBean(String id) {
        return map.get(id);
    }

    

    // Autowired getBean by Field Type
    public static Object getBean(Class aclass) {
        for(Map.Entry<String,Object> entry: map.entrySet()) {
            if(aclass.isInstance(entry.getValue()))
                return entry.getValue();
        }
        return null;
    }
}
