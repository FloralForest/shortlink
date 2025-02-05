package com.project.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.remote.dto.LinkRemoteService;
import com.project.shortlink.admin.remote.dto.req.LinkCreateDTO;
import com.project.shortlink.admin.remote.dto.req.LinkPageDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shortlink/admin/")
public class LinkController {
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};

    //创建
    //创建短链接
    @PostMapping("group/createLink")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateDTO linkCreateDTO){
        return linkRemoteService.createLink(linkCreateDTO);
    }

    //分页查询
    @GetMapping("group/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(LinkPageDTO linkPageDTO){
        return linkRemoteService.pageLink(linkPageDTO);
    }
}
