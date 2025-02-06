package com.project.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 短链接请求参数
 */
@Data
//可以自动生成一个建造者模式相关的代码 使用生成的建造者模式来创建对象
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkCreateDTO {

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
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 创建类型 0：控制台 1：接口
     */
    @TableField("created_type")
    private Integer createdType;

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
