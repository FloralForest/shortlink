package com.project.shortlink.project.dao.entity.datebase;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 数据库基础属性时间、逻辑删除
 */
@Data
public class BaseDO {
    /**
     * 创建时间
     */
    //标记 配合com.project.shortlink.admin.config.MyMetaObjectHandler;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /**
     * 修改时间
     */
    //标记 配合com.project.shortlink.admin.config.MyMetaObjectHandler;
    @TableField(fill =FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /**
     * 删除标识 0：未删除 1：已删除
     */
    //标记 配合com.project.shortlink.admin.config.MyMetaObjectHandler;
    @TableField(fill = FieldFill.INSERT)
    private Integer delFlag;
}
