package com.boot.invoker.sample.controller;

import cn.hutool.extra.spring.SpringUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boot.invoker.facade.RedisSpringInvokerSampleServiceFacade;
import com.boot.invoker.facade.vo.TestVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RRemoteService;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:09<p>
 **/
@RestController
@RequestMapping("/test")
public class TestController implements SmartInitializingSingleton {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RedisSpringInvokerSampleServiceFacade springInvokerSampleServiceFacade;

//    @Autowired
//    private HttpInvokerProperties httpInvokerProperties;

    @RequestMapping("/query")
    public String query(TestVo testVo1) throws InterruptedException {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");

//        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
//        RRemoteService remoteService = redissonClient.getRemoteService();
//        SpringInvokerSampleServiceFacade sampleServiceFacade = remoteService.get(SpringInvokerSampleServiceFacade.class);
//        String testQuery = sampleServiceFacade.testQuery(testVo);
//        return testQuery;
//
//        String result = springInvokerSampleServiceFacade.testQuery(testVo);

        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock("");
        RLock rLock = readWriteLock.writeLock();
        rLock.lock();
        RRemoteService remoteService = redissonClient.getRemoteService();
        RedisSpringInvokerSampleServiceFacade springInvokerSampleServiceFacade = remoteService.get(RedisSpringInvokerSampleServiceFacade.class);

        return springInvokerSampleServiceFacade.testQuery(testVo);
    }

    @JsonSerialize
    @RequestMapping("/list")
    public List<TestVo> list() throws InterruptedException {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");


        Map<String, TestVo> objectHashMap = new HashMap<>() {{
            put("test", testVo);
        }};

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(testVo));
        jsonObject.put("abc1", 123456);
        jsonObject.put("abc2", 1.1314156);
        jsonObject.put("abc3", 1.1314156D);
        List<TestVo> testVoList = springInvokerSampleServiceFacade.list(Arrays.asList(testVo), jsonObject, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);
        return testVoList;
    }

    @RequestMapping("/testArrays")
    public void testArrays(){
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");
        springInvokerSampleServiceFacade.testArrays(null, new TestVo[]{testVo}, 1, new int[]{0} );
    }

    @RequestMapping("/testVoid")
    public TestVo testVoid(){
        return springInvokerSampleServiceFacade.testVoid();
    }

    @RequestMapping("/testNull")
    public Object testNull(){
        springInvokerSampleServiceFacade.testNull(null);
        return "ok";
    }

    @RequestMapping("/testBytes")
    public Object testBytes(){
        return springInvokerSampleServiceFacade.testByte(new byte[]{1, 2, 3}, new Byte[]{1, 2, 3});
    }

    @Override
    public void afterSingletonsInstantiated() {
//        TestVo testVo = new TestVo();
//        testVo.setUserName("admin");
//        testVo.setPassword("123456");
//
//        Map<String, TestVo> objectHashMap = new HashMap<>() {{
//            put("test", testVo);
//        }};
//
//        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(testVo));
//        jsonObject.put("abc1", 123456);
//        jsonObject.put("abc2", 1.1314156);
//        jsonObject.put("abc3", 1.1314156D);
////        List<TestVo> list = this.springInvokerSampleServiceFacade.list(Arrays.asList(testVo), jsonObject, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);
//
//
//        RedissonClient redissonClient = SpringUtil.getBean(RedissonClient.class);
//        RRemoteService remoteService = redissonClient.getRemoteService();
//        SpringInvokerSampleServiceFacade springInvokerSampleServiceFacade = remoteService.get(SpringInvokerSampleServiceFacade.class);
//        springInvokerSampleServiceFacade.list(Arrays.asList(testVo), jsonObject, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);
    }

}
