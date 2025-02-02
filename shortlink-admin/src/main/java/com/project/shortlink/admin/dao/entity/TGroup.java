package com.project.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.time.LocalDateTime;

import com.project.shortlink.admin.dao.entity.datebase.BaseDO;
import lombok.*;
import lombok.experimental.Accessors;

/**
 * <p>
 * 
 * </p>
 *
 * @author project
 * @since 2025-01-29
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
@TableName("t_group")
public class TGroup extends BaseDO implements Serializable {

    private static final long serialVersionUID = 1234567891L;

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
     * 分组名称
     */
    @TableField("name")
    private String name;

    /**
     * 创建分组用户名
     */
    @TableField("username")
    private String username;

    /**
     * 分组排序
     */
    @TableField("sort_order")
    private Integer sortOrder;
}
