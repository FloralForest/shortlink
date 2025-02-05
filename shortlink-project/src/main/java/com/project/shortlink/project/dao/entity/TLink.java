package com.project.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;

import com.project.shortlink.project.dao.entity.datebase.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author project
 * @since 2025-02-02
 */
@Data
//可以自动生成一个建造者模式相关的代码 使用生成的建造者模式来创建对象
@Builder
@NoArgsConstructor
@AllArgsConstructor
//自动生成 equals() 和 hashCode() 方法，仅基于当前类的字段（默认callSuper = false）
@EqualsAndHashCode(callSuper = false)
//将 Setter 方法改为链式调用风格（默认chain = true）.setXXX().setXXX()
@Accessors(chain = true)
//@TableName注解对应其里面的表，但因为ShardingSphere框架的原因这里的"t_user"为逻辑表数据库中不存在
//做了数据库分表处理，操作时ShardingSphere框架会根据逻辑表去查询数据库的真实表
@TableName("t_link")
public class TLink extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1234567892L;

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
     * 点击量
     */
    @TableField("click_num")
    private Integer clickNum;

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
     * 启用标识 （0：启用）（1：未启用）
     */
    @TableField("enable_status")
    private Integer enableStatus;

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
     */
    @TableField("valid_date")
    private LocalDateTime validDate;

    /**
     * 描述
     * 涉及到关键字使用``包起来
     */
    @TableField("`describe`")
    private String describe;

}
