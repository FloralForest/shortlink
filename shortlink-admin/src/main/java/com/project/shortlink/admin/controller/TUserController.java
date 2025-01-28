package com.project.shortlink.admin.controller;


import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.dao.entity.TUser;
import com.project.shortlink.admin.dto.req.UserLoginDTO;
import com.project.shortlink.admin.dto.req.UserRegisterDTO;
import com.project.shortlink.admin.dto.req.UserUpdateDTO;
import com.project.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.project.shortlink.admin.dto.resp.UserRespDTO;
import com.project.shortlink.admin.service.TUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.project.shortlink.admin.common.enums.UserErrorCodeEnum.USER_LOGIN_ERROR;

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

    //查询全部
    @GetMapping("user/All")
    public Result<List<TUser>> userAll() {
        return Results.success(tUserService.list())
                .setCode("20000")
                .setMessage("查询成功！");
    }

    //根据用户名查询
    @GetMapping("user/{username}")
    //@PathVariable 从URL获取username值
    public Result<UserRespDTO> userName(@PathVariable("username") String username) {
        return Results.success(tUserService.getUserByUsername(username))
                .setCode("20000")
                .setMessage("查询成功！");
    }

    //查询用户名是否存在
    @GetMapping("user/isUsername")
    //@RequestParam 从URL的查询字符串（如 ?name=John&age=20）或 POST 表单数据中获取参数值。
    public Result<Boolean> ifUsername(@RequestParam("username") String username) {
        return Results.success(tUserService.hasUsername(username))
                .setCode("20000")
                .setMessage("查询成功！");
    }

    //注册用户
    @PostMapping("user/register")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> register(@RequestBody UserRegisterDTO userRegisterDTO) {
        tUserService.register(userRegisterDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("注册成功！");
    }

    //修改用户
    @PutMapping("user/update")
    public Result<Void> updateUser(@RequestBody UserUpdateDTO userUpdateDTO) {
        tUserService.updateUser(userUpdateDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("修改成功！");
    }

    //用户登录
    @PostMapping("user/login")
    public Result<UserLoginRespDTO> userLogin(@RequestBody UserLoginDTO userLoginDTO) {
        return Results.success(tUserService.login(userLoginDTO))
                .setCode("20000")
                .setMessage("登录成功！");
    }
    //检查用户是否登录
    @GetMapping("user/checkLogin")
    //@RequestParam 从URL的查询字符串（如 ?name=John&age=20）或 POST 表单数据中获取参数值。
    public Result<Boolean> checkLogin(@RequestParam("username") String username,
                                      @RequestParam("token") String token){
        final Boolean aBoolean = tUserService.checkLogin(username, token);
        if (aBoolean)
            return Results.success(true)
                    .setCode("20000")
                    .setMessage("用户已登录");
        return Results.success(false)
                .setCode("20001")
                .setMessage("用户未登录");
    }

    //退出登录
    @DeleteMapping("user/logout")
    public Result<Void> logout(@RequestParam("username") String username,
                                @RequestParam("token") String token){
        tUserService.logout(username, token);
        return Results.success().setCode("20000").setMessage("已退出登录");
    }
}

