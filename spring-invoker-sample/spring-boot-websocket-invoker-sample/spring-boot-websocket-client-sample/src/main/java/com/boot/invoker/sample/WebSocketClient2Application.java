package com.boot.invoker.sample;


import com.i360day.invoker.annotation.EnableRemoteDiscoveryClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.boot.invoker")
@EnableRemoteDiscoveryClient(basePackages = "com.boot.invoker")
public class WebSocketClient2Application {


	public static void main(String[] args) {
		System.setProperty("server.port", "9093");
		SpringApplication.run(WebSocketClient2Application.class, args);
	}
}
