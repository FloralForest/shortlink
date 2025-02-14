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
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;

    /**
     * 分组下短链接数量
     */
    private Integer linkCount;
}
