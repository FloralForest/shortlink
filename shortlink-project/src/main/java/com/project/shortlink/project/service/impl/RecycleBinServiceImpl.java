package com.project.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.shortlink.project.dao.entity.TLink;
import com.project.shortlink.project.dao.mapper.TLinkMapper;
import com.project.shortlink.project.dto.req.*;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;
import com.project.shortlink.project.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import static com.project.shortlink.project.common.constant.RedisKeyConstant.GOTO_IS_NULL_SHORT_LINK_KEY;
import static com.project.shortlink.project.common.constant.RedisKeyConstant.GOTO_SHORT_LINK_KEY;

/**
 * 回收站业务实现
 */
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class RecycleBinServiceImpl extends ServiceImpl<TLinkMapper, TLink> implements RecycleBinService {

    private final StringRedisTemplate stringRedisTemplate;

    //修改短链接的启用标识
    @Override
    public void saveRecycleBin(RecycleBinSaveDTO recycleBinSaveDTO) {
        final LambdaUpdateWrapper<TLink> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(TLink::getFullShortUrl, recycleBinSaveDTO.getFullShortUrl())
                .eq(TLink::getGid, recycleBinSaveDTO.getGid())
                .eq(TLink::getEnableStatus, 0)
                .eq(TLink::getDelFlag, 0);

        final TLink tLink = TLink
                .builder()
                .enableStatus(1)
                .build();
        baseMapper.update(tLink, updateWrapper);
        //删除缓存
        stringRedisTemplate.delete(String.format(GOTO_SHORT_LINK_KEY, recycleBinSaveDTO.getFullShortUrl()));
    }

    //回收站分页
    //分页查询 配合DataBaseConfiguration工具类分页插件
    @Override
    public IPage<LinkPageRespDTO> pageLink(LinkRecycleBinPageDTO linkPageDTO) {
        final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                //查询多个值(多个gid)
                .in(TLink::getGid, linkPageDTO.getGidList())
                .eq(TLink::getEnableStatus, 1)
                .eq(TLink::getDelFlag, 0)
                //根据修改时间排序
                .orderByDesc(TLink::getUpdateTime);
        IPage<TLink> resultPage = baseMapper.selectPage(linkPageDTO, lambdaQueryWrapper);

        return resultPage.convert(page -> {
            final LinkPageRespDTO result = BeanUtil.toBean(page, LinkPageRespDTO.class);
            result.setDomain("http://" + result.getDomain());
            return result;
        });
    }

    //回收站恢复
    @Override
    public void recoverLink(RecycleBinRecoverDTO recycleBinRecoverDTO) {
        final LambdaUpdateWrapper<TLink> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper
                .eq(TLink::getFullShortUrl, recycleBinRecoverDTO.getFullShortUrl())
                .eq(TLink::getGid, recycleBinRecoverDTO.getGid())
                .eq(TLink::getEnableStatus, 1)
                .eq(TLink::getDelFlag, 0);

        final TLink tLink = TLink
                .builder()
                .enableStatus(0)
                .build();
        baseMapper.update(tLink, updateWrapper);
        //删除空白key(因为用户可能使用过已经置入回收站的短链接，导致短链接黑名单，这步是移出可能存在的黑名单短链接)
        stringRedisTemplate.delete(String.format(GOTO_IS_NULL_SHORT_LINK_KEY, recycleBinRecoverDTO.getFullShortUrl()));
    }

    //回收站删除
    @Override
    public void removeLink(RecycleBinRemoveDTO recycleBinRemoveDTO) {
        final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TLink::getFullShortUrl, recycleBinRemoveDTO.getFullShortUrl())
                .eq(TLink::getGid, recycleBinRemoveDTO.getGid())
                .eq(TLink::getEnableStatus, 1)
                .eq(TLink::getDelFlag, 0);
        //在实体类的delFlag字段上添加@TableLogic注解mybatis会自动寻找delFlag 逻辑删除
        baseMapper.delete(lambdaQueryWrapper);
    }
}
