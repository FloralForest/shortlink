package com.project.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.remote.LinkActuaRemoteService;
import com.project.shortlink.admin.remote.LinkRemoteService;
import com.project.shortlink.admin.remote.dto.req.LinkGroupStatsAccessRecordDTO;
import com.project.shortlink.admin.remote.dto.req.LinkGroupStatsDTO;
import com.project.shortlink.admin.remote.dto.req.LinkStatsAccessRecordDTO;
import com.project.shortlink.admin.remote.dto.req.LinkStatsDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkStatsAccessRecordRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
public class LinkStatsController {
    //SpringCloud调用
    private final LinkActuaRemoteService linkActuaRemoteService;

    //传统调用
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};


    //短链接监控所有数据
    @GetMapping("/api/shortlink/admin/stats")
    public Result<LinkStatsRespDTO> linkStats(LinkStatsDTO linkStatsDTO){
        return linkActuaRemoteService.oneLinkStats(
                linkStatsDTO.getGid(),
                linkStatsDTO.getFullShortUrl(),
                linkStatsDTO.getStartDate(),
                linkStatsDTO.getEndDate());
    }

    //短链接监控访问记录(日志) + 分页
    @GetMapping("/api/shortlink/admin/stats/lar")
    public Result<Page<LinkStatsAccessRecordRespDTO>> linkAccessRecord(LinkStatsAccessRecordDTO linkStatsDTO){
        return linkActuaRemoteService.linkStatsAccessRecord(
                linkStatsDTO.getGid(),
                linkStatsDTO.getFullShortUrl(),
                linkStatsDTO.getStartDate(),
                linkStatsDTO.getEndDate());
    }

    ////分数监控相关数据
    //访问分组短链接指定时间内监控数据
    @GetMapping("/api/shortlink/admin/stats/group")
    public Result<LinkStatsRespDTO> groupShortLinkStats(LinkGroupStatsDTO linkStatsDTO) {
        return linkActuaRemoteService.groupShortLinkStats(
                linkStatsDTO.getGid(),
                linkStatsDTO.getStartDate(),
                linkStatsDTO.getEndDate());
    }

    //访问分组短链接监控访问记录(日志) + 分页
    @GetMapping("/api/shortlink/admin/stats/group/lar")
    public Result<Page<LinkStatsAccessRecordRespDTO>> groupLinkAccessRecord(LinkGroupStatsAccessRecordDTO linkStatsDTO){
        return linkActuaRemoteService.groupLinkStatsAccessRecord(
                linkStatsDTO.getGid(),
                linkStatsDTO.getStartDate(),
                linkStatsDTO.getEndDate());
    }
}
