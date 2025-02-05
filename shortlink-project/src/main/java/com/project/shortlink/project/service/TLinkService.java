package com.project.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.dao.entity.TLink;
import com.baomidou.mybatisplus.extension.service.IService;
import com.project.shortlink.project.dto.req.LinkCreateDTO;
import com.project.shortlink.project.dto.req.LinkPageDTO;
import com.project.shortlink.project.dto.req.LinkUpdateDTO;
import com.project.shortlink.project.dto.resp.LinkCountRespDTO;
import com.project.shortlink.project.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author project
 * @since 2025-02-02
 */
public interface TLinkService extends IService<TLink> {

    /**
     * 创建短板链接
     */
    LinkCreateRespDTO createLink(LinkCreateDTO linkCreateDTO);

    /**
     * 分页查询
     */
    IPage<LinkPageRespDTO> pageLink(LinkPageDTO linkPageDTO);

    /**
     * 分组下的短链接数量
     */
    List<LinkCountRespDTO> listLinkCount(List<String> listLink);

    /**
     * 短链接修改
     */
    void linkUpdate(LinkUpdateDTO linkUpdateDTO);
}
