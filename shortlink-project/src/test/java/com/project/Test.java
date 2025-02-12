package com.project;

import cn.hutool.core.util.StrUtil;

public class Test {

    @org.junit.jupiter.api.Test
    public void test(){
        String furl = "baidu.com/3V21X8";
        System.out.println(StrUtil.sub(furl, furl.indexOf("/"), furl.length()));
    }
}
