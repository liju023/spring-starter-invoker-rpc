package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
public class AmqpClientApplication {


    public static void main(String[] args) {
        SpringApplication.run(AmqpClientApplication.class, args);
    }
}
