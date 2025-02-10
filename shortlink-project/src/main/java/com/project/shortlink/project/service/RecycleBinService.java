package com.project.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.project.shortlink.project.dao.entity.TLink;
import com.project.shortlink.project.dto.req.*;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * 回收站接口
 */
public interface RecycleBinService extends IService<TLink> {

    /**
     * 置入回收站
     */
    void saveRecycleBin(RecycleBinSaveDTO recycleBinSaveDTO);

    /**
     * 分页查询
     */
    IPage<LinkPageRespDTO> pageLink(LinkRecycleBinPageDTO linkPageDTO);

    /**
     * 回收站恢复
     */
    void recoverLink(RecycleBinRecoverDTO recycleBinRecoverDTO);

    /**
     * 回收站删除
     */
    void removeLink(RecycleBinRemoveDTO recycleBinRemoveDTO);
}
