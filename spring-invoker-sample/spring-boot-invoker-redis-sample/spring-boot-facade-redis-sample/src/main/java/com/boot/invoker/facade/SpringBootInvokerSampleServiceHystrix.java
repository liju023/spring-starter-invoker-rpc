package com.boot.invoker.facade;

import com.boot.invoker.facade.vo.TestVo;
import com.i360day.invoker.hystrix.FallbackFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p> @description:   <p>
 *
 * <p> @author: 青牛.胡 <p>
 *
 * <p> @date: 2019/7/22 0022  16:11<p>
 **/
@Component
public class SpringBootInvokerSampleServiceHystrix implements FallbackFactory<SpringBootInvokerSampleServiceFacade> {

    @Override
    public SpringBootInvokerSampleServiceFacade create(Throwable var1) {
        var1.printStackTrace();
        return new SpringBootInvokerSampleServiceFacade() {
            @Override
            public <T> List<TestVo> list(List<TestVo> testVo, Map<String, Object> params, Set<TestVo> testVoSet, Class<TestVo> clazz, Map<String, TestVo> testVoMap, HttpStatus httpStatus) {
                return Collections.emptyList();
            }
        };
    }
}
