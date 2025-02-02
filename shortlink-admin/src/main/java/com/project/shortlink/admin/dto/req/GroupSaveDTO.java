package com.project.shortlink.admin.dto.req;

import lombok.Data;

/**
 * 添加分组名请求参数
 */
@Data
public class GroupSaveDTO {

    /**
     * 分组名
     */
    private String name;
}
