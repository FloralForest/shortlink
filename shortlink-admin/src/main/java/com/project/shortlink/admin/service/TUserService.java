package com.project.shortlink.admin.service;

import com.project.shortlink.admin.dao.entity.TUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.project.shortlink.admin.dto.req.UserLoginDTO;
import com.project.shortlink.admin.dto.req.UserRegisterDTO;
import com.project.shortlink.admin.dto.req.UserUpdateDTO;
import com.project.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.project.shortlink.admin.dto.resp.UserRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * <p>
 * 用户表 服务类
 * </p>
 *
 * @author project
 * @since 2025-01-26
 */
public interface TUserService extends IService<TUser> {
    /**
     * 根据用户名返回信息
     */
    UserRespDTO getUserByUsername (String username);

    /**
     * 查看用户名是否可用 存在：false 不存在：true
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     */
    void register(UserRegisterDTO userRegisterDTO);

    /**
     * 修改用户
     */
    void updateUser(UserUpdateDTO userUpdateDTO);

    /**
     * 用户登录
     */
    UserLoginRespDTO login(UserLoginDTO userLoginDTO);

    /**
     * 检查用户是否登录
     */
    Boolean checkLogin(String username, String token);

    /**
     * 退出登录
     */
    void logout(String username, String token);
}
