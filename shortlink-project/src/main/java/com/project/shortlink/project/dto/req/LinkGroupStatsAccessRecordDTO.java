package com.project.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.shortlink.project.dao.entity.TLinkAccessLogs;
import lombok.Data;

/**
 * 访问分组短链接监控访问记录(日志)请求参数 + 分页
 */
@Data
public class LinkGroupStatsAccessRecordDTO extends Page<TLinkAccessLogs> {
    /**
     * 分组标识
     */
    private String gid;

    /**
     *查询的开始时间
     */
    private String startDate;

    /**
     *查询的结束时间
     */
    private String endDate;

}

