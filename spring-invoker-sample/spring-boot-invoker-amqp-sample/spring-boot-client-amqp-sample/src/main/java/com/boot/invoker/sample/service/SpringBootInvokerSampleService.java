package com.boot.invoker.sample.service;

import com.boot.invoker.facade.SpringBootInvokerSampleServerFacade;
import org.springframework.stereotype.Service;

@Service
public class SpringBootInvokerSampleService implements SpringBootInvokerSampleServerFacade {
    @Override
    public Object query() {
        return "请求成功";
    }
}
