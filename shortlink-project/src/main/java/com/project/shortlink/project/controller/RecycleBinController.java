package com.project.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.common.convention.result.Result;
import com.project.shortlink.project.common.convention.result.Results;
import com.project.shortlink.project.dto.req.*;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;
import com.project.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 回收站控制层
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
@RequestMapping("/api/shortlink/project/")
public class RecycleBinController {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final RecycleBinService recycleBinService;

    //置入回收站
    @PostMapping("link/recycle/saveRB")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO){
        recycleBinService.saveRecycleBin(recycleBinSaveDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("已置入回收站");
    }

    //回收站分页
    @GetMapping("link/recycle/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(LinkRecycleBinPageDTO linkPageDTO) {
        return Results.success(recycleBinService.pageLink(linkPageDTO))
                .setCode("20000")
                .setMessage("查询成功");
    }

    //回收站恢复
    @PostMapping("link/recycle/recover")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> recoverLink(@RequestBody RecycleBinRecoverDTO recycleBinRecoverDTO) {
        recycleBinService.recoverLink(recycleBinRecoverDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("已移出回收站");
    }

    //删除回收站
    @PostMapping("link/recycle/remove")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> removeLink(@RequestBody RecycleBinRemoveDTO recycleBinRemoveDTO) {
        recycleBinService.removeLink(recycleBinRemoveDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("已从回收站删除");
    }
}

