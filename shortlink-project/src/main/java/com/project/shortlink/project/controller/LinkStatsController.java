package com.project.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.common.convention.result.Result;
import com.project.shortlink.project.common.convention.result.Results;
import com.project.shortlink.project.dto.req.LinkGroupStatsDTO;
import com.project.shortlink.project.dto.req.LinkStatsAccessRecordDTO;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import com.project.shortlink.project.dto.resp.LinkStatsAccessRecordRespDTO;
import com.project.shortlink.project.dto.resp.LinkStatsRespDTO;
import com.project.shortlink.project.service.TLinkStatsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 短链接监控控制层
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
public class LinkStatsController {
    private final TLinkStatsService linkStatsService;

    //单个短链接监控的所有数据
    @GetMapping("/api/shortlink/project/stats")
    public Result<LinkStatsRespDTO> linkStats(LinkStatsDTO linkStatsDTO){
        return Results.success(linkStatsService.oneLinkStats(linkStatsDTO))
                .setCode("20000")
                .setMessage("查询成功");
    }

    //短链接监控访问记录(日志) + 分页
    @GetMapping("/api/shortlink/project/stats/lar")
    public Result<IPage<LinkStatsAccessRecordRespDTO>> linkAccessRecord(LinkStatsAccessRecordDTO linkStatsDTO){
        return Results.success(linkStatsService.linkStatsAccessRecord(linkStatsDTO))
                .setCode("20000")
                .setMessage("查询成功");
    }

    //分数监控相关数据
    //访问分组短链接指定时间内监控数据
    @GetMapping("/api/shortlink/project/stats/group")
    public Result<LinkStatsRespDTO> groupShortLinkStats(LinkGroupStatsDTO linkGroupStatsDTO) {
        return Results.success(linkStatsService.groupShortLinkStats(linkGroupStatsDTO));
    }
}
