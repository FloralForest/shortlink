package com.project.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.common.convention.exception.ClientException;
import com.project.shortlink.project.common.convention.exception.ServiceException;
import com.project.shortlink.project.common.enums.VailDateTypeEnum;
import com.project.shortlink.project.dao.entity.*;
import com.project.shortlink.project.dao.mapper.*;
import com.project.shortlink.project.config.GotoDomainWhiteListDTO;
import com.project.shortlink.project.dto.biz.LinkStatsRecordDTO;
import com.project.shortlink.project.dto.req.LinkBatchCreateDTO;
import com.project.shortlink.project.dto.req.LinkCreateDTO;
import com.project.shortlink.project.dto.req.LinkPageDTO;
import com.project.shortlink.project.dto.req.LinkUpdateDTO;
import com.project.shortlink.project.dto.resp.*;
import com.project.shortlink.project.mq.producer.DelayShortLinkStatsProducer;
import com.project.shortlink.project.service.LinkStatsTodayService;
import com.project.shortlink.project.service.TLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.shortlink.project.util.HashUtil;
import com.project.shortlink.project.util.LinkUtil;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.HttpURLConnection;
import java.net.URL;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static com.project.shortlink.project.common.constant.RedisKeyConstant.*;
import static com.project.shortlink.project.common.constant.ShortLinkConstant.AMAP_REMOTE_URL;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author project
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class TLinkServiceImpl extends ServiceImpl<TLinkMapper, TLink> implements TLinkService {

    //布隆过滤器
    private final RBloomFilter<String> shorUriCreateCachePenetrationBloomFilter;
    private final TLinkGotoMapper tLinkGotoMapper;
    private final StringRedisTemplate stringRedisTemplate;
    //锁
    private final RedissonClient redissonClient;

    private final TLinkAccessStatsMapper tLinkAccessStatsMapper;
    private final TLinkLocaleStatsMapper tLinkLocaleStatsMapper;
    private final TLinkOsStatsMapper tLinkOsStatsMapper;
    private final TLinkBrowserStatsMapper tLinkBrowserStatsMapper;
    private final TLinkAccessLogsMapper tLinkAccessLogsMapper;
    private final TLinkDeviceStatsMapper tLinkDeviceStatsMapper;
    private final TLinkNetworkStatsMapper tLinkNetworkStatsMapper;
    private final TLinkStatsTodayMapper tLinkStatsTodayMapper;

    private final LinkStatsTodayService linkStatsTodayService;
    private final DelayShortLinkStatsProducer delayShortLinkStatsProducer;
    private final GotoDomainWhiteListDTO gotoDomainWhiteListDTO;

    //高德获取ip密钥
    @Value("${short-link.stats.locale.amap-key}")
    private String localeKey;

    @Value("${short-link.domain.defaull}")
    private String shortLinkDomain;

    //创建短链接
    @Override
    public LinkCreateRespDTO createLink(LinkCreateDTO linkCreateDTO) {
        //判断原始链接是否在白名单
        verificationWhitelist(linkCreateDTO.getOriginUrl());
        final String linkDomain = linkCreateDTO.getDomain() != null ? linkCreateDTO.getDomain() + ":8001" : shortLinkDomain;
        //生成的短链
        final String suffix = generateSuffix(linkCreateDTO);
        //与域名拼接的完整短链接
        final String fullShorUrl =linkDomain + "/" + suffix;
        final TLink tLink = TLink
                .builder()
                .domain(linkDomain)
                .originUrl(linkCreateDTO.getOriginUrl())
                .gid(linkCreateDTO.getGid())
                .createdType(linkCreateDTO.getCreatedType())
                .validDateType(linkCreateDTO.getValidDateType())
                .validDate(linkCreateDTO.getValidDate())
                .describe(linkCreateDTO.getDescribe())
                .favicon(this.getFavicon(linkCreateDTO.getOriginUrl()))
                .totalPv(0)
                .totalUv(0)
                .totalUip(0)
                .delTime(0L)
                //添加短链
                .shortUri(suffix)
                .enableStatus(0)
                //完整链接(域名+生成的短链接)
                .fullShortUrl(fullShorUrl)
                .build();

        //添加关联表
        final TLinkGoto tLinkGoto = TLinkGoto
                .builder()
                .fullShortUrl(fullShorUrl)
                .gid(linkCreateDTO.getGid())
                .build();
        try {
            //如果数据库添加失败，抛出异常
            baseMapper.insert(tLink);
            //添加关联表
            tLinkGotoMapper.insert(tLinkGoto);
        } catch (DuplicateKeyException e) {
            //进一步查询数据库判断是否真的存在
            final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TLink::getFullShortUrl, fullShorUrl);
            final TLink istLink = baseMapper.selectOne(lambdaQueryWrapper);
            if (istLink != null) {
                log.warn("短链接：{} 重复", fullShorUrl);
                throw new ServiceException("短链接重复");
            }
        }
        //缓存预热(把必访问的资源添加到缓存)
        stringRedisTemplate.opsForValue().set(
                String.format(GOTO_SHORT_LINK_KEY, fullShorUrl),
                linkCreateDTO.getOriginUrl(),
                LinkUtil.getLinkCacheValidTime(linkCreateDTO.getValidDate()), TimeUnit.MILLISECONDS);
        //添加缓存（一个短链接可配合多个域名，反之同理，所以这里需要添加拼接了域名的完整的短链接）
        shorUriCreateCachePenetrationBloomFilter.add(fullShorUrl);
        //返回前端
        return LinkCreateRespDTO
                .builder()
                .fullShortUrl("http://" + tLink.getFullShortUrl())
                .originUrl(linkCreateDTO.getOriginUrl())
                .gid(linkCreateDTO.getGid())
                .build();
    }

    //原链接生成62进制的短链接
    private String generateSuffix(LinkCreateDTO linkCreateDTO) {
        final String linkDomain = linkCreateDTO.getDomain() != null ? linkCreateDTO.getDomain() + ":8001" : shortLinkDomain;
        //确保短链的唯一性
        //设置最大循环次数
        int custom = 0;
        String shorUri;
        while (true) {
            if (custom > 10) {
                throw new ServiceException("短链频繁生成，稍后再试");
            }
            //原链接
            String originUrl = linkCreateDTO.getOriginUrl();
            //添加一个当前毫秒数，尽量降低冲突
            originUrl += System.currentTimeMillis();
            //数据库保存的原链接是前端传的原链接，这里添加的毫秒数生成的短链接目的是为了降低冲突
            //说白了数据库里只要有短链接去对应原链接就行了
            shorUri = HashUtil.hashToBase(originUrl);
//            //查询是否有生成的短链
//            final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(TLink::getFullShortUrl, linkCreateDTO.getDomain() + "/" + shorUri);
//            final TLink tLink = baseMapper.selectOne(lambdaQueryWrapper);
//            if (tLink == null){
//                break;
//            }
            //布隆过滤器判断 如果不存在break退出while循环，返回生成的链接，布隆过滤器存在误判
            if (!shorUriCreateCachePenetrationBloomFilter.contains(linkDomain + "/" + shorUri)) {
                break;
            }
            custom++;
        }

        //返回生成的链接
        return shorUri;
    }

    //批量创建短链接
    @Override
    public LinkBatchCreateRespDTO batchCreateShortLink(LinkBatchCreateDTO linkBatchCreateDTO) {
        //传过来的url集合与描述集合
        List<String> originUrls = linkBatchCreateDTO.getOriginUrls();
        List<String> describes = linkBatchCreateDTO.getDescribes();
        List<LinkBaseInfoRespDTO> linkBaseInfoRespS = new ArrayList<>();
        //集合里的数据逐条操作转化
        for (int i = 0; i < originUrls.size(); i++) {
            try {
                LinkCreateDTO linkCreateDTO = BeanUtil.toBean(linkBatchCreateDTO, LinkCreateDTO.class);
                linkCreateDTO.setOriginUrl(originUrls.get(i));
                linkCreateDTO.setDescribe(describes.get(i));
                //本质创建单个短链接
                LinkCreateRespDTO link = createLink(linkCreateDTO);
                LinkBaseInfoRespDTO infoRespDTO = LinkBaseInfoRespDTO
                        .builder()
                        .fullShortUrl(link.getFullShortUrl())
                        .originUrl(link.getOriginUrl())
                        .describe(describes.get(i))
                        .build();
                linkBaseInfoRespS.add(infoRespDTO);
            } catch (Throwable e) {
                log.error("批量创建短链接失败——>{}", originUrls.get(i));
            }
        }

        return LinkBatchCreateRespDTO
                .builder()
                .total(linkBaseInfoRespS.size())
                .baseLinkInfos(linkBaseInfoRespS)
                .build();
    }

    //分页查询 配合DataBaseConfiguration工具类分页插件
    @Override
    public IPage<LinkPageRespDTO> pageLink(LinkPageDTO linkPageDTO) {
        IPage<TLink> resultPage = baseMapper.pageLink(linkPageDTO);

        return resultPage.convert(page -> {
            final LinkPageRespDTO result = BeanUtil.toBean(page, LinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    //分组下的短链接数量
    @Override
    public List<LinkCountRespDTO> listLinkCount(List<String> gidNumber) {
        final QueryWrapper<TLink> wrapper = new QueryWrapper<>();
        wrapper.select("gid as gid, count(*) as linkCount")
                .lambda()
                .in(TLink::getGid, gidNumber)
                .eq(TLink::getEnableStatus, 0)
                .eq(TLink::getDelTime, 0L)
                .eq(TLink::getDelFlag, 0)
                .groupBy(TLink::getGid);

        final List<Map<String, Object>> list = baseMapper.selectMaps(wrapper);

        return BeanUtil.copyToList(list, LinkCountRespDTO.class);
    }

    //修改短链接
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void linkUpdate(LinkUpdateDTO linkUpdateDTO) {
        //判断原始链接是否在白名单
        verificationWhitelist(linkUpdateDTO.getOriginUrl());
        //短链分组问题
        final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //前端传修改前的原来的gid(getOriginGid)查询数据，拿修改后的gid(linkUpdateDTO.getGid())会查不到数据
        lambdaQueryWrapper
                .eq(TLink::getGid, linkUpdateDTO.getOriginGid())
                .eq(TLink::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                .eq(TLink::getDelFlag, 0)
                .eq(TLink::getEnableStatus, 0);
        final TLink selectOne = baseMapper.selectOne(lambdaQueryWrapper);
        if (selectOne == null) {
            throw new ClientException("短链接不存在");
        }
        //若gid不变则正常修改
        if (Objects.equals(selectOne.getGid(), linkUpdateDTO.getGid())) {
            //短链信息修改
            LambdaUpdateWrapper<TLink> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            lambdaUpdateWrapper
                    .eq(TLink::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                    .eq(TLink::getGid, linkUpdateDTO.getGid())
                    .eq(TLink::getEnableStatus, 0)
                    .eq(TLink::getDelFlag, 0)
                    .set(Objects.equals(linkUpdateDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType()),
                            TLink::getValidDate, null);
            final TLink tLink = TLink
                    .builder()
                    .domain(selectOne.getDomain())
                    .shortUri(selectOne.getShortUri())
                    .clickNum(selectOne.getClickNum())
                    .favicon(selectOne.getFavicon())
                    .createdType(selectOne.getCreatedType())
                    .gid(linkUpdateDTO.getGid())
                    .originUrl(linkUpdateDTO.getOriginUrl())
                    .describe(linkUpdateDTO.getDescribe())
                    .validDateType(linkUpdateDTO.getValidDateType())
                    .validDate(linkUpdateDTO.getValidDate())
                    .build();
            //修改根据lambdaUpdateWrapper条件查询到的数据
            baseMapper.update(tLink, lambdaUpdateWrapper);
        } else {
            //引入读写锁  设置并获取唯一锁(由固定前缀和当前操作短链接组成)
            RReadWriteLock readWriteLock = redissonClient.getReadWriteLock(String.format(LOCK_GID_UPDATE_KEY, linkUpdateDTO.getFullShortUrl()));
            RLock rLock = readWriteLock.writeLock();
            //若锁不空闲返回信息
            if (!rLock.tryLock()) {
                throw new ServiceException("短链接正在被访问，请稍后再试...");
            }
            try {
                LambdaUpdateWrapper<TLink> lambdaUpdateWrapper = new LambdaUpdateWrapper<>();
                lambdaUpdateWrapper
                        .eq(TLink::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLink::getGid, selectOne.getGid())
                        .eq(TLink::getEnableStatus, 0)
                        .eq(TLink::getDelFlag, 0);
                //逻辑删除原表数据
                TLink delShortLinkDO = TLink.builder()
                        .delTime(System.currentTimeMillis())
                        .build();
                delShortLinkDO.setDelFlag(1);
                baseMapper.update(delShortLinkDO, lambdaUpdateWrapper);
                //向新表添加数据
                final TLink tLink = TLink
                        .builder()
                        .domain(linkUpdateDTO.getDomain() != null ? linkUpdateDTO.getDomain() + ":8001" : shortLinkDomain)
                        .originUrl(linkUpdateDTO.getOriginUrl())
                        .gid(linkUpdateDTO.getGid())
                        .createdType(selectOne.getCreatedType())
                        .validDateType(linkUpdateDTO.getValidDateType())
                        .validDate(linkUpdateDTO.getValidDate())
                        .describe(linkUpdateDTO.getDescribe())
                        .favicon(this.getFavicon(linkUpdateDTO.getOriginUrl()))
                        .totalPv(selectOne.getTotalPv())
                        .totalUv(selectOne.getTotalUv())
                        .totalUip(selectOne.getTotalUip())
                        .delTime(0L)
                        //添加短链
                        .shortUri(selectOne.getShortUri())
                        .enableStatus(selectOne.getEnableStatus())
                        //完整链接(域名+生成的短链接)
                        .fullShortUrl(selectOne.getFullShortUrl())
                        .build();
                baseMapper.insert(tLink);
                //查询该短链接基础统计信息
                LambdaQueryWrapper<TLinkStatsToday> statsTodayQueryWrapper = new LambdaQueryWrapper<>();
                statsTodayQueryWrapper
                        .eq(TLinkStatsToday::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkStatsToday::getGid, selectOne.getGid())
                        .eq(TLinkStatsToday::getDelFlag, 0);
                List<TLinkStatsToday> linkStatsTodayDOList = tLinkStatsTodayMapper.selectList(statsTodayQueryWrapper);
                //若不为空删除gid添加新的gid
                if (CollUtil.isNotEmpty(linkStatsTodayDOList)) {
                    tLinkStatsTodayMapper.deleteBatchIds(linkStatsTodayDOList.stream()
                            .map(TLinkStatsToday::getId)
                            .toList()
                    );
                    linkStatsTodayDOList.forEach(each -> each.setGid(linkUpdateDTO.getGid()));
                    linkStatsTodayService.saveBatch(linkStatsTodayDOList);
                }
                //处理关联表gid
                LambdaQueryWrapper<TLinkGoto> linkGotoQueryWrapper = new LambdaQueryWrapper<>();
                linkGotoQueryWrapper
                        .eq(TLinkGoto::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkGoto::getGid, linkUpdateDTO.getGid());
                TLinkGoto shortLinkGotoDO = tLinkGotoMapper.selectOne(linkGotoQueryWrapper);
                tLinkGotoMapper.deleteById(shortLinkGotoDO.getId());
                shortLinkGotoDO.setGid(linkUpdateDTO.getGid());
                tLinkGotoMapper.insert(shortLinkGotoDO);
                //处理用户访问表gid
                LambdaUpdateWrapper<TLinkAccessStats> linkAccessStatsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkAccessStatsUpdateWrapper
                        .eq(TLinkAccessStats::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkAccessStats::getGid, selectOne.getGid())
                        .eq(TLinkAccessStats::getDelFlag, 0);
                TLinkAccessStats linkAccessStatsDO = TLinkAccessStats.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkAccessStatsMapper.update(linkAccessStatsDO, linkAccessStatsUpdateWrapper);
                //处理地区表gid
                LambdaUpdateWrapper<TLinkLocaleStats> linkLocaleStatsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkLocaleStatsUpdateWrapper
                        .eq(TLinkLocaleStats::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkLocaleStats::getGid, selectOne.getGid())
                        .eq(TLinkLocaleStats::getDelFlag, 0);
                TLinkLocaleStats linkLocaleStatsDO = TLinkLocaleStats.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkLocaleStatsMapper.update(linkLocaleStatsDO, linkLocaleStatsUpdateWrapper);
                //处理操作系统表gid
                LambdaUpdateWrapper<TLinkOsStats> linkOsStatsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkOsStatsUpdateWrapper
                        .eq(TLinkOsStats::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkOsStats::getGid, selectOne.getGid())
                        .eq(TLinkOsStats::getDelFlag, 0);
                TLinkOsStats linkOsStatsDO = TLinkOsStats.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkOsStatsMapper.update(linkOsStatsDO, linkOsStatsUpdateWrapper);
                //处理浏览器表gid
                LambdaUpdateWrapper<TLinkBrowserStats> linkBrowserStatsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkBrowserStatsUpdateWrapper
                        .eq(TLinkBrowserStats::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkBrowserStats::getGid, selectOne.getGid())
                        .eq(TLinkBrowserStats::getDelFlag, 0);
                TLinkBrowserStats linkBrowserStatsDO = TLinkBrowserStats.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkBrowserStatsMapper.update(linkBrowserStatsDO, linkBrowserStatsUpdateWrapper);
                //处理设备表gid
                LambdaUpdateWrapper<TLinkDeviceStats> linkDeviceStatsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkDeviceStatsUpdateWrapper
                        .eq(TLinkDeviceStats::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkDeviceStats::getGid, selectOne.getGid())
                        .eq(TLinkDeviceStats::getDelFlag, 0);
                TLinkDeviceStats linkDeviceStatsDO = TLinkDeviceStats.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkDeviceStatsMapper.update(linkDeviceStatsDO, linkDeviceStatsUpdateWrapper);
                //处理网络类型表gid
                LambdaUpdateWrapper<TLinkNetworkStats> linkNetworkStatsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkNetworkStatsUpdateWrapper
                        .eq(TLinkNetworkStats::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkNetworkStats::getGid, selectOne.getGid())
                        .eq(TLinkNetworkStats::getDelFlag, 0);
                TLinkNetworkStats linkNetworkStatsDO = TLinkNetworkStats.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkNetworkStatsMapper.update(linkNetworkStatsDO, linkNetworkStatsUpdateWrapper);
                //处理日志表gid
                LambdaUpdateWrapper<TLinkAccessLogs> linkAccessLogsUpdateWrapper = new LambdaUpdateWrapper<>();
                linkAccessLogsUpdateWrapper
                        .eq(TLinkAccessLogs::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                        .eq(TLinkAccessLogs::getGid, selectOne.getGid())
                        .eq(TLinkAccessLogs::getDelFlag, 0);
                TLinkAccessLogs linkAccessLogsDO = TLinkAccessLogs.builder()
                        .gid(linkUpdateDTO.getGid())
                        .build();
                tLinkAccessLogsMapper.update(linkAccessLogsDO, linkAccessLogsUpdateWrapper);
            } finally {
                rLock.unlock();
            }
        }
        //若短链接时间变更(兼顾提前手动过期)，需要删除缓存的短链接跳转
        if (!Objects.equals(selectOne.getValidDateType(), linkUpdateDTO.getValidDateType())
                || !Objects.equals(selectOne.getValidDate(), linkUpdateDTO.getValidDate())
                || !Objects.equals(selectOne.getOriginUrl(), linkUpdateDTO.getOriginUrl())) {
            stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, linkUpdateDTO.getFullShortUrl()));
            //若把已过期的短链接又恢复为可用，需要把缓存里的”黑名单“删除。 =把LocalDateTime转成Date再比较确实麻烦=
            //如数据库中的短链接有有效期并且在当前时间之前说明过期，已进入缓存黑名单
            if (selectOne.getValidDate() != null
                    && Date.from(selectOne.getValidDate().atZone(ZoneId.systemDefault()).toInstant()).before(new Date())) {
                //若当前传过来的修改的时间满足可用条件(无有效期或大于当前时间)，则删除缓存的”黑名单“
                if (Objects.equals(linkUpdateDTO.getValidDateType(), VailDateTypeEnum.PERMANENT.getType())
                        || Date.from(linkUpdateDTO.getValidDate().atZone(ZoneId.systemDefault()).toInstant()).after(new Date())) {
                    stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, linkUpdateDTO.getFullShortUrl()));
                }
            }
        }
    }

    //实现短链接跳转(重定向到原链接)
    //因为实际情况用户只会传递短链（如baidu.com/linkUri），没有其它参数，因此需要关联表
    //@SneakyThrows在方法体中自动捕获并处理异常，将异常转换为非受检异常（Unchecked Exception）并抛出。
    @SneakyThrows
    @Override
    public void linkUri(String linkUri, ServletRequest request, ServletResponse response) {
        //请求头中获取（服务器主机名或域名）
        final String serverName = request.getServerName();
        //端口验证
        final String serverPort = Optional.of(request.getServerPort())
                .filter(each -> !Objects.equals(each, 80))
                .map(String::valueOf).map(each -> ":" + each)
                .orElse("");
        //完整短链接
        String fullShortUrl = serverName + serverPort + "/" + linkUri;
        //查找redis缓存 根据前缀key和完整短链接查找原链接 (处理缓存击穿)
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        //使用字符串工具类判空
        if (StrUtil.isNotBlank(originalLink)) {
            LinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            this.shortLinkStats(fullShortUrl, null, statsRecord);
            ((HttpServletResponse) response).sendRedirect(originalLink);
            return;
        }
        //处理缓存穿透(黑名单)
        final boolean contains = shorUriCreateCachePenetrationBloomFilter.contains(fullShortUrl);
        if (!contains) {
            ((HttpServletResponse) response).sendRedirect("/link/notfound");
            return;
        }
        //处理缓存穿透(黑名单)
        final String gotoIsNULL = stringRedisTemplate.opsForValue().get(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl));
        if (StrUtil.isNotBlank(gotoIsNULL)) {
            ((HttpServletResponse) response).sendRedirect("/link/notfound");
            return;
        }
        //redis 分布式锁
        final RLock lock = redissonClient.getLock(String.format(LOCK_GOTO_SHORT_LINK_KEY, fullShortUrl));
        lock.lock();
        try {
            //第一个线程拿到数据存入缓存后，后面就不必继续往下执行
            originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
            if (StrUtil.isBlank(originalLink)) {
                LinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
                this.shortLinkStats(fullShortUrl, null, statsRecord);
                ((HttpServletResponse) response).sendRedirect(originalLink);
                return;
            }

            //查找关联表中完整短链是否存在
            final LambdaQueryWrapper<TLinkGoto> linkGotoWrapper = new LambdaQueryWrapper<>();
            linkGotoWrapper.eq(TLinkGoto::getFullShortUrl, fullShortUrl);
            final TLinkGoto tLinkGoto = tLinkGotoMapper.selectOne(linkGotoWrapper);
            if (tLinkGoto == null) {
                //处理缓存穿透(黑名单)
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                        "-",
                        30,
                        TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/link/notfound");
                return;
            }
            //若关联表完整短链存在，根据关联表的gid和完整短链查找短链表中的数据（主要获取原链接）
            final LambdaQueryWrapper<TLink> tLinkWrapper = new LambdaQueryWrapper<>();
            tLinkWrapper
                    .eq(TLink::getGid, tLinkGoto.getGid())
                    .eq(TLink::getFullShortUrl, fullShortUrl)
                    .eq(TLink::getDelFlag, 0)
                    .eq(TLink::getEnableStatus, 0);
            final TLink tLink = baseMapper.selectOne(tLinkWrapper);
            if (tLink == null || tLink.getValidDate() != null
                    && Date.from(tLink.getValidDate().atZone(ZoneId.systemDefault()).toInstant()).before(new Date())) {
                //短链接过期
                //处理缓存穿透(黑名单)
                stringRedisTemplate.opsForValue().set(
                        String.format(GOTO_IS_NULL_SHORT_LINK_KEY, fullShortUrl),
                        "-",
                        30,
                        TimeUnit.MINUTES);
                ((HttpServletResponse) response).sendRedirect("/link/notfound");
                return;
            }
            //第一个线程拿到存入缓存
            //key前缀(GOTO_SHORT_LINK_KEY)和完整短链接(fullShortUrl)组成的key+原链接(tLink.getShortUri())组成的value存入redis
            //缓存预热(把必访问的资源添加到缓存)过期时间按
            stringRedisTemplate.opsForValue().set(
                    String.format(GOTO_SHORT_LINK_KEY, fullShortUrl),
                    tLink.getOriginUrl(),
                    LinkUtil.getLinkCacheValidTime(tLink.getValidDate()), TimeUnit.MILLISECONDS);
            //跳转（重定向）
            LinkStatsRecordDTO statsRecord = buildLinkStatsRecordAndSetUser(fullShortUrl, request, response);
            this.shortLinkStats(fullShortUrl, tLink.getGid(), statsRecord);
            ((HttpServletResponse) response).sendRedirect(tLink.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    //以上短链接跳转抽取的公共信息
    private LinkStatsRecordDTO buildLinkStatsRecordAndSetUser(String fullShortUrl, ServletRequest request, ServletResponse response) {
        AtomicBoolean aBoolean = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        final AtomicReference<String> uv = new AtomicReference<>();
        Runnable addCookie = () -> {
            uv.set(UUID.fastUUID().toString());
            //设置cookie存于浏览器，标识同一个用户访问短链接
            Cookie cookie = new Cookie("uv", uv.get());
            //过期时间一个月
            cookie.setMaxAge(60 * 60 * 24 * 30);
            //如baidu.com/3V21X8 返回/3V21X8并添加cookie的作用路径（只要短连接部分，因为一个短链接可能有多个域名）
            //控制浏览器在哪些请求中携带该Cookie。表示只有访问/3V21X8 及其子路径时，浏览器才会发送此 Cookie
            cookie.setPath(StrUtil.sub(fullShortUrl, fullShortUrl.indexOf("/"), fullShortUrl.length()));
            ((HttpServletResponse) response).addCookie(cookie);
            stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, uv.get());
            aBoolean.set(Boolean.TRUE);
        };
        //判断是否携带cookie 如果携带进一步判断
        if (ArrayUtil.isNotEmpty(cookies)) {
            Arrays.stream(cookies)
                    //只保留名为 "uv" 的 Cookie
                    .filter(each -> Objects.equals(each.getName(), "uv"))
                    //获取第一个匹配项
                    .findFirst()
                    .map(Cookie::getValue)
                    .ifPresentOrElse(each -> {
                        //如果存在将UV标识存入Redis Set（统计去重）
                        uv.set(each);
                        final Long addUV = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                        aBoolean.set(addUV != null && addUV > 0L);
                    }, addCookie);
        } else {
            addCookie.run();
        }
        String remoteAddr = LinkUtil.getClientIp(((HttpServletRequest) request));
        String os = LinkUtil.getOs(((HttpServletRequest) request));
        String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
        String device = LinkUtil.getDevice(((HttpServletRequest) request));
        String network = LinkUtil.getNetwork((HttpServletRequest) request);
        Long addUIP = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
        boolean ipBoolean = addUIP != null && addUIP > 0L;
        return LinkStatsRecordDTO.builder()
                .fullShortUrl(fullShortUrl)
                .uv(uv.get())
                .uvFirstFlag(aBoolean.get())
                .uipFirstFlag(ipBoolean)
                .remoteAddr(remoteAddr)
                .os(os)
                .browser(browser)
                .device(device)
                .network(network)
                .build();
    }

    //引入延迟队列与读写锁 短链接跳转数据统计
    //com.project.shortlink.project.mq.consumer.DelayShortLinkStatsConsumer
    @Override
    public void shortLinkStats(String fullShortUrl, String gid, LinkStatsRecordDTO statsRecord) {
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
            baseMapper.incrementStats(gid, fullShortUrl, 1, statsRecord.getUvFirstFlag() ? 1 : 0, statsRecord.getUipFirstFlag() ? 1 : 0);
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

    //获取原链接图标
    //@SneakyThrows在方法体中自动捕获并处理异常，将异常转换为非受检异常（Unchecked Exception）并抛出。
    @SneakyThrows
    private String getFavicon(String url) {
        final URL targetUrl = new URL(url);
        HttpURLConnection connection = (HttpURLConnection) targetUrl.openConnection();
        connection.setRequestMethod("GET");
        connection.connect();

        int responseCode = connection.getResponseCode();
        if (HttpURLConnection.HTTP_OK == responseCode) {
            Document document = Jsoup.connect(url).get();
            Element favicon = document.select("link[rel~=(?i)^(shortcut )?icon]").first();
            if (favicon != null) {
                return favicon.attr("abs:href");
            }
        }
        return "获取原链接图标失败";
    }

    //简单判断创建、修改的原始链接是否在白名单
    private void verificationWhitelist(String originUrl) {
        Boolean enable = gotoDomainWhiteListDTO.getEnable();
        //若未开启白名单设置放行
        if (enable == null || !enable) {
            return;
        }
        //若用户传带www的链接需要简单处理一下(因为yml配置的白名单没有www)
        String domain = LinkUtil.extractDomain(originUrl);
        if (StrUtil.isBlank(domain)) {
            throw new ClientException("跳转链接填写错误，请重新输入");
        }
        List<String> details = gotoDomainWhiteListDTO.getDetails();
        //比较若包含至少一个元素时返回true
        if (!details.contains(domain)) {
            throw new ClientException("演示环境为避免恶意攻击，请生成以下网站跳转链接：" + gotoDomainWhiteListDTO.getNames());
        }
    }
}
