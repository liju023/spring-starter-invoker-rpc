package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import com.i360day.invoker.annotation.RemoteScan;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@ImportAutoConfiguration(RedissonAutoConfiguration.class)
//@ImportAutoConfiguration(RedisAutoConfiguration.class)
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
@RemoteScan(basePackages = "com.boot")
public class RedisClientApplication {


    public static void main(String[] args) {
        SpringApplication.run(RedisClientApplication.class, args);
    }
}
