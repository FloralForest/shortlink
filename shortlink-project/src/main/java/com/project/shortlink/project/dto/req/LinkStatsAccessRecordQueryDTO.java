package com.project.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 短链接监控访问记录(日志)的用于SQL查询的抽象对象参数
 */
@Data
//可以自动生成一个建造者模式相关的代码 使用生成的建造者模式来创建对象
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsAccessRecordQueryDTO {
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

    /**
     *查询的用户集合
     */
    private List<String> userList;

}

