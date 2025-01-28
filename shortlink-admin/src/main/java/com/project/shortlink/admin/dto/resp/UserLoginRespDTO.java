package com.project.shortlink.admin.dto.resp;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户登录返回token
 */
@Data
//构造器全参 无参
@AllArgsConstructor
@NoArgsConstructor
public class UserLoginRespDTO {
    /**
     * token
     */
    private String token;
}
