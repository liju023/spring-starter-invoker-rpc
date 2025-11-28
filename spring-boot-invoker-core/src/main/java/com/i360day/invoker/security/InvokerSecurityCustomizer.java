package com.i360day.invoker.security;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;

/**
 * security Customizer
 */
public interface InvokerSecurityCustomizer {
    /**
     * invoker security Customizer
     * @param httpSecurity
     * @throws Exception
     */
    void customize(HttpSecurity httpSecurity) throws Exception;
}
