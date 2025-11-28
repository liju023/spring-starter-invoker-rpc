package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.boot.invoker")
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
public class WebSocketClient1Application {


	public static void main(String[] args) {
		SpringApplication.run(WebSocketClient1Application.class, args);
	}
}
