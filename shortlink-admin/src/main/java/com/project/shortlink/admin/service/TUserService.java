package com.project.shortlink.admin.service;

import com.project.shortlink.admin.dao.entity.TUser;
import com.baomidou.mybatisplus.extension.service.IService;
import com.project.shortlink.admin.dto.req.UserRegisterDTO;
import com.project.shortlink.admin.dto.resp.UserRespDTO;

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
}
