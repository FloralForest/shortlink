package com.project.shortlink.project.mq.idempotent;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * 消息队列幂等处理
 * 幂等指任意一个操作的多次执行总是能获得相同的结果，不会对系统状态产生额外影响
 */
//交由Spring 管理
@Component
@RequiredArgsConstructor//配合Lombok的构造器注入
public class MessageQueueIdempotentHandler {
    private final StringRedisTemplate stringRedisTemplate;
    private static final String IDEMPOTENT_KEY_PREFIX = "short-link:idempotent:";

    //判断当前消息是否消费
    public boolean isMessageProcessed(String messageId){
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        //设置key以保存字符串值，如果key不存在，则设置过期超时返回true，如果存在无操作返回false。
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().setIfAbsent(key,"0",2, TimeUnit.MINUTES));
    }

    //判断消息消费流程是否执行完成
    public boolean isAccomplish(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        //若为1执行完成
        return Objects.equals(stringRedisTemplate.opsForValue().get(key), "1");
    }

    //如果消息处理遇到异常情况，删除幂等标识
    public void delMessageProcessed(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        stringRedisTemplate.delete(key);
    }

    //消息流程执行完成
    public void setAccomplish(String messageId) {
        String key = IDEMPOTENT_KEY_PREFIX + messageId;
        //执行完成设置为1
        stringRedisTemplate.opsForValue().set(key, "1", 2, TimeUnit.MINUTES);
    }
}
