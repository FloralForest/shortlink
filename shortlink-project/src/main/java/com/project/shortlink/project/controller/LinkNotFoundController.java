package com.project.shortlink.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * 短链接不存在跳转控制器
 */
@Controller
public class LinkNotFoundController {

    /**
     * 短链接不存在跳转页面
     */
    @RequestMapping("link/notfound")
    public String notfound() {
        return "notfound";
    }
}
