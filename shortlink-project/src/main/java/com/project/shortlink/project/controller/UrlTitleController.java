package com.project.shortlink.project.controller;

import com.project.shortlink.project.common.convention.result.Result;
import com.project.shortlink.project.common.convention.result.Results;
import com.project.shortlink.project.service.TLinkService;
import com.project.shortlink.project.service.UrlTitleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取原链接标题控制层
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
@RequestMapping("/api/shortlink/project/")
public class UrlTitleController {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final UrlTitleService urlTitleService;

    /**
     * 获取原链接标题
     */
    @GetMapping("link/title")
    //@RequestParam 从URL的查询字符串（如 ?name=John&age=20）或 POST 表单数据中获取参数值。
    public Result<String> getLinkTitle(@RequestParam("url") String url){
        return Results.success(urlTitleService.getLinkTitle(url));
    }
}
