package com.project.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 回收站保存
 */
@Data
public class RecycleBinSaveDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}


