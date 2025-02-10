package com.project.shortlink.admin.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.remote.dto.req.LinkPageDTO;
import com.project.shortlink.admin.remote.dto.req.LinkRecycleBinPageDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;

/**
 * 回收站分页接口 （查询用户的所有分组gid下的已置入回收站的短链接）
 */
public interface RecycleBinService {

    Result<IPage<LinkPageRespDTO>> pageRecycleLink(LinkRecycleBinPageDTO linkPageDTO);
}
