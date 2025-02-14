package com.project.shortlink.project.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateField;
import cn.hutool.core.date.DateUtil;
import com.project.shortlink.project.dao.entity.TLinkAccessStats;
import com.project.shortlink.project.dao.entity.TLinkDeviceStats;
import com.project.shortlink.project.dao.entity.TLinkLocaleStats;
import com.project.shortlink.project.dao.entity.TLinkNetworkStats;
import com.project.shortlink.project.dao.mapper.*;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import com.project.shortlink.project.dto.resp.*;
import com.project.shortlink.project.service.TLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 短链接监控实现类
 */
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class TLinkStatsServiceImpl implements TLinkStatsService {

    private final TLinkAccessStatsMapper tLinkAccessStatsMapper;
    private final TLinkLocaleStatsMapper tLinkLocaleStatsMapper;
    private final TLinkOsStatsMapper tLinkOsStatsMapper;
    private final TLinkBrowserStatsMapper tLinkBrowserStatsMapper;
    private final TLinkAccessLogsMapper tLinkAccessLogsMapper;
    private final TLinkDeviceStatsMapper tLinkDeviceStatsMapper;
    private final TLinkNetworkStatsMapper tLinkNetworkStatsMapper;

    @Override
    public LinkStatsRespDTO oneLinkStats(LinkStatsDTO linkStatsDTO) {
        //没有记录返回空
        List<TLinkAccessStats> listStatsByShortLink = tLinkAccessStatsMapper.listStatsByShortLink(linkStatsDTO);
        //判断集合是否空
        if (CollUtil.isEmpty(listStatsByShortLink)) {
            return null;
        }
        //基础信息pv、uv、uip
        List<LinkStatsAccessDailyRespDTO> dailyList = new ArrayList<>();
        //时间范围（若范围内或外没有记录的情况，需要给前端返回0）
        List<String> rangeDates = DateUtil.rangeToList(DateUtil.parse(linkStatsDTO.getStartDate()), DateUtil.parse(linkStatsDTO.getEndDate()), DateField.DAY_OF_MONTH)
                .stream().map(DateUtil::formatDate).toList();
        rangeDates.forEach(each -> listStatsByShortLink.stream()
                //只保留date
                .filter(item -> Objects.equals(each, DateUtil.formatDate(item.getDate())))
                .findFirst()
                .ifPresentOrElse(item -> {
                    //如果有值返回，没有值返回0
                    LinkStatsAccessDailyRespDTO accessDailyRespDTO = LinkStatsAccessDailyRespDTO
                            .builder()
                            .date(each)
                            .pv(item.getPv())
                            .uv(item.getUv())
                            .uip(item.getUip())
                            .build();
                    dailyList.add(accessDailyRespDTO);
                }, () -> {
                    LinkStatsAccessDailyRespDTO accessDailyRespDTO = LinkStatsAccessDailyRespDTO
                            .builder()
                            .date(each)
                            .pv(0)
                            .uv(0)
                            .uip(0)
                            .build();
                    dailyList.add(accessDailyRespDTO);
                }));
        //获取地区
        List<LinkStatsLocaleCNRespDTO> localeCNList = new ArrayList<>();
        List<TLinkLocaleStats> tLinkLocaleStats = tLinkLocaleStatsMapper.listLocaleByShortLink(linkStatsDTO);
        //所有短链接访问量的总和
        int localeCNSum = tLinkLocaleStats.stream().mapToInt(TLinkLocaleStats::getCnt).sum();
        tLinkLocaleStats.forEach(each -> {
            //单短链接访问量占总和比值
            double ratio = (double) each.getCnt() / localeCNSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            LinkStatsLocaleCNRespDTO localeCNRespDTO = LinkStatsLocaleCNRespDTO
                    .builder()
                    .cnt(each.getCnt())
                    .locale(each.getProvince())
                    .ratio(actualRatio)
                    .build();
            localeCNList.add(localeCNRespDTO);
        });
        //小时访问
        List<Integer> hourList = new ArrayList<>();
        //返回短链接、小时和该小时下的访问量总和的集合
        List<TLinkAccessStats> listHourStatsByShortLink = tLinkAccessStatsMapper.listHourStatsByShortLink(linkStatsDTO);
        for (int i = 0; i < 24; i++) {
            //AtomicInteger适用于无锁、线程安全的数值操作
            AtomicInteger hour = new AtomicInteger(i);
            int hourCnt = listHourStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getHour(), hour.get()))
                    .findFirst()
                    //例如15时存在值 把该小时的访问量pv返回
                    .map(TLinkAccessStats::getPv)
                    //不存在返回0
                    .orElse(0);
            hourList.add(hourCnt);
        }
        //高频ip访问
        List<LinkStatsTopIpRespDTO> topIpList = new ArrayList<>();
        List<HashMap<String, Object>> listTopIpByShortLink = tLinkAccessLogsMapper.listTopIpByShortLink(linkStatsDTO);
        listTopIpByShortLink.forEach(each -> {
            LinkStatsTopIpRespDTO statsTopIpRespDTO = LinkStatsTopIpRespDTO
                    .builder()
                    .ip(each.get("ip").toString())
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .build();
            topIpList.add(statsTopIpRespDTO);
        });
        //一周访问
        List<Integer> weekdayList = new ArrayList<>();
        List<TLinkAccessStats> listWeekdayStatsByShortLink = tLinkAccessStatsMapper.listWeekdayStatsByShortLink(linkStatsDTO);
        for (int i = 1; i < 8; i++) {
            //AtomicInteger适用于无锁、线程安全的数值操作
            AtomicInteger weekday = new AtomicInteger(i);
            int weekdayCnt = listWeekdayStatsByShortLink.stream()
                    .filter(each -> Objects.equals(each.getWeekday(), weekday.get()))
                    .findFirst()
                    //例如若周一时存在值 把该周一的访问量pv返回
                    .map(TLinkAccessStats::getPv)
                    //不存在返回0
                    .orElse(0);
            weekdayList.add(weekdayCnt);
        }
        //浏览器访问
        List<LinkStatsBrowserRespDTO> browserList = new ArrayList<>();
        List<HashMap<String, Object>> listBrowserStatsByShortLink = tLinkBrowserStatsMapper.listBrowserStatsByShortLink(linkStatsDTO);
        //所有浏览器访问量总和
        int browserSum = listBrowserStatsByShortLink.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        listBrowserStatsByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("cnt").toString()) / browserSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            LinkStatsBrowserRespDTO browserRespDTO = LinkStatsBrowserRespDTO
                    .builder()
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .browser(each.get("browser").toString())
                    .ratio(actualRatio)
                    .build();
            browserList.add(browserRespDTO);
        });
        //操作系统访问
        List<LinkStatsOsRespDTO> osList = new ArrayList<>();
        List<HashMap<String, Object>> listOsStatsByShortLink = tLinkOsStatsMapper.listOsStatsByShortLink(linkStatsDTO);
        int osSum = listOsStatsByShortLink.stream().mapToInt(each -> Integer.parseInt(each.get("cnt").toString())).sum();
        listOsStatsByShortLink.forEach(each -> {
            double ratio = (double) Integer.parseInt(each.get("cnt").toString()) / osSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            LinkStatsOsRespDTO osRespDTO = LinkStatsOsRespDTO
                    .builder()
                    .cnt(Integer.parseInt(each.get("cnt").toString()))
                    .os(each.get("os").toString())
                    .ratio(actualRatio)
                    .build();
            osList.add(osRespDTO);
        });
        //新老访客 这里重点在SQL
        List<LinkStatsUvRespDTO> uvTypeList = new ArrayList<>();
        Map<String, Object> findUvTypeByShortLink = tLinkAccessLogsMapper.findUvTypeCntByShortLink(linkStatsDTO);
        int oldUserCnt = Integer.parseInt(
                Optional.ofNullable(findUvTypeByShortLink)
                        .map(each -> each.get("oldUserCnt"))
                        .map(Object::toString)
                        .orElse("0"));
        int newUserCnt = Integer.parseInt(
                Optional.ofNullable(findUvTypeByShortLink)
                        .map(each -> each.get("newUserCnt"))
                        .map(Object::toString)
                        .orElse("0"));
        //访客的总和（新老访客）
        int uvSum = oldUserCnt + newUserCnt;
        double oldRatio = (double) oldUserCnt / uvSum;
        double actualOldRatio = Math.round(oldRatio * 100.0) / 100.0;
        double newRatio = (double) newUserCnt / uvSum;
        double actualNewRatio = Math.round(newRatio * 100.0) / 100.0;
        LinkStatsUvRespDTO oldUvRespDTO = LinkStatsUvRespDTO
                .builder()
                .uvType("oldUser")
                .cnt(oldUserCnt)
                .ratio(actualOldRatio)
                .build();
        uvTypeList.add(oldUvRespDTO);
        LinkStatsUvRespDTO newUvRespDTO = LinkStatsUvRespDTO
                .builder()
                .uvType("newUser")
                .cnt(newUserCnt)
                .ratio(actualNewRatio)
                .build();
        uvTypeList.add(newUvRespDTO);
        //设备访问
        List<LinkStatsDeviceRespDTO> deviceList = new ArrayList<>();
        List<TLinkDeviceStats> listDeviceStatsByShortLink = tLinkDeviceStatsMapper.listDeviceStatsByShortLink(linkStatsDTO);
        int deviceSum = listDeviceStatsByShortLink.stream()
                .mapToInt(TLinkDeviceStats::getCnt)
                .sum();
        listDeviceStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / deviceSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            LinkStatsDeviceRespDTO deviceRespDTO = LinkStatsDeviceRespDTO
                    .builder()
                    .cnt(each.getCnt())
                    .device(each.getDevice())
                    .ratio(actualRatio)
                    .build();
            deviceList.add(deviceRespDTO);
        });
        //网络类型访问
        List<LinkStatsNetworkRespDTO> networkList = new ArrayList<>();
        List<TLinkNetworkStats> listNetworkStatsByShortLink = tLinkNetworkStatsMapper.listNetworkStatsByShortLink(linkStatsDTO);
        int networkSum = listNetworkStatsByShortLink.stream()
                .mapToInt(TLinkNetworkStats::getCnt)
                .sum();
        listNetworkStatsByShortLink.forEach(each -> {
            double ratio = (double) each.getCnt() / networkSum;
            double actualRatio = Math.round(ratio * 100.0) / 100.0;
            LinkStatsNetworkRespDTO networkRespDTO = LinkStatsNetworkRespDTO
                    .builder()
                    .cnt(each.getCnt())
                    .network(each.getNetwork())
                    .ratio(actualRatio)
                    .build();
            networkList.add(networkRespDTO);
        });
        return LinkStatsRespDTO
                .builder()
                .daily(dailyList)
                .localeCnStats(localeCNList)
                .hourStats(hourList)
                .topIpStats(topIpList)
                .weekdayStats(weekdayList)
                .browserStats(browserList)
                .osStats(osList)
                .uvTypeStats(uvTypeList)
                .deviceStats(deviceList)
                .networkStats(networkList)
                .build();
    }
}
