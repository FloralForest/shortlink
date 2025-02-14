package com.project.shortlink.project.service;

import com.project.shortlink.project.dto.req.LinkStatsDTO;
import com.project.shortlink.project.dto.resp.LinkStatsRespDTO;

/**
 * 短链接监控接口
 */
public interface TLinkStatsService {

    LinkStatsRespDTO oneLinkStats(LinkStatsDTO linkStatsDTO);
}
