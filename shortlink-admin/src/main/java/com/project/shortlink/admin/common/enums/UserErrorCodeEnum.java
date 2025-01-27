package com.project.shortlink.admin.common.enums;

import com.project.shortlink.admin.common.convention.errorcode.IErrorCode;

public enum UserErrorCodeEnum implements IErrorCode {
    USER_NULL("B000200", "用户不存在"),
    USER_EXIST("B000201", "用户已存在"),
    USER_SUCCESS("B000202", "查询成功");


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
