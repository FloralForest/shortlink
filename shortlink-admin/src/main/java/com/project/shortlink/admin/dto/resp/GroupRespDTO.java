package com.project.shortlink.admin.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 分组返回实体
 */
@Data
public class GroupRespDTO {
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

    /**
     * 分组下短链接数量
     */
    private Integer linkCount;
}
