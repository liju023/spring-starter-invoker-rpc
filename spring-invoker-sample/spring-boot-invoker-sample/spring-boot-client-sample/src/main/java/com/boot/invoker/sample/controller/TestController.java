package com.boot.invoker.sample.controller;

import cn.hutool.core.map.MapUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boot.invoker.facade.SpringInvokerSampleServiceFacade;
import com.boot.invoker.facade.event.HttpInvokerEvent;
import com.boot.invoker.facade.vo.TestVo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
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
    private SpringInvokerSampleServiceFacade springInvokerSampleServiceFacade;

//    @Autowired
//    private HttpInvokerProperties httpInvokerProperties;

    @RequestMapping("/testSerialize")
    public TestVo testSerialize() {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");

        TestVo test = springInvokerSampleServiceFacade.<TestVo, Object, String>testSerialize(
                HttpInvokerEvent.class,
                testVo,
                Arrays.asList(new HttpInvokerEvent(Arrays.asList(testVo))).stream().collect(Collectors.toSet()),
                new List[]{Arrays.asList(1, 2, 3)},
                Arrays.asList(new HttpInvokerEvent(Arrays.asList(testVo))),
                MapUtil.builder("test", testVo).map(),
                HttpStatus.OK,
                HttpStatus.OK
        );
        return test;
    }

    @RequestMapping("/query")
    public String query(TestVo testVo1) throws InterruptedException {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");
        return springInvokerSampleServiceFacade.testQuery(testVo);
    }

    @JsonSerialize
    @RequestMapping("/list")
    public List<TestVo> list() throws InterruptedException {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");


        Map<String, TestVo> objectHashMap =  MapUtil.builder("test", testVo).map();

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(testVo));
        jsonObject.put("abc1", 123456);
        jsonObject.put("abc2", 1.1314156);
        jsonObject.put("abc3", 1.1314156D);

        Map jsonObject1 = new HashMap<>();
        jsonObject1.put("test", jsonObject);

        Map jsonObject2 = new HashMap<>();
        jsonObject2.put("test", jsonObject1);
        return springInvokerSampleServiceFacade.list(Arrays.asList(Arrays.asList(Arrays.asList(testVo))), jsonObject2, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);
    }

    @RequestMapping("/testArrays")
    public void testArrays() {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");
        springInvokerSampleServiceFacade.testArrays(null, new TestVo[]{testVo}, 1, new int[]{0});
        System.out.println("执行完成");
    }

    @RequestMapping("/testVoid")
    public TestVo testVoid() {
        return springInvokerSampleServiceFacade.testVoid();
    }

    @RequestMapping("/testNull")
    public Object testNull() {
        Object obj = springInvokerSampleServiceFacade.testNull(null);
        return "ok";
    }

    @RequestMapping("/testBytes")
    public Object testBytes() {
        return springInvokerSampleServiceFacade.testByte(new byte[]{1, 2, 3}, new Byte[]{1, 2, 3});
    }

    @Override
    public void afterSingletonsInstantiated() {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");

        Map<String, TestVo> objectHashMap = MapUtil.builder("test", testVo).map();


        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(testVo));
        jsonObject.put("abc1", 123456);
        jsonObject.put("abc2", 1.1314156);
        jsonObject.put("abc3", 1.1314156D);

        Map jsonObject1 = new HashMap<>();
        jsonObject1.put("test", jsonObject);

        Map jsonObject2 = new HashMap<>();
        jsonObject2.put("test", jsonObject1);

        List<TestVo> list = springInvokerSampleServiceFacade.list(Arrays.asList(Arrays.asList(Arrays.asList(testVo))), jsonObject2, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);

    }

}
