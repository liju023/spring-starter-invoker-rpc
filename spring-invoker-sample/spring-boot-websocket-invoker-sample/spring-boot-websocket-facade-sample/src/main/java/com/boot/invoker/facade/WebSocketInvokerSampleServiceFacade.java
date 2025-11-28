package com.boot.invoker.facade;

import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.annotation.RemoteRequestParam;
import com.i360day.invoker.annotation.RemoteWebSocketClient;
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
@RemoteWebSocketClient(address = "ws://127.0.0.1:9092/invoker-server/${spring.invoker.web-socket.endpoint}", username = "invoker-username", password = "123456", fallbackFactory = WebSocketInvokerSampleServiceHystrix.class)
public interface WebSocketInvokerSampleServiceFacade {

    String testQuery(TestVo testVo);

    <T> List<TestVo> list(List<TestVo> testVo, Map<String, Object> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatus httpStatus);

    void testArrays(String[] strs, TestVo[] testVos, int number, int [] numbers);

    void testNull(Object obj);

    TestVo testVoid();

    TestVo testByte(byte [] bytes1, Byte [] bytes2);
}
