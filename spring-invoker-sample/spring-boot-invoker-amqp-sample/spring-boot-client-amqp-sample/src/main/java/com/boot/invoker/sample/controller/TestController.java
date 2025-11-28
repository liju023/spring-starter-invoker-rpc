package com.boot.invoker.sample.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.boot.invoker.facade.AmqpInvokerSampleServiceFacade;
import com.boot.invoker.facade.hystrix.SpringInvokerSampleServiceHystrix;
import com.boot.invoker.facade.vo.TestVo;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.i360day.invoker.HttpInvokerClientMethodInterceptor;
import com.i360day.invoker.InvokerBuilder;
import com.i360day.invoker.codes.decoder.Decoder;
import com.i360day.invoker.context.InvokerContext;
import com.i360day.invoker.executor.AmqpInvokerRequestExecutor;
import com.i360day.invoker.proxy.DefaultTargeterHandler;
import com.i360day.invoker.support.RemoteInvocationFactory;
import org.aopalliance.intercept.MethodInterceptor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
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
    private ApplicationContext applicationContext;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpInvokerSampleServiceFacade amqpInvokerSampleServiceFacade;

//    @Autowired
//    private HttpInvokerProperties httpInvokerProperties;

    @RequestMapping("/query")
    public String query(TestVo testVo1) throws InterruptedException {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");


        //test amqp
        {
            AmqpInvokerSampleServiceFacade facade = InvokerBuilder.create()
                    .interfaceClass(AmqpInvokerSampleServiceFacade.class)
                    .classLoader(AmqpInvokerSampleServiceFacade.class.getClassLoader())
                    .httpInvokerContext(applicationContext.getBean(InvokerContext.class))
                    .fallbackFactory(SpringInvokerSampleServiceHystrix.class)
                    .interceptor((Function<InvokerBuilder, MethodInterceptor>) (customizer) -> {
                        return new HttpInvokerClientMethodInterceptor(
                                customizer.getTargetProxy(),
                                customizer.getHttpInvokerContext().getBean(AmqpInvokerRequestExecutor.class),
                                customizer.getHttpInvokerContext().getBean(RemoteInvocationFactory.class),
                                customizer.getHttpInvokerContext().getBean(Decoder.class)
                        );
                    })
                    .targeter(new DefaultTargeterHandler())
                    .build();
            String result = facade.testQuery(testVo);
            System.out.println(result);
        }


        return amqpInvokerSampleServiceFacade.testQuery(testVo);
    }

    @JsonSerialize
    @RequestMapping("/list")
    public List<TestVo> list() throws InterruptedException {
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");


        Map<String, TestVo> objectHashMap = new HashMap<String, TestVo>() {{
            put("test", testVo);
        }};

        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(testVo));
        jsonObject.put("abc1", 123456);
        jsonObject.put("abc2", 1.1314156);
        jsonObject.put("abc3", 1.1314156D);
        return amqpInvokerSampleServiceFacade.list(Arrays.asList(testVo), jsonObject, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);
    }

    @RequestMapping("/testArrays")
    public void testArrays(){
        TestVo testVo = new TestVo();
        testVo.setUserName("admin");
        testVo.setPassword("123456");
        amqpInvokerSampleServiceFacade.testArrays(null, new TestVo[]{testVo}, 1, new int[]{0} );
    }

    @RequestMapping("/testVoid")
    public TestVo testVoid(){
        return amqpInvokerSampleServiceFacade.testVoid();
    }

    @RequestMapping("/testNull")
    public Object testNull(){
        amqpInvokerSampleServiceFacade.testNull(null);
        return "ok";
    }

    @RequestMapping("/testBytes")
    public Object testBytes(){
        return amqpInvokerSampleServiceFacade.testByte(new byte[]{1, 2, 3}, new Byte[]{1, 2, 3});
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
//        List<TestVo> list = springInvokerSampleServiceFacade.list(Arrays.asList(testVo), jsonObject, Arrays.asList(testVo).stream().collect(Collectors.toSet()), TestVo.class, objectHashMap, HttpStatus.OK);

    }

}
