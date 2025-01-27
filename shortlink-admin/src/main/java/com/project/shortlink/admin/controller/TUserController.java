package com.project.shortlink.admin.controller;


import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.project.shortlink.admin.dao.entity.TUser;
import com.project.shortlink.admin.dto.resp.UserRespDTO;
import com.project.shortlink.admin.service.TUserService;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <p>
 * 用户表 前端控制器
 * </p>
 *
 * @author project
 * @since 2025-01-26
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
@RequestMapping("/api/shortlink/admin/")
public class TUserController {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final TUserService tUserService;

    @GetMapping("user/All")
    public Result<List<TUser>> userAll(){
        return Results.success(tUserService.list())
                .setCode(UserErrorCodeEnum.USER_SUCCESS.code())
                .setMessage(UserErrorCodeEnum.USER_SUCCESS.message());
    }

    @GetMapping("user/{username}")
    //@PathVariable 从请求链接获取值
    public Result<UserRespDTO> userName(@PathVariable("username") String username){
        return Results.success(tUserService.getUserByUsername(username))
                .setCode(UserErrorCodeEnum.USER_SUCCESS.code())
                .setMessage(UserErrorCodeEnum.USER_SUCCESS.message());
    }
}

