package com.boot.test;


import com.boot.invoker.facade.SpringInvokerSampleServiceFacade;
import com.boot.invoker.facade.SpringInvokerSampleServiceHystrix;
import com.boot.invoker.facade.event.HttpInvokerEvent;
import com.boot.invoker.facade.vo.TestVo;
import com.boot.invoker.sample.service.TestService;
import com.i360day.invoker.annotation.RemoteIgnoreSecurity;
import com.i360day.invoker.properties.InvokerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  13:26<p>
 **/
@Service
@RemoteIgnoreSecurity
public class SpringInvokerSampleService implements SpringInvokerSampleServiceFacade {
    @Autowired
    private TestService testService;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private InvokerProperties invokerProperties;

    public SpringInvokerSampleService() {
        System.out.println();
    }

//    @RemoteDisable
    public String testQuery(TestVo testVo){
        new SpringInvokerSampleServiceHystrix();
//        if(testVo == null || "admin".equals(testVo.getUserName())){
//            throw new IllegalStateException("不允许访问");
//        }
        return String.format("%s_%s, 密码正确", testVo.getUserName(), testVo.getPassword());
    }

    @Override
    public <T> List<TestVo> list(List<List<List<TestVo>>> testVo, Map<String, Map<String, Map<Object, Object>>> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, HttpStatusCode httpStatus) {
        return testVo.stream().flatMap(f -> f.stream().flatMap(b -> b.stream())).collect(Collectors.toList());
    }

    @Override
    public void testArrays(String[] strs, TestVo[] testVos, int number, int[] numbers) {

    }

    @Override
    public Object testNull(Object obj) {
        return null;
    }

    @Override
    public TestVo testVoid() {
        TestVo testVo = new TestVo();
        testVo.setUserName("ok");
        return testVo;
    }

    @Override
    public TestVo testByte(byte[] bytes1, Byte[] bytes2) {
        TestVo testVo = new TestVo();
        testVo.setUserName("ok");
        return testVo;
    }

    @Override
    public <T, O, E> T testSerialize(Class<? extends HttpInvokerEvent> clazz, TestVo testVo, Set<? extends Object> set, List<?>[] a, List<O> list, Map<E, ?> param, HttpStatusCode httpStatusCode, Enum<? extends HttpStatus> httpStatus) {
        testVo.setUserName(String.format("服务端返回：%s", testVo.getUserName()));
        testVo.setPassword(String.format("服务端返回：%s", testVo.getPassword()));
        return (T) testVo;
    }
}
