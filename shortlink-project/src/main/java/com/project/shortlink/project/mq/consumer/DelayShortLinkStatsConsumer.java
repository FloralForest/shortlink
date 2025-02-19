package com.project.shortlink.project.mq.consumer;

import com.project.shortlink.project.dto.biz.LinkStatsRecordDTO;
import com.project.shortlink.project.service.TLinkService;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBlockingDeque;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

import static com.project.shortlink.project.common.constant.RedisKeyConstant.DELAY_QUEUE_STATS_KEY;

/**
 * 延迟队列
 * 延迟记录短链接统计消费者
 * Redis的延迟队列
 * 消费阻塞
 */
//将类交给 Spring 管理 适用于工具类
@Component
@RequiredArgsConstructor//配合Lombok的构造器注入
public class DelayShortLinkStatsConsumer implements InitializingBean {

    private final RedissonClient redissonClient;
    private final TLinkService tLinkService;

    public void onMessage() {
        //通过Executors.newSingleThreadExecutor创建单线程线程池
        Executors.newSingleThreadExecutor(
                //自定义线程名为delay_short-link_stats_consumer，并设置为守护线程
                        runnable -> {
                            Thread thread = new Thread(runnable);
                            thread.setName("delay_short-link_stats_consumer");
                            thread.setDaemon(Boolean.TRUE);
                            return thread;
                        })
                .execute(() -> {
                    //获取队列（com.project.shortlink.project.mq.producer中设置了队列）
                    RBlockingDeque<LinkStatsRecordDTO> blockingDeque = redissonClient.getBlockingDeque(DELAY_QUEUE_STATS_KEY);
                    RDelayedQueue<LinkStatsRecordDTO> delayedQueue = redissonClient.getDelayedQueue(blockingDeque);
                    for (; ; ) {
                        try {
                            //非阻塞获取队列元素
                            LinkStatsRecordDTO statsRecord = delayedQueue.poll();
                            // 若队列不为空，调用统计接口
                            if (statsRecord != null) {
                                tLinkService.shortLinkStats(null, null, statsRecord);
                                continue;
                            }
                            //空队列等待
                            LockSupport.parkUntil(500);
                        } catch (Throwable ignored) {
                        }
                    }
                });
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        onMessage();
    }
}
