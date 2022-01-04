package com.lagou.edu.factory;

import com.lagou.edu.factory.annnotation.Autowired;
import com.lagou.edu.factory.annnotation.Service;
import com.lagou.edu.utils.TransactionManager;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author 应癫
 *
 *
 * 代理对象工厂：生成代理对象的
 */

public class ProxyFactory implements ProxyFactoryInterface {

    private TransactionManager transactionManager;

    public void setTransactionManager(TransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    /*private ProxyFactory(){

    }

    private static ProxyFactory proxyFactory = new ProxyFactory();

    public static ProxyFactory getInstance() {
        return proxyFactory;
    }*/

    /**
     *  封装getProxy方法，判断使用正确的getProxy方式
     * @param obj
     * @return
     */
    @Override
    public Object getProxy(Object obj) {
        if(obj.getClass().getInterfaces().length == 0 )
            return getCglibProxy(obj);
        else
            return getJdkProxy(obj);
    }


    /**
     * Jdk动态代理
     * @param obj  委托对象
     * @return   代理对象
     */
    public Object getJdkProxy(Object obj) {

        // 获取代理对象
        return  Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;

                        try{
                            // 开启事务(关闭事务的自动提交)
                            transactionManager.beginTransaction();
                            System.out.println("jdk trans begin");

                            result = method.invoke(obj,args);

                            // 提交事务

                            transactionManager.commit();
                            System.out.println("jdk trans commit");
                        }catch (Exception e) {
                            e.printStackTrace();
                            // 回滚事务
                            transactionManager.rollback();
                            System.out.println("jdk trans rollback");

                            // 抛出异常便于上层servlet捕获
                            throw e;

                        }

                        return result;
                    }
                });

    }


    /**
     * 使用cglib动态代理生成代理对象
     * @param obj 委托对象
     * @return
     */
    public Object getCglibProxy(Object obj) {
        return  Enhancer.create(obj.getClass(), new MethodInterceptor() {
            @Override
            public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
                Object result = null;
                try{
                    // 开启事务(关闭事务的自动提交)
                    transactionManager.beginTransaction();
                    System.out.println("cglib trans begin");
                    result = method.invoke(obj,objects);

                    // 提交事务

                    transactionManager.commit();
                    System.out.println("cglib trans commit");

                }catch (Exception e) {
                    e.printStackTrace();
                    // 回滚事务
                    transactionManager.rollback();
                    System.out.println("cglib trans rollback");

                    // 抛出异常便于上层servlet捕获
                    throw e;

                }
                return result;
            }
        });
    }
}
