package com.project.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.common.convention.result.Result;
import com.project.shortlink.project.common.convention.result.Results;
import com.project.shortlink.project.dto.req.LinkCreateDTO;
import com.project.shortlink.project.dto.req.LinkPageDTO;
import com.project.shortlink.project.dto.req.LinkUpdateDTO;
import com.project.shortlink.project.dto.resp.LinkCountRespDTO;
import com.project.shortlink.project.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;
import com.project.shortlink.project.service.TLinkService;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 功能实现：域名+hash工具类生成的短链（组合成完整短链接）跳转到 长的原始链接
 * <p>
 * 前端控制器
 * </p>
 *
 * @author project
 * @since 2025-02-02
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
@RequestMapping("/api/shortlink/project/")
public class TLinkController {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final TLinkService tLinkService;

    //创建短链接
    @PostMapping("link/createLink")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateDTO linkCreateDTO) {
        return Results.success(tLinkService.createLink(linkCreateDTO))
                .setCode("20000")
                .setMessage("短链接创建成功");
    }

    //分页
    @GetMapping("link/page")
    public Result<IPage<LinkPageRespDTO>> pageLink(LinkPageDTO linkPageDTO) {
        return Results.success(tLinkService.pageLink(linkPageDTO))
                .setCode("20000")
                .setMessage("查询成功");
    }

    //分组内的短链接数量
    @GetMapping("link/count")
    //@RequestParam 从URL的查询字符串（如 ?name=John&age=20）或 POST 表单数据中获取参数值。
    public Result<List<LinkCountRespDTO>> listLinkCount(@RequestParam("number") List<String> gidNumber) {
        return Results.success(tLinkService.listLinkCount(gidNumber))
                .setCode("20000")
                .setMessage("查询成功");
    }

    //短链接修改
    @PostMapping("link/update")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> linkUpdate(@RequestBody LinkUpdateDTO linkUpdateDTO) {
        tLinkService.linkUpdate(linkUpdateDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("修改成功");
    }

    //实现短链接跳转(重定向到原链接)
    @GetMapping("{linkUri}")
    //@PathVariable 从URL获取值
    public void linkUri(@PathVariable("linkUri") String linkUri,
                        ServletRequest request,
                        ServletResponse response){
        //短链接 请求 响应
        tLinkService.linkUri(linkUri, request, response);
    }
}
