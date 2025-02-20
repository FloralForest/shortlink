package com.project.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.shortlink.admin.common.biz.user.UserContext;
import com.project.shortlink.admin.common.convention.exception.ServiceException;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.dao.entity.TGroup;
import com.project.shortlink.admin.dao.mapper.TGroupMapper;
import com.project.shortlink.admin.remote.LinkActuaRemoteService;
import com.project.shortlink.admin.remote.LinkRemoteService;
import com.project.shortlink.admin.remote.dto.req.LinkRecycleBinPageDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import com.project.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 回收站分页接口 （查询用户的所有分组gid下的已置入回收站的短链接）
 */
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class RecycleBinImpl implements RecycleBinService {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final TGroupMapper tGroupMapper;
    //SpringCloud调用
    private final LinkActuaRemoteService linkActuaRemoteService;
    //传统调用
    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};

    @Override
    public Result<Page<LinkPageRespDTO>> pageRecycleLink(LinkRecycleBinPageDTO linkPageDTO) {
        final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TGroup::getUsername, UserContext.getUsername())
                .eq(TGroup::getDelFlag,0);
        final List<TGroup> tGroups = tGroupMapper.selectList(lambdaQueryWrapper);
        if (CollUtil.isEmpty(tGroups)){
            throw new ServiceException("用户无分组");
        }
        //转换
        linkPageDTO.setGidList(tGroups.stream().map(TGroup::getGid).toList());
        return linkActuaRemoteService.pageRecycleLink(
                linkPageDTO.getGidList(),
                linkPageDTO.getCurrent(),
                linkPageDTO.getSize());
    }
}
