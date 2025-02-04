package com.project.shortlink.admin.common.biz.user;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.project.shortlink.admin.common.convention.exception.ClientException;
import com.project.shortlink.admin.common.convention.result.Results;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.List;
import java.util.Objects;

import static com.project.shortlink.admin.common.enums.UserErrorCodeEnum.USER_TOKEN_FALL;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * 用户信息传输过滤器 获取当前登录用户
 */
@RequiredArgsConstructor//配合Lombok的构造器注入
public class UserTransmitFilter implements Filter {
    private final StringRedisTemplate stringRedisTemplate;

    //白名单集合
    private static final List<String> IGNORE_URI = Lists.newArrayList(
            "/api/shortlink/admin/user/login",
            "/api/shortlink/admin/user/register",
            "/api/shortlink/admin/user/isUsername"
    );

    @SneakyThrows
    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String requestURL = httpServletRequest.getRequestURI();
        //如果uri为以上集合中的一个，跳过过滤器
        if (!IGNORE_URI.contains(requestURL)) {
            String username = httpServletRequest.getHeader("username");
            String token = httpServletRequest.getHeader("token");
            if (!StrUtil.isAllNotBlank(username, token)) {
                returnJson(
                        (HttpServletResponse) servletResponse,
                        JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FALL))));
                return;
            }
            Object userInfoJson;
            try {
                userInfoJson = stringRedisTemplate.opsForHash().get("login_" + username, token);
                if (userInfoJson == null) {
                    throw new ClientException(USER_TOKEN_FALL);
                }
            } catch (Exception e) {
                returnJson(
                        (HttpServletResponse) servletResponse,
                        JSON.toJSONString(Results.failure(new ClientException(USER_TOKEN_FALL))));
                return;
            }
            UserInfoDTO userInfoDTO = JSON.parseObject(userInfoJson.toString(), UserInfoDTO.class);
            UserContext.setUser(userInfoDTO);
        }
        try {
            filterChain.doFilter(servletRequest, servletResponse);
        } finally {
            UserContext.removeUser();
        }
    }

    private void returnJson(HttpServletResponse response, String json) throws Exception {
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);
        }catch(IOException e){
        } finally{
            if (writer != null)
                writer.close();
        }
    }
}