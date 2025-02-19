package com.project.shortlink.project.mq.consumer;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.project.common.convention.exception.ServiceException;
import com.project.shortlink.project.dao.entity.*;
import com.project.shortlink.project.dao.mapper.*;
import com.project.shortlink.project.dto.biz.LinkStatsRecordDTO;
import com.project.shortlink.project.mq.idempotent.MessageQueueIdempotentHandler;
import com.project.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.project.shortlink.project.common.constant.RedisKeyConstant.LOCK_GID_UPDATE_KEY;
import static com.project.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * 短链接监控状态保存消息队列消费者
 */
@Slf4j
//交由Spring 管理
@Component
@RequiredArgsConstructor//配合Lombok的构造器注入
//注解注入yml配置
@RocketMQMessageListener(
        topic = "${rocketmq.producer.topic}",
        consumerGroup = "${rocketmq.consumer.group}"
)
//生产者send(Map<String, String> producerMap)，消费者实现接口也必须为Map
public class ShortLinkStatsSaveConsumer implements RocketMQListener<Map<String, String>> {

    private final TLinkMapper tLinkMapper;
    private final TLinkGotoMapper tLinkGotoMapper;
    private final RedissonClient redissonClient;

    private final TLinkAccessStatsMapper tLinkAccessStatsMapper;
    private final TLinkLocaleStatsMapper tLinkLocaleStatsMapper;
    private final TLinkOsStatsMapper tLinkOsStatsMapper;
    private final TLinkBrowserStatsMapper tLinkBrowserStatsMapper;
    private final TLinkAccessLogsMapper tLinkAccessLogsMapper;
    private final TLinkDeviceStatsMapper tLinkDeviceStatsMapper;
    private final TLinkNetworkStatsMapper tLinkNetworkStatsMapper;
    private final TLinkStatsTodayMapper tLinkStatsTodayMapper;
    //延迟队列
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;
    private final MessageQueueIdempotentHandler messageQueueIdempotentHandler;
    //高德获取ip密钥
    @Value("${short-link.stats.locale.amap-key}")
    private String localeKey;


    @Override
    public void onMessage(Map<String, String> producerMap) {
        //keys在com.project.shortlink.project.mq.producer.ShortLinkStatsSaveProducer
        String keys = producerMap.get("keys");
        //判断当前消息缓存是否消费 (避免幂等现象)
        if (!messageQueueIdempotentHandler.isMessageProcessed(keys)) {
            //进一步判断当前的这个消息流程是否执行完成
            //额外加一层判断而不是直接return出去的意义是:预防某些极端情况下程序没有执行完成，同时也没有删除缓存的情况
            if (messageQueueIdempotentHandler.isAccomplish(keys)) {
                return;
            }
            throw new ServiceException("消息未完成流程，需要消息队列重试");
        }
        try {
            String fullShortUrl = producerMap.get("fullShortUrl");
            if (StrUtil.isNotBlank(fullShortUrl)) {
                String gid = producerMap.get("gid");
                //短链接统计
                LinkStatsRecordDTO statsRecord = JSON.parseObject(producerMap.get("statsRecord"), LinkStatsRecordDTO.class);
                actualSaveShortLinkStats(fullShortUrl, gid, statsRecord);
            }
        } catch (Throwable ex) {
            log.error("记录短链接监控消费异常", ex);
            try {
                //如果消息处理遇到异常情况，删除缓存幂等标识 等待MQ自动重试消费消息
                messageQueueIdempotentHandler.delMessageProcessed(keys);
            }catch (Throwable e){
                log.error("删除幂等标识错误",e);
            }
            throw ex;
        }
        //消息流程执行完成
        messageQueueIdempotentHandler.setAccomplish(keys);
    }

