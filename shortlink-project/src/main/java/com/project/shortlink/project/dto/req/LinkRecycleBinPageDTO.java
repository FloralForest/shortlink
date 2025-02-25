package com.project.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.shortlink.project.dao.entity.TLink;
import lombok.Data;

import java.util.List;

/**
 * 回收站短链接分页请求参数
 */
@Data
public class LinkRecycleBinPageDTO extends Page<TLink> {

    /**
     * 分组标识，获得该组下的短链接
     */
    private List<String> gidList;
}
