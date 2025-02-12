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

/**
 * <p>
 * 统计浏览器
 * </p>
 *
 * @author project
 * @since 2025-02-13
 */
@Data
//可以自动生成一个建造者模式相关的代码 使用生成的建造者模式来创建对象
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("t_link_browser_stats")
public class TLinkBrowserStats extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1234567896L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

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
     * 日期
     */
    @TableField("date")
    private Date date;

    /**
     * 访问量
     */
    @TableField("cnt")
    private Integer cnt;

    /**
     * 浏览器
     */
    @TableField("browser")
    private String browser;
}