    //短链接统计
    public void actualSaveShortLinkStats(String fullShortUrl, String gid, LinkStatsRecordDTO statsRecord) {
        fullShortUrl = Optional.ofNullable(fullShortUrl).orElse(statsRecord.getFullShortUrl());
        //读写锁 设置并获取唯一锁(由固定前缀和当前操作短链接组成)
        RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, fullShortUrl));
        RLock rLock = readWriteLock.readLock();
        //若锁不空闲，加入延迟队列
        if (!rLock.tryLock()) {
            //引入延迟队列调用（为避免修改操作(修改操作也设置了读写锁)和跳转统计操作冲突）
            delayShortLinkStatsProducer.send(statsRecord);
            return;
        }
        try {
            if (StrUtil.isBlank(gid)) {
                final LambdaQueryWrapper<TLinkGoto> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(TLinkGoto::getFullShortUrl, fullShortUrl);
                gid = tLinkGotoMapper.selectOne(wrapper).getGid();
            }
            //获取当前时间为星期几，使用getIso8601Value更直观
            final int week = DateUtil.dayOfWeekEnum(new Date()).getIso8601Value();
            //获取当前时间的小时部分
            final int hour = DateUtil.hour(new Date(), true);
            final TLinkAccessStats stats = TLinkAccessStats
                    .builder()
                    .pv(1)
                    .uv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .uip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .hour(hour)
                    .weekday(week)
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .build();
            tLinkAccessStatsMapper.shortLinkStats(stats);
            //短链接访问地区统计
            final Map<String, Object> map = new HashMap<>();
            //高德密钥
            map.put("Key", localeKey);
            //当前访问ip
            map.put("ip", statsRecord.getRemoteAddr());
            final String localeStr = HttpUtil.get(AMAP_REMOTE_URL, map);
            final JSONObject localeObject = JSON.parseObject(localeStr);
            final String infocode = localeObject.getString("infocode");
            String actualProvince = "未知";
            String actualCity = "未知";
            if (StrUtil.isNotBlank(infocode) && StrUtil.equals(infocode, "10000")) {
                final boolean blank = StrUtil.equals(localeObject.getString("province"), "[]");
                TLinkLocaleStats localeStats = TLinkLocaleStats
                        .builder()
                        .fullShortUrl(fullShortUrl)
                        .gid(gid)
                        .date(new Date())
                        .province(actualProvince = blank ? actualProvince : localeObject.getString("province"))
                        .city(actualCity = blank ? actualCity : localeObject.getString("city"))
                        .adcode(blank ? "未知" : localeObject.getString("adcode"))
                        .cnt(1)
                        .country("中国")
                        .build();
                tLinkLocaleStatsMapper.shortLinkLocalStats(localeStats);
            }
            //统计操作设备
            TLinkOsStats osStats = TLinkOsStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .os(statsRecord.getOs())
                    .build();
            tLinkOsStatsMapper.shortLinkOsStats(osStats);
            //统计浏览器
            TLinkBrowserStats browserStats = TLinkBrowserStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .browser(statsRecord.getBrowser())
                    .build();
            tLinkBrowserStatsMapper.shortLinkBrowserState(browserStats);
            //统计设备
            TLinkDeviceStats deviceStats = TLinkDeviceStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .device(statsRecord.getDevice())
                    .build();
            tLinkDeviceStatsMapper.shortLinkDeviceStats(deviceStats);
            //统计网络类型
            TLinkNetworkStats networkStats = TLinkNetworkStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .network(statsRecord.getNetwork())
                    .build();
            tLinkNetworkStatsMapper.shortLinkNetworkStats(networkStats);
            //统计高频访问IP(user用于统计新老访客->选择一段时间，查询用户是否在过去访问过，访问过则为老访客)
            TLinkAccessLogs accessLogs = TLinkAccessLogs
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .user(statsRecord.getUv())
                    .browser(statsRecord.getBrowser())
                    .os(statsRecord.getOs())
                    .ip(statsRecord.getRemoteAddr())
                    .network(statsRecord.getNetwork())
                    .device(statsRecord.getDevice())
                    .locale(StrUtil.join("-", "中国", actualProvince, actualCity))
                    .build();
            tLinkAccessLogsMapper.insert(accessLogs);
            //link表修改历史pv、uv、uip
            tLinkMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
            //”今日统计“
            TLinkStatsToday statsToday = TLinkStatsToday
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .todayPv(1)
                    .todayUv(statsRecord.getUvFirstFlag() ? 1 : 0)
                    .todayUip(statsRecord.getUipFirstFlag() ? 1 : 0)
                    .build();
            tLinkStatsTodayMapper.shortLinkStatsToday(statsToday);
        } catch (Throwable e) {
            log.error("短链接访问量统计异常", e);
        } finally {
            //修改操作完成后释放锁
            rLock.unlock();
        }
    }
}
