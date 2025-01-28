package com.project.shortlink.admin.common.enums;

import com.project.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_NULL("B000200", "用户不存在"),
    USER_EXIST("B000201", "用户已存在"),
    USER_SUCCESS("B000202", "查询成功"),
    USER_SUCCESS_ERROR("B000205", "查询失败，未查询到用户"),
    USER_SAVE_ERROR("B000203", "用户注册失败"),
    USER_SAVE_NAME_ERROR("B000204", "用户注册失败,请尝试更换用户名"),
    USER_LOGIN_ERROR("B000206", "登录失败，请检查用户名密码");


    private final String code;

    private final String message;

    UserErrorCodeEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String message() {
        return message;
    }
}
