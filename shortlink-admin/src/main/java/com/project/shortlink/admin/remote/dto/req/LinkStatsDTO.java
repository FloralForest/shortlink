package com.project.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * 短链接监控请求参数
 */
@Data
public class LinkStatsDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     *查询的开始时间
     */
    private String startDate;

    /**
     *查询的结束时间
     */
    private String endDate;

}

