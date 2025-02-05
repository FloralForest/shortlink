package com.project.shortlink.project.dto.resp;

import lombok.Data;

/**
 * 短链接数量
 */
@Data
public class LinkCountRespDTO {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 短链接数量
     */
    private Integer linkCount;
}

