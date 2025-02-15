package com.project.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.dto.req.LinkStatsAccessRecordDTO;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import com.project.shortlink.project.dto.resp.LinkStatsAccessRecordRespDTO;
import com.project.shortlink.project.dto.resp.LinkStatsRespDTO;

/**
 * 短链接监控接口
 */
public interface TLinkStatsService {

    //单个短链接监控的所有数据
    LinkStatsRespDTO oneLinkStats(LinkStatsDTO linkStatsDTO);

    //短链接监控访问记录(日志) + 分页
    IPage<LinkStatsAccessRecordRespDTO> linkStatsAccessRecord(LinkStatsAccessRecordDTO linkStatsDTO);
}
