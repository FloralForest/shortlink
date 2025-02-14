package com.project.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
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
import com.project.shortlink.project.dto.req.LinkCreateDTO;
import com.project.shortlink.project.dto.req.LinkPageDTO;
import com.project.shortlink.project.dto.req.LinkUpdateDTO;
import com.project.shortlink.project.dto.resp.LinkCountRespDTO;
import com.project.shortlink.project.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;
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

    //高德获取ip密钥
    @Value("${short-link.stats.locale.amap-key}")
    private String localeKey;

    //创建短链接
    @Override
    public LinkCreateRespDTO createLink(LinkCreateDTO linkCreateDTO) {
        //生成的短链
        final String suffix = generateSuffix(linkCreateDTO);
        //与域名拼接的完整短链接
        final String fullShorUrl = linkCreateDTO.getDomain() + "/" + suffix;
        final TLink tLink = TLink
                .builder()
                .domain(linkCreateDTO.getDomain())
                .originUrl(linkCreateDTO.getOriginUrl())
                .gid(linkCreateDTO.getGid())
                .createdType(linkCreateDTO.getCreatedType())
                .validDateType(linkCreateDTO.getValidDateType())
                .validDate(linkCreateDTO.getValidDate())
                .describe(linkCreateDTO.getDescribe())
                .favicon(this.getFavicon(linkCreateDTO.getOriginUrl()))
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
            //布隆过滤器判断 如果不存在break退出循环（添加），布隆过滤器存在误判
            if (!shorUriCreateCachePenetrationBloomFilter.contains(linkCreateDTO.getDomain() + "/" + shorUri)) {
                break;
            }
            custom++;
        }

        //返回生成的链接
        return shorUri;
    }

    //分页查询 配合DataBaseConfiguration工具类分页插件
    @Override
    public IPage<LinkPageRespDTO> pageLink(LinkPageDTO linkPageDTO) {
        final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(TLink::getGid, linkPageDTO.getGid())
                .eq(TLink::getEnableStatus, 0)
                .eq(TLink::getDelFlag, 0)
                //默认根据创建时间排序
                .orderByDesc(TLink::getCreateTime);
        IPage<TLink> resultPage = baseMapper.selectPage(linkPageDTO, lambdaQueryWrapper);

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
                .groupBy(TLink::getGid);

        final List<Map<String, Object>> list = baseMapper.selectMaps(wrapper);

        return BeanUtil.copyToList(list, LinkCountRespDTO.class);
    }

    //修改短链接
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void linkUpdate(LinkUpdateDTO linkUpdateDTO) {
        //短链分组问题
        final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TLink::getGid, linkUpdateDTO.getGid())
                .eq(TLink::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                .eq(TLink::getDelFlag, 0)
                .eq(TLink::getEnableStatus, 0);
        final TLink selectOne = baseMapper.selectOne(lambdaQueryWrapper);
        if (selectOne == null) {
            throw new ClientException("短链接不存在");
        }
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
            //修改根据lambdaUpdateWrapper条件查询到的数据
            baseMapper.update(tLink, lambdaUpdateWrapper);
        } else {
            LambdaUpdateWrapper<TLink> lambdaWrapper = new LambdaUpdateWrapper<>();
            lambdaWrapper
                    .eq(TLink::getFullShortUrl, linkUpdateDTO.getFullShortUrl())
                    .eq(TLink::getGid, selectOne.getGid())
                    .eq(TLink::getEnableStatus, 0)
                    .eq(TLink::getDelFlag, 0);
            baseMapper.delete(lambdaWrapper);
            baseMapper.insert(tLink);
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
        //完整短链接
        String fullShortUrl = serverName + "/" + linkUri;
        //查找redis缓存 根据前缀key和完整短链接查找原链接 (处理缓存击穿)
        String originalLink = stringRedisTemplate.opsForValue().get(String.format(GOTO_SHORT_LINK_KEY, fullShortUrl));
        //使用字符串工具类判空
        if (StrUtil.isNotBlank(originalLink)) {
            this.shortLinkStats(fullShortUrl, null, request, response);
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
                this.shortLinkStats(fullShortUrl, null, request, response);
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
            this.shortLinkStats(fullShortUrl, tLink.getGid(), request, response);
            ((HttpServletResponse) response).sendRedirect(tLink.getOriginUrl());
        } finally {
            lock.unlock();
        }
    }

    //短链接跳转数据统计
    private void shortLinkStats(String fullShortUrl, String gid, ServletRequest request, ServletResponse response) {
        AtomicBoolean aBoolean = new AtomicBoolean();
        Cookie[] cookies = ((HttpServletRequest) request).getCookies();
        try {
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
                        .findFirst().map(Cookie::getValue)
                        .ifPresentOrElse(each -> {
                            //如果存在将UV标识存入Redis Set（统计去重）
                            uv.set(each);
                            final Long addUV = stringRedisTemplate.opsForSet().add("short-link:stats:uv:" + fullShortUrl, each);
                            aBoolean.set(addUV != null && addUV > 0L);
                        }, addCookie);
            } else {
                addCookie.run();
            }
            //统计ip
            final String remoteAddr = LinkUtil.getClientIp(((HttpServletRequest) request));
            final Long addUIP = stringRedisTemplate.opsForSet().add("short-link:stats:uip:" + fullShortUrl, remoteAddr);
            boolean ipBoolean = addUIP != null && addUIP > 0L;
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
                    .uv(aBoolean.get() ? 1 : 0)
                    .uip(ipBoolean ? 1 : 0)
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
            map.put("ip", remoteAddr);
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
                        .province(actualProvince = blank ? "未知" : localeObject.getString("province"))
                        .city(actualCity = blank ? "未知" : localeObject.getString("city"))
                        .adcode(blank ? "未知" : localeObject.getString("adcode"))
                        .cnt(1)
                        .country("中国")
                        .build();
                tLinkLocaleStatsMapper.shortLinkLocalStats(localeStats);
            }
            //统计操作设备
            final String os = LinkUtil.getOs(((HttpServletRequest) request));
            TLinkOsStats osStats = TLinkOsStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .os(os)
                    .build();
            tLinkOsStatsMapper.shortLinkOsStats(osStats);
            //统计浏览器
            final String browser = LinkUtil.getBrowser(((HttpServletRequest) request));
            TLinkBrowserStats browserStats = TLinkBrowserStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .browser(browser)
                    .build();
            tLinkBrowserStatsMapper.shortLinkBrowserState(browserStats);
            //统计设备
            final String device = LinkUtil.getDevice(((HttpServletRequest) request));
            TLinkDeviceStats deviceStats = TLinkDeviceStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .device(device)
                    .build();
            tLinkDeviceStatsMapper.shortLinkDeviceStats(deviceStats);
            //统计网络类型
            final String network = LinkUtil.getNetwork((HttpServletRequest) request);
            TLinkNetworkStats networkStats = TLinkNetworkStats
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .date(new Date())
                    .cnt(1)
                    .network(network)
                    .build();
            tLinkNetworkStatsMapper.shortLinkNetworkStats(networkStats);
            //统计高频访问IP(user用于统计新老访客->选择一段时间，查询用户是否在过去访问过，访问过则为老访客)
            TLinkAccessLogs accessLogs = TLinkAccessLogs
                    .builder()
                    .fullShortUrl(fullShortUrl)
                    .gid(gid)
                    .user(uv.get())
                    .browser(browser)
                    .os(os)
                    .ip(remoteAddr)
                    .network(network)
                    .device(device)
                    .locale(StrUtil.join("-","中国",actualProvince,actualCity))
                    .build();
            tLinkAccessLogsMapper.insert(accessLogs);
        } catch (Throwable e) {
            log.error("短链接访问量统计异常", e);
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
}
