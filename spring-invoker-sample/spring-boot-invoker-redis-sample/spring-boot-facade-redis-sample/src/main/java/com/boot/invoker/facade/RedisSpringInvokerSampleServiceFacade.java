package com.boot.invoker.facade;

import com.boot.invoker.facade.module.SpringInvokerSampleServiceModule;
import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.annotation.RemoteRedisClient;
import com.i360day.invoker.annotation.RemoteRequestParam;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

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
@RemoteRedisClient(address = "redis://127.0.0.1:6379", password = "123456", fallbackFactory = RedisSpringInvokerSampleServiceHystrix.class)
public interface RedisSpringInvokerSampleServiceFacade extends SpringInvokerSampleServiceModule{

    String testQuery(TestVo testVo);

    <T> List<TestVo> list(List<TestVo> testVo, Map<String, Object> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatusCode httpStatus);

    void testArrays(String[] strs, TestVo[] testVos, int number, int [] numbers);

    void testNull(Object obj);

    TestVo testVoid();

    TestVo testByte(byte [] bytes1, Byte [] bytes2);
}
