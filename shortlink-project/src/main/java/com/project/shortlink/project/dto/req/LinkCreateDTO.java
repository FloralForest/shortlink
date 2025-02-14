package com.project.shortlink.project.dto.req;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
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
    private String domain;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 创建类型 0：控制台 1：接口
     */
    private Integer createdType;

    /**
     * 有效期类型 0：永久有效 1：用户自定义
     */
    private Integer validDateType;

    /**
     * 有效期
     * json序列化工具
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime validDate;

    /**
     * 描述
     */
    private String describe;
}
