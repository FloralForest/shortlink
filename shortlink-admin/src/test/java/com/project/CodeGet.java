//package com.project;
//
//
//import com.baomidou.mybatisplus.annotation.DbType;
//import com.baomidou.mybatisplus.generator.AutoGenerator;
//import com.baomidou.mybatisplus.generator.config.DataSourceConfig;
//import com.baomidou.mybatisplus.generator.config.GlobalConfig;
//import com.baomidou.mybatisplus.generator.config.PackageConfig;
//import com.baomidou.mybatisplus.generator.config.StrategyConfig;
//import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;
//
//
///**
// * 代码生成器工具类
// * 打包时不会打包test所以放在这作为开发用
// */
//public class CodeGet {
//
//    public static void main(String[] args) {
//
//        // 1、创建代码生成器
//        AutoGenerator mpg = new AutoGenerator();
//
//        // 2、全局配置
//        // 全局配置
//        GlobalConfig gc = new GlobalConfig();
//        String projectPath = System.getProperty("user.dir");
//        //gc.setOutputDir(projectPath + "/src/main/java");
//        //项目路径
//        gc.setOutputDir("C:\\JAVA\\Shortlink_Project\\shortlink\\shortlink-admin"+"/src/main/java");
//
//        gc.setServiceName("%sService");	//去掉Service接口的首字母I
//        gc.setAuthor("project");
//        gc.setOpen(false);
//        mpg.setGlobalConfig(gc);
//
//        // 3、数据源配置
//        DataSourceConfig dsc = new DataSourceConfig();
//        dsc.setUrl("jdbc:mysql://localhost:3306/shortlink?serverTimezone=UTC");
//        dsc.setDriverName("com.mysql.cj.jdbc.Driver");
//        dsc.setUsername("root");
//        dsc.setPassword("root");
//        dsc.setDbType(DbType.MYSQL);
//        mpg.setDataSource(dsc);
//
//        // 4、包配置
//        PackageConfig pc = new PackageConfig();
//
//        pc.setParent("com.project.shortlink");
//        pc.setModuleName("admin"); //模块名
//        pc.setController("controller");
//        pc.setEntity("dao.entity");
//        pc.setMapper("dao.mapper");
//        pc.setXml("dao.mapper.xml");
//        pc.setService("service");
//        mpg.setPackageInfo(pc);
//
//        // 5、策略配置
//        StrategyConfig strategy = new StrategyConfig();
//
//        strategy.setInclude("t_user");//数据库表名
//
//        strategy.setNaming(NamingStrategy.underline_to_camel);//数据库表映射到实体的命名策略
//
//        strategy.setColumnNaming(NamingStrategy.underline_to_camel);//数据库表字段映射到实体的命名策略
//        strategy.setEntityLombokModel(true); // lombok 模型 @Accessors(chain = true) setter链式操作
//
//        strategy.setRestControllerStyle(true); //restful api风格控制器
//        strategy.setControllerMappingHyphenStyle(true); //url中驼峰转连字符
//
//        mpg.setStrategy(strategy);
//
//        // 6、执行
//        mpg.execute();
//    }
//}
