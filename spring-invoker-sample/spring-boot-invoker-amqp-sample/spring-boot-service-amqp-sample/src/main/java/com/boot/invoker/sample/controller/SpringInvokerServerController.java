package com.boot.invoker.sample.controller;

import com.boot.invoker.facade.SpringBootInvokerSampleServerFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class SpringInvokerServerController{

    @Autowired
    private SpringBootInvokerSampleServerFacade springBootInvokerSampleServerFacade;

    @GetMapping("query")
    public Object query(){
        return springBootInvokerSampleServerFacade.query();
    }
}
