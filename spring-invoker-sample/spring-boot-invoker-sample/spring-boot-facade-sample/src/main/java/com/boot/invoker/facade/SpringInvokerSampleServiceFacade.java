package com.boot.invoker.facade;

import com.boot.invoker.facade.event.HttpInvokerEvent;
import com.boot.invoker.facade.module.SpringInvokerSampleServiceModule;
import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.annotation.RemoteClient;
import com.i360day.invoker.annotation.RemoteDisable;
import com.i360day.invoker.annotation.RemoteRequestParam;
import com.i360day.invoker.annotation.RemoteResponseBody;
import com.i360day.invoker.codes.decoder.SpringDecode;
import com.i360day.invoker.codes.encoder.SpringEncoder;
import org.springframework.http.HttpStatus;
import org.springframework.scheduling.annotation.Async;

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
//@RemoteClient(decoder = JavaObjectDecoder.class, encoder = JavaObjectEncoder.class)
@RemoteClient(decoder = SpringDecode.class, encoder = SpringEncoder.class, fallbackFactory = SpringInvokerSampleServiceHystrix.class)
public interface SpringInvokerSampleServiceFacade extends SpringInvokerSampleServiceModule {

    String testQuery(TestVo testVo);

    <T> List<TestVo> list(List<List<List<TestVo>>> testVo, Map<String, Map<String, Map<Object, Object>>> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatus httpStatus);

    @Async
    void testArrays(String[] strs, TestVo[] testVos, int number, int[] numbers);

    @RemoteDisable
    Object testNull(Object obj);

    TestVo testVoid();

    TestVo testByte(byte[] bytes1, Byte[] bytes2);

    @RemoteResponseBody(deserialize = TestVo.class)
    <T, O, E> T testSerialize(Class<? extends HttpInvokerEvent> clazz, TestVo testVo, Set<? extends Object> set, List<?>[] a, List<O> list, Map<E, ?> param, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatus httpStatusCode, Enum<? extends HttpStatus> httpStatus);
}
