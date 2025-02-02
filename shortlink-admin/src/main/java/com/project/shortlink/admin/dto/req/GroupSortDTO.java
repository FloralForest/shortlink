package com.project.shortlink.admin.dto.req;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

/**
 * 组排序请求参数
 */
@Data
public class GroupSortDTO {
    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组排序
     */
    private Integer sortOrder;
}
