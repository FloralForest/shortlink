package com.project.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import lombok.Data;

/**
 * 短链接分页请求参数
 */
@Data
public class LinkPageDTO extends Page {

    /**
     * 分组标识，获得该组下的短链接
     */
    private String gid;
}
