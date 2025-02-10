package com.project.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

import java.util.List;

/**
 * 回收站短链接分页请求参数
 */
@Data
public class LinkRecycleBinPageDTO extends Page {

    /**
     * 分组标识，获得该组下的短链接
     */
    private List<String> gidList;
}
