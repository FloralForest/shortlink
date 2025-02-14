package com.project.shortlink.admin.controller;

import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.remote.LinkRemoteService;
import com.project.shortlink.admin.remote.dto.req.LinkStatsDTO;
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
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};


    @GetMapping("/api/shortlink/admin/stats")
    public Result<LinkStatsRespDTO> linkStats(LinkStatsDTO linkStatsDTO){
        return linkRemoteService.oneLinkStats(linkStatsDTO);
    }

}
