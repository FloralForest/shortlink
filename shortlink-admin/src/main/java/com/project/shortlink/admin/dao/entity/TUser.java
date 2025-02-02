package com.project.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.time.LocalDateTime;
import java.io.Serializable;

import com.project.shortlink.admin.dao.entity.datebase.BaseDO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @author project
 * @since 2025-01-26
 */
@Data
//自动生成 equals() 和 hashCode() 方法，仅基于当前类的字段（默认callSuper = false）
@EqualsAndHashCode(callSuper = false)
//将 Setter 方法改为链式调用风格（默认chain = true）.setXXX().setXXX()
@Accessors(chain = true)
//@TableName注解对应其里面的表，但因为ShardingSphere框架的原因这里的"t_user"为逻辑表数据库中不存在
//做了数据库分表处理，操作时ShardingSphere框架会根据逻辑表去查询数据库的真实表
@TableName("t_user")
public class TUser extends BaseDO implements Serializable {

    private static final long serialVersionUID = 123456789L;

    /**
     * ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 姓名
     */
    private String realName;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 邮箱
     */
    private String mail;

    /**
     * 注销时间戳
     */
    private Long deletionTime;
}
