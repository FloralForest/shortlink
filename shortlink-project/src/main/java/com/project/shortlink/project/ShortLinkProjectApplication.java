package com.project.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
//持久层接口扫描器
@MapperScan("com.project.shortlink.project.dao.mapper")
//SpringCloud Nacos 注册中心注解(被调用者添加)
@EnableDiscoveryClient
public class ShortLinkProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkProjectApplication.class, args);
    }
}
