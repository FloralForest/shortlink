package com.project.shortlink.admin.common.constant;

/**
 * 后管 Redis 缓存常量类
 */
public class RedisCacheConstant {
    /**
     * 用户注册key
     */
    public static final String LOCK_USER_REGISTER_KEY = "shortlink:lock_user_register:";

    /**
     * 限制用户创建分组分布式锁key
     */
    public static final String LOCK_GROUP_CREATE_KEY = "shortlink:lock_group_create:%s";
}
