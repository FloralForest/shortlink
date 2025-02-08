package com.project.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

import static com.project.shortlink.project.common.constant.ShortLinkConstant.DEFAULT_CACHE_VALID_TIME;

/**
 * 短链接缓存过期工具类
 */
public class LinkUtil {

    /**
     * 计算设置短链接的缓存有效期
     */
    public static long getLinkCacheValidTime(Date validDate) {

        return Optional.ofNullable(validDate)
                //如果validDate不为空(时限短链接)把有效期跟当前时间计算差值(毫秒数)，缓存有效期为差值
                .map(each -> DateUtil.between(new Date(), each, DateUnit.MS))
                //如果validDate为空(永久短链接)赋值一个月(毫秒数)，缓存有效期为一个月
                .orElse(DEFAULT_CACHE_VALID_TIME);
    }

    /**
     * 重载 转换为 Date（validDateTime 是系统默认时区的时间）
     */
    public static long getLinkCacheValidTime(LocalDateTime validDateTime) {
        if (validDateTime == null) {
            return DEFAULT_CACHE_VALID_TIME;
        }
        // 转换（指定时区 ZoneId.systemDefault()使用jvm当前默认系统时区 转换为 Instant），Date.from()将Instant转换为Date
        Date validDate = Date.from(validDateTime.atZone(ZoneId.systemDefault()).toInstant());
        return getLinkCacheValidTime(validDate);
    }
}

