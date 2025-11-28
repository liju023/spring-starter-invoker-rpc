package com.invoker.sample.nacos;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableRemoteDiscoveryClient
@EnableDiscoveryClient
@SpringBootApplication
public class NacosServiceApplication {


	public static void main(String[] args) {
		SpringApplication.run(NacosServiceApplication.class, args);
	}


}
