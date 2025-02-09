package com.project.shortlink.admin.controller;

import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.remote.LinkRemoteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 获取原链接标题控制层
 */
@RestController
@RequestMapping("/api/shortlink/admin/")
public class UrlTitleController {
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};

    /**
     * 获取原链接标题
     */
    @GetMapping("link/title")
    //@RequestParam 从URL的查询字符串（如 ?name=John&age=20）或 POST 表单数据中获取参数值。
    public Result<String> getLinkTitle(@RequestParam("url") String url){
        return linkRemoteService.getLinkTitle(url);
    }
}
