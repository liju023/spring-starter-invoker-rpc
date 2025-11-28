package com.boot.invoker.facade;

import com.boot.invoker.facade.hystrix.SpringInvokerSampleServiceHystrix;
import com.boot.invoker.facade.module.AmqpInvokerSampleServiceModule;
import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.annotation.RemoteAmqpClient;
import com.i360day.invoker.annotation.RemoteRequestParam;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> @description:   <p>
 * <p>
 * <p> @author: 胡.青牛 <p>
 * <p>
 * <p> @date: 2019/6/19 0019  14:33<p>fallback = SpringEurekaEurekaInvokerSampleServiceHystrix.class,
 **/
@RemoteAmqpClient(address = "amqp://47.92.170.164:25672", username = "ryb", password = "Ryb@2021", virtualHost = "/ryb-data", fallbackFactory = SpringInvokerSampleServiceHystrix.class)
public interface AmqpInvokerSampleServiceFacade extends AmqpInvokerSampleServiceModule {

    String testQuery(TestVo testVo);

    <T> List<TestVo> list(List<TestVo> testVo, Map<String, Object> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatus httpStatus);

    void testArrays(String[] strs, TestVo[] testVos, int number, int [] numbers);

    void testNull(Object obj);

    TestVo testVoid();

    TestVo testByte(byte [] bytes1, Byte [] bytes2);
}
