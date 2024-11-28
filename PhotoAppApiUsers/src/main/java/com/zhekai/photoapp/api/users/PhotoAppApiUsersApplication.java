package com.zhekai.photoapp.api.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.web.exchanges.HttpExchangeRepository;
import org.springframework.boot.actuate.web.exchanges.InMemoryHttpExchangeRepository;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

import com.zhekai.photoapp.api.users.shared.FeignErrorDecoder;

import feign.Logger;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class PhotoAppApiUsersApplication {
    
    @Autowired
    Environment environment;

    public static void main(String[] args) {
	SpringApplication.run(PhotoAppApiUsersApplication.class, args);
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
	return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpExchangeRepository httpTraceRepository() {
	return new InMemoryHttpExchangeRepository();
    }

    @Bean
    @LoadBalanced
    public RestTemplate getRestTemplate(RestTemplateBuilder builder) {
	return builder.build();
    }

    @Bean
    @Profile("production")
    Logger.Level feignLoggerLover() {
	return Logger.Level.NONE;
    }
    
    @Bean
    @Profile("!production")
    Logger.Level feignDefaultLoggerLover() {
	return Logger.Level.FULL;
    }
    
    @Bean
    @Profile("production")
    public String createProductionBean() {
	System.out.println("Production bean created. myapplication.environment = " + environment.getProperty("myapplication.environment"));
	return "Production bean";
    }
    
    @Bean
    @Profile("default")
    public String createDevelopmentBean() {
	System.out.println("Development bean created. myapplication.environment = " + environment.getProperty("myapplication.environment"));
	return "Development bean";
    }

//    @Bean
//    public FeignErrorDecoder getFeignErrorDecoder() {
//	return new FeignErrorDecoder();
//    }
}
