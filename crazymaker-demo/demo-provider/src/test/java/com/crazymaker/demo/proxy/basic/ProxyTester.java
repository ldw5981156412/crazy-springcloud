package com.crazymaker.demo.proxy.basic;

import com.alibaba.fastjson.JSONObject;
import com.crazymaker.demo.proxy.MockDemoClient;
import com.crazymaker.springcloud.common.result.RestOut;
import com.crazymaker.springcloud.seckill.remote.client.DemoClient;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import sun.misc.ProxyGenerator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * 静态代理和动态代理，测试用例
 */
@Slf4j
public class ProxyTester {

    /**
     * 不用代理，进行简单的远程调用
     */
    @Test
    public void simpleRPCTest() {
        /*
         * 简单的 PRC 调用类
         */
        MockDemoClient realObject = new RealRpcDemoClientImpl();

        /*
         * 调用   demo-provider 的  REST 接口  api/demo/hello/v1
         */
        RestOut<JSONObject> result1 = realObject.hello();
        log.info("result1={}", result1.toString());

        /*
         * 调用   demo-provider 的  REST 接口  api/demo/echo/{0}/v1
         */
        RestOut<JSONObject> result2 = realObject.echo("回显内容");
        log.info("result2={}", result2.toString());
    }

    /**
     * 静态代理测试
     */
    @Test
    public void staticProxyTest() {
        /*
         * 被代理的真实 PRC 调用类
         */
        MockDemoClient realObject = new RealRpcDemoClientImpl();

        /*
         *  静态的代理类
         */
        DemoClient proxy = new DemoClientStaticProxy(realObject);

        RestOut<JSONObject> result1 = proxy.hello();
        log.info("result1={}", result1.toString());

        RestOut<JSONObject> result2 = proxy.echo("回显内容");
        log.info("result2={}", result2.toString());
    }

    @Test
    public void dynamicProxyTest() throws IOException {
        /*
         * 被代理的真实 PRC 调用类
         */
        MockDemoClient realObject = new RealRpcDemoClientImpl();

        //参数1：类装载器
        ClassLoader classLoader = ProxyTester.class.getClassLoader();
        //参数2：代理类和委托类共同的抽象接口
        Class[] clazz = new Class[]{MockDemoClient.class};

        //参数3：动态代理的调用处理器
        InvocationHandler invocationHandler = new DemoClientInvocationHandler(realObject);
        /*
         * 使用以上三个参数，创建 JDK 动态代理类
         */
        MockDemoClient proxy = (MockDemoClient) Proxy.newProxyInstance(classLoader, clazz, invocationHandler);
        RestOut<JSONObject> result1 = proxy.hello();
        log.info("result1={}", result1.toString());
        RestOut<JSONObject> result2 = proxy.echo("回显内容");
        log.info("result2={}", result2.toString());

        /*
         * 将动态代理类的class字节码，保存在当前的工程目录下
         */
        byte[] classFile = ProxyGenerator.generateProxyClass("Proxy0", RealRpcDemoClientImpl.class.getInterfaces());
        /*
         * 输出到文件
         */
        FileOutputStream fos = new FileOutputStream("Proxy0.class");
        fos.write(classFile);
        fos.flush();
        fos.close();
    }
}
