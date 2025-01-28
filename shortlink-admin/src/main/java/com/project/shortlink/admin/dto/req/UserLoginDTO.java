package com.project.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户登录需要参数
 */
@Data
public class UserLoginDTO {
    /**
     * 用户名
     */
    private String username;
    /**
     * 密码
     */
    private String password;
}
