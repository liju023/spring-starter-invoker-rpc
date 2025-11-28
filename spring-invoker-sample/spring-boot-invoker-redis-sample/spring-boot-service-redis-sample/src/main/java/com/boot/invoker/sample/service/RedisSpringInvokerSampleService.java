package com.boot.invoker.sample.service;


import com.boot.invoker.facade.RedisSpringInvokerSampleServiceFacade;
import com.boot.invoker.facade.RedisSpringInvokerSampleServiceHystrix;
import com.boot.invoker.facade.SpringBootInvokerSampleServiceFacade;
import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.properties.InvokerProperties;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  13:26<p>
 **/
@Service
public class RedisSpringInvokerSampleService implements RedisSpringInvokerSampleServiceFacade, SpringBootInvokerSampleServiceFacade, SmartInitializingSingleton {
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private InvokerProperties invokerProperties;

    public String testQuery(TestVo testVo) {
        new RedisSpringInvokerSampleServiceHystrix();
//        if(testVo == null || "admin".equals(testVo.getUserName())){
//            throw new IllegalStateException("不允许访问");
//        }
        return String.format("%s_%s, 密码正确", testVo.getUserName(), testVo.getPassword());
    }

    @Override
    public <T> List<TestVo> list(List<TestVo> testVo, Map<String, Object> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, HttpStatus httpStatus) {
        return testVo;
    }

    @Override
    public void testArrays(String[] strs, TestVo[] testVos, int number, int[] numbers) {

    }

    @Override
    public void testNull(Object obj) {

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
    public void afterSingletonsInstantiated() {
//        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
//        RRemoteService remoteService = redissonClient.getRemoteService();
//        remoteService.register(RedisSpringInvokerSampleServiceFacade.class, this);
    }
}
