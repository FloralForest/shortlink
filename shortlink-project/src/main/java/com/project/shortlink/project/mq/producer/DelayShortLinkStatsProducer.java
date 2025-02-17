package com.project.shortlink.project.mq.producer;

import com.project.shortlink.project.dto.biz.LinkStatsRecordDTO;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

import static com.project.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟队列
 * 延迟消费短链接统计发送者
 * Redis的延迟队列
 * 设置阻塞
 */
//将类交给 Spring 管理 适用于工具类
@Component
@RequiredArgsConstructor//配合Lombok的构造器注入
public class DelayShortLinkStatsProducer {

    private final RedissonClient redissonClient;

    /**
     * 发送延迟消费短链接统计
     */
    public void send(LinkStatsRecordDTO statsRecord) {
        //获取阻塞队列 消费者会从这个队列中消费数据。
        RBlockingDeque<LinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
        //绑定延迟队列 允许元素在指定延迟时间后自动转移到绑定的阻塞队列（这里绑定的是以上阻塞队列）。
        RDelayedQueue<LinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
        //对象放入延迟队列，并设置5秒的延迟时间 5秒后，该元素会自动转移到RBlockingDeque，消费者可开始消费。
        delayedQueue.offer(statsRecord, 5, TimeUnit.SECONDS);
    }
}
