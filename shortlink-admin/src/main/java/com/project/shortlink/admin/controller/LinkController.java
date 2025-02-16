package com.project.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.remote.LinkRemoteService;
import com.project.shortlink.admin.remote.dto.req.LinkBatchCreateDTO;
import com.project.shortlink.admin.remote.dto.req.LinkCreateDTO;
import com.project.shortlink.admin.remote.dto.req.LinkPageDTO;
import com.project.shortlink.admin.remote.dto.req.LinkUpdateDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkBaseInfoRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkBatchCreateRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import com.project.shortlink.admin.util.EasyExcelWebUtil;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/shortlink/admin/")
public class LinkController {
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};

    //创建
    //创建短链接
    @PostMapping("link/createLink")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateDTO linkCreateDTO){
        return linkRemoteService.createLink(linkCreateDTO);
    }
    //批量创建短链接
    @PostMapping("link/createLink/batch")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public void batchCreateLink(@RequestBody LinkBatchCreateDTO linkBatchCreateDTO, HttpServletResponse response){
        Result<LinkBatchCreateRespDTO> linkBatchCreateRespDTOResult = linkRemoteService.batchCreateLink(linkBatchCreateDTO);
        if (linkBatchCreateRespDTOResult.isSuccess()){
            final List<LinkBaseInfoRespDTO> baseLinkInfos = linkBatchCreateRespDTOResult.getData().getBaseLinkInfos();
            EasyExcelWebUtil.write(response, "批量创建短链接-SaaS短链接系统", LinkBaseInfoRespDTO.class, baseLinkInfos);
        }
    }

    //分页查询
    @GetMapping("link/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(LinkPageDTO linkPageDTO){
        return linkRemoteService.pageLink(linkPageDTO);
    }

    //短链接修改
    @PostMapping("link/update")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> linkUpdate(@RequestBody LinkUpdateDTO linkUpdateDTO) {
        linkRemoteService.linkUpdate(linkUpdateDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("修改成功");
    }
}
