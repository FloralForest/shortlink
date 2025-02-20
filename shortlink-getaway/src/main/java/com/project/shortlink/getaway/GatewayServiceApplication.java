package com.project.shortlink.getaway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 网关服务应用启动器
 * 在微服务项目中应用多个实例负载均衡，网关作用是方便前端路由管理、服务管理、安全等等
 */
@SpringBootApplication
//开启注册功能使用nacos
@EnableDiscoveryClient
public class GatewayServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayServiceApplication.class, args);
    }
}
