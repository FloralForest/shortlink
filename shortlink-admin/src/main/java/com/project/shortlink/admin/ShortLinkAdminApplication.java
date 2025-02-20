package com.project.shortlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
//持久层接口扫描器
@MapperScan("com.project.shortlink.admin.dao.mapper")
//SpringCloud Nacos 注册中心注解(被调用者添加)
@EnableDiscoveryClient
//调用者添加
@EnableFeignClients("com.project.shortlink.admin.remote")
public class ShortLinkAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkAdminApplication.class, args);
    }
}
