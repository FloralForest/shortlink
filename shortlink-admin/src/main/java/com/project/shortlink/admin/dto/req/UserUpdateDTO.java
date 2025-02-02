package com.project.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 用户修改请求参数
 */
@Data
public class UserUpdateDTO {

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

}
