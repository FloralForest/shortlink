package com.project.shortlink.project.dto.resp;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Date;

/**
 * 短链接分页响应参数
 */
@Data
public class LinkPageRespDTO {

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 域名
     */
    @TableField("domain")
    private String domain;

    /**
     * 短链接
     */
    @TableField("short_uri")
    private String shortUri;

    /**
     * 完整短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @TableField("origin_url")
    private String originUrl;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 网站图片
     */
    @TableField("favicon")
    private String favicon;

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
     * 创建时间
     * json序列化工具
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;
    /**
     * 描述
     * 涉及到关键字使用``包起来
     */
    @TableField("`describe`")
    private String describe;

}
