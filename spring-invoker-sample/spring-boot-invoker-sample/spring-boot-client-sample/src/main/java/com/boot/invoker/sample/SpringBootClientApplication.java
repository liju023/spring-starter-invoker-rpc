package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import com.i360day.invoker.annotation.RemoteScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@RemoteScan(basePackages = "com.boot.invoker")
@EnableRemoteDiscoveryClient
@SpringBootApplication(scanBasePackages = "com.boot.invoker")
public class SpringBootClientApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringBootClientApplication.class, args);
	}
}
