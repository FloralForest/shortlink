package com.project.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import com.project.shortlink.project.dao.entity.datebase.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 每日统计表
 * </p>
 *
 * @author project
 * @since 2025-02-15
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
@TableName("t_link_stats_today")
public class TLinkStatsToday extends BaseDO implements Serializable {

    private static final long serialVersionUID = 12345678910L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 分组标识
     */
    @TableField("gid")
    private String gid;

    /**
     * 短链接
     */
    @TableField("full_short_url")
    private String fullShortUrl;

    /**
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 今日PV
     */
    @TableField("today_pv")
    private Integer todayPv;

    /**
     * 今日UV
     */
    @TableField("today_uv")
    private Integer todayUv;

    /**
     * 今日IP数
     */
    @TableField("today_uip")
    private Integer todayUip;
}
