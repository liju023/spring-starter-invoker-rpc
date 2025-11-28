package com.i360day.invoker.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

public class InvokerSecurityIgnoreService implements InvokerSecurityCustomizer {


    @Override
    public void customize(HttpSecurity httpSecurity) throws Exception {
        
    }
}
