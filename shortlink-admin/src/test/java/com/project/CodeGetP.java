package com.project;

import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
import com.baomidou.mybatisplus.generator.config.GlobalConfig;
import com.baomidou.mybatisplus.generator.config.PackageConfig;
import com.baomidou.mybatisplus.generator.config.StrategyConfig;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

//MyBatis-Plus 3.5.0+ 版本要求使用 建造者模式（Builder） 配置代码生成器
public class CodeGetP {
    public static void main(String[] args) {
        //数据源配置
        DataSourceConfig dataSourceConfig = new DataSourceConfig.Builder(
                "jdbc:mysql://localhost:3306/shortlink?useSSL=false&serverTimezone=Asia/Shanghai",
                "root",
                "root"
        ).build();

        //全局配置（新版API）
        GlobalConfig globalConfig = new GlobalConfig.Builder()
                .outputDir("C:\\JAVA\\Shortlink_Project\\shortlink\\shortlink-admin" + "/src/main/java")
                .author("project")
                .disableOpenDir()  //open(false)
                .build();

        //包配置
        PackageConfig packageConfig = new PackageConfig.Builder()
                .parent("com.project.shortlink")
                .moduleName("admin")
                .entity("dao.entity")
                .mapper("dao.mapper")
                .xml("dao.mapper.xml")
                .service("service")
                .controller("controller")
                .build();

        //策略配置
        StrategyConfig strategyConfig = new StrategyConfig.Builder()
                .addInclude("t_group")
                .entityBuilder()  // 实体配置分离
                .naming(NamingStrategy.underline_to_camel)
                .columnNaming(NamingStrategy.underline_to_camel)
                .enableLombok()  // 正确启用Lombok
                .enableTableFieldAnnotation()
                .serviceBuilder()
                .formatServiceFileName("%sService")  //去掉Service接口的首字母I
                .controllerBuilder()
                .enableRestStyle()
                .mapperBuilder()
                .enableMapperAnnotation()
                .build();

        //生成器配置使用链式调用
        new AutoGenerator(dataSourceConfig)
                .global(globalConfig)     // 替换 setGlobalConfig
                .packageInfo(packageConfig) // 替换 setPackageInfo
                .strategy(strategyConfig)   // 替换 setStrategy
                .execute();
    }
}