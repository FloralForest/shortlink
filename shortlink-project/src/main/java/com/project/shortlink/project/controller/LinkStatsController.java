package com.project.shortlink.project.controller;

import com.project.shortlink.project.common.convention.result.Result;
import com.project.shortlink.project.common.convention.result.Results;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
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

    @GetMapping("/api/shortlink/project/stats")
    public Result<LinkStatsRespDTO> linkStats(LinkStatsDTO linkStatsDTO){
        return Results.success(linkStatsService.oneLinkStats(linkStatsDTO))
                .setCode("20000")
                .setMessage("查询成功");
    }

}
