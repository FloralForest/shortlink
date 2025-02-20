package com.project.shortlink.admin.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.remote.LinkActuaRemoteService;
import com.project.shortlink.admin.remote.dto.req.RecycleBinSaveDTO;
import com.project.shortlink.admin.remote.LinkRemoteService;
import com.project.shortlink.admin.remote.dto.req.LinkRecycleBinPageDTO;
import com.project.shortlink.admin.remote.dto.req.RecycleBinRecoverDTO;
import com.project.shortlink.admin.remote.dto.req.RecycleBinRemoveDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import com.project.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
@RequestMapping("/api/shortlink/admin/")
public class RecycleBinController {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final RecycleBinService recycleBinService;

    //SpringCloud调用
    private final LinkActuaRemoteService linkActuaRemoteService;

    //传统调用
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};

    @PostMapping("link/recycle/saveRB")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO){
        linkActuaRemoteService.saveRecycleBin(recycleBinSaveDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("已置入回收站");
    }

    //回收站分页
    @GetMapping("link/recycle/page")
    public Result<Page<LinkPageRespDTO>> pageLink(LinkRecycleBinPageDTO linkPageDTO) {
        return recycleBinService.pageRecycleLink(linkPageDTO);
    }

    //回收站恢复
    @PostMapping("link/recycle/recover")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> recoverLink(@RequestBody RecycleBinRecoverDTO recycleBinRecoverDTO) {
        linkActuaRemoteService.recoverLink(recycleBinRecoverDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("已移出回收站");
    }

    //删除回收站
    @PostMapping("link/recycle/remove")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> removeLink(@RequestBody RecycleBinRemoveDTO recycleBinRemoveDTO) {
        linkActuaRemoteService.removeLink(recycleBinRemoveDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("已从回收站删除");
    }
}

