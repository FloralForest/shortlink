package com.project.shortlink.project.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 短链接修改请求实体
 */
@Data
public class LinkUpdateDTO {
    /**
     * 域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 原始链接
     */
    @TableField("origin_url")
    private String originUrl;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    @TableField("valid_date_type")
    private Integer validDateType;

    /**
     * 有效期
     * json序列化工具
     */
    @TableField("valid_date")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validDate;

    /**
     * 描述
     */
    @TableField("describe")
    private String describe;
}
