package com.project.shortlink.getaway.config;

import lombok.Data;

import java.util.List;

/**
 * 过滤器配置
 */
@Data
public class Config {

    /**
     * 白名单前置路径（application.yml中）
     */
    private List<String> whitePathList;
}
