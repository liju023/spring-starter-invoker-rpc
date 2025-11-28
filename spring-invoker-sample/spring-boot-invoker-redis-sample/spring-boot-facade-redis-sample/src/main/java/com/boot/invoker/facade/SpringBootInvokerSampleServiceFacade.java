package com.boot.invoker.facade;

import com.boot.invoker.facade.module.SpringInvokerSampleServiceModule;
import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.annotation.RemoteClient;
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
@RemoteClient(address = "http://127.0.0.1:9092", fallbackFactory = SpringBootInvokerSampleServiceHystrix.class)
public interface SpringBootInvokerSampleServiceFacade extends SpringInvokerSampleServiceModule{

    <T> List<TestVo> list(List<TestVo> testVo, Map<String, Object> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, @RemoteRequestParam(serialize = HttpStatus.class) HttpStatus httpStatus);

}
