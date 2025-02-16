package com.project.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.shortlink.project.dao.entity.TLinkStatsToday;
import com.project.shortlink.project.dao.mapper.TLinkStatsTodayMapper;
import com.project.shortlink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

/**
 * 短链接今日统计接口实现层
 */
@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<TLinkStatsTodayMapper, TLinkStatsToday> implements LinkStatsTodayService {
}
