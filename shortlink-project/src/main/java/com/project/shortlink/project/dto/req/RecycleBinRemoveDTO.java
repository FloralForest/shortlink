package com.project.shortlink.project.dto.req;

import lombok.Data;

/**
 * 回收站删除
 */
@Data
public class RecycleBinRemoveDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}


