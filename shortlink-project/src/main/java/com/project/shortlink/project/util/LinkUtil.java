package com.project.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import jakarta.servlet.http.HttpServletRequest;

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

    /**
     *  获取用户ip
     */
    public static String getClientIp(HttpServletRequest request) {
        String ip;
        // 检查X-Forwarded-For头
        ip = getHeaderValue(request, "X-Forwarded-For");
        if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
            // 取第一个IP（客户端真实IP）
            int index = ip.indexOf(',');
            if (index != -1) {
                ip = ip.substring(0, index).trim();
            }
            return ip;
        }
        // 检查其他头字段
        String[] headers = {
                "X-Real-IP",
                "Proxy-Client-IP",
                "WL-Proxy-Client-IP",
                "HTTP_CLIENT_IP",
                "HTTP_X_FORWARDED_FOR"
        };
        for (String header : headers) {
            ip = getHeaderValue(request, header);
            if (isValidIp(ip)) {
                return ip;
            }
        }
        // 回退到remoteAddr
        return request.getRemoteAddr();
    }
    private static String getHeaderValue(HttpServletRequest request, String header) {
        return request.getHeader(header);
    }
    private static boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip);
    }
}

