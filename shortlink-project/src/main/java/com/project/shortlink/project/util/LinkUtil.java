package com.project.shortlink.project.util;

import cn.hutool.core.date.DateUnit;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;

import java.net.URI;
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
     * 获取用户ip
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

    /**
     * 获取用户操作系统
     */
    public static String getOs(HttpServletRequest request) {
        final String userAgent = request.getHeader("User-Agent");
        //解析获取操作系统信息 将整个User-Agent字符串转换为全小写 检查转换后的字符串中是否包含“windows”等
        //XXX windowsXXX -> true
        if (userAgent.toLowerCase().contains("windows")) {
            return "Windows";
        } else if (userAgent.toLowerCase().contains("mac")) {
            return "Mac OS";
        } else if (userAgent.toLowerCase().contains("linux")) {
            return "Linux";
        } else if (userAgent.toLowerCase().contains("android")) {
            return "Android";
        } else if (userAgent.toLowerCase().contains("iphone") || userAgent.toLowerCase().contains("ipad")) {
            return "IOS";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问浏览器
     */
    public static String getBrowser(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("edg")) {
            return "Microsoft Edge";
        } else if (userAgent.toLowerCase().contains("chrome")) {
            return "Google Chrome";
        } else if (userAgent.toLowerCase().contains("firefox")) {
            return "Mozilla Firefox";
        } else if (userAgent.toLowerCase().contains("safari")) {
            return "Apple Safari";
        } else if (userAgent.toLowerCase().contains("opera")) {
            return "Opera";
        } else if (userAgent.toLowerCase().contains("msie") || userAgent.toLowerCase().contains("trident")) {
            return "Internet Explorer";
        } else {
            return "Unknown";
        }
    }

    /**
     * 获取用户访问设备
     */
    public static String getDevice(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        if (userAgent.toLowerCase().contains("mobile")) {
            return "Mobile";
        }
        return "PC";
    }

    /**
     * 获取用户访问网络
     */
    public static String getNetwork(HttpServletRequest request) {
        String actualIp = getClientIp(request);
        // 这里简单判断IP地址范围， 例如，通过调用IP地址来判断网络类型
        return actualIp.startsWith("192.168.") || actualIp.startsWith("10.") ? "WIFI" : "Mobile Networks";
    }

    /**
     * 获取原始链接中的域名
     * 如果原始链接包含 www 开头的话需要去掉
     */
    public static String extractDomain(String url) {
        String domain = null;
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (StrUtil.isNotBlank(host)) {
                domain = host;
                //检查是否以www.开头(就是请求头这么多数据里，只有www.开头的才能进去)
                if (domain.startsWith("www.")) {
                    //返回去www.的数据
                    domain = host.substring(4);
                }
            }
        } catch (Exception ignored) {
        }
        return domain;
    }
}

