package com.project.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//持久层接口扫描器
@MapperScan("com.project.shortlink.project.dao.mapper")
public class ShortLinkProjectApplication {
    public static void main(String[] args) {
        SpringApplication.run(ShortLinkProjectApplication.class, args);
    }
}
