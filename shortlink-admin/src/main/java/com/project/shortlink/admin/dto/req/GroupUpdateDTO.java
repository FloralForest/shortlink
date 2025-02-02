package com.project.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 修改组名请求参数
 */
@Data
public class GroupUpdateDTO {
    /**
     * 分组标识
     */
    private String gid;
    /**
     * 组名
     */
    private String name;
}
