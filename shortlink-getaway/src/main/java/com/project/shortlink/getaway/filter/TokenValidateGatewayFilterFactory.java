package com.project.shortlink.getaway.filter;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.nacos.client.naming.utils.CollectionUtils;
import com.project.shortlink.getaway.config.Config;
import com.project.shortlink.getaway.dto.GatewayErrorResult;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

/**
 * SpringCloud Gateway Token 拦截器
 * 继承AbstractGatewayFilterFactory<配置> 固定写法
 * 如果pom文件中的filters name为 TokenValidate，那么过滤器名字必须以再拼接上GatewayFilterFactory.
 */
@Component
public class TokenValidateGatewayFilterFactory extends AbstractGatewayFilterFactory<Config> {
    private final StringRedisTemplate stringRedisTemplate;

    public TokenValidateGatewayFilterFactory(StringRedisTemplate stringRedisTemplate) {
        super(Config.class);
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();
            //获取frl路径 /api/shortlink/admin/user
            String requestPath = request.getPath().toString();
            //获取Get||Post||Put等等
            String requestMethod = request.getMethod().name();
            //判断是否在白名单 存在直接往下return
            if (!isPathInWhiteList(requestPath, requestMethod, config.getWhitePathList())) {
                String username = request.getHeaders().getFirst("username");
                String token = request.getHeaders().getFirst("token");
                Object userInfo;
                //判空与判断缓存中是否有值
                if (StringUtils.hasText(username) && StringUtils.hasText(token)
                        && (userInfo = stringRedisTemplate.opsForHash().get("login_" + username, token)) != null) {
                    //正常跳转
                    JSONObject userInfoJsonObject = JSON.parseObject(userInfo.toString());
                    ServerHttpRequest.Builder builder = exchange.getRequest().mutate().headers(httpHeaders -> {
                        httpHeaders.set("userId", userInfoJsonObject.getString("id"));
                        httpHeaders.set("realName", URLEncoder.encode(userInfoJsonObject.getString("realName"), StandardCharsets.UTF_8));
                    });
                    return chain.filter(exchange.mutate().request(builder.build()).build());
                }
                //用户未登录或缓存过期返回401错误
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.writeWith(Mono.fromSupplier(() -> {
                    DataBufferFactory bufferFactory = response.bufferFactory();
                    //自定义网关错误返回信息
                    GatewayErrorResult resultMessage = GatewayErrorResult
                            .builder()
                            .status(HttpStatus.UNAUTHORIZED.value())
                            .message("Token 验证错误")
                            .build();
                    return bufferFactory.wrap(JSON.toJSONString(resultMessage).getBytes());
                }));
            }
            //调取服务请求
            return chain.filter(exchange);
        };
    }

    //判断是否需要验证token
    private boolean isPathInWhiteList(String requestPath, String requestMethod, List<String> whitePathList) {
        //从上到下 白名单不为空&&该路径存在于白名单 || 该路径为user&&该路径请求为Post（只有登录注册为Post详见TUserController）
        return (!CollectionUtils.isEmpty(whitePathList)
                && whitePathList.stream().anyMatch(requestPath::startsWith))
                || (Objects.equals(requestPath, "/api/shortlink/admin/user")
                && Objects.equals(requestMethod, "POST"));
    }
}
