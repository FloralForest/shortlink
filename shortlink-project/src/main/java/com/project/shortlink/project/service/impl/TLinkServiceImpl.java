package com.project.shortlink.project.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.project.common.convention.exception.ServiceException;
import com.project.shortlink.project.dao.entity.TLink;
import com.project.shortlink.project.dao.mapper.TLinkMapper;
import com.project.shortlink.project.dto.req.LinkCreateDTO;
import com.project.shortlink.project.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.project.service.TLinkService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.shortlink.project.util.HashUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author project
 * @since 2025-02-02
 */
@Slf4j
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class TLinkServiceImpl extends ServiceImpl<TLinkMapper, TLink> implements TLinkService {

    //布隆过滤器
    private final RBloomFilter<String> shorUriCreateCachePenetrationBloomFilter;

    //创建短链接
    @Override
    public LinkCreateRespDTO createLink(LinkCreateDTO linkCreateDTO) {
        //生成的短链
        final String suffix = generateSuffix(linkCreateDTO);
        //与域名拼接的完整短链接
        final String fullShorUrl = linkCreateDTO.getDomain() + "/" + suffix;
        final TLink tLink = TLink
                .builder()
                .domain(linkCreateDTO.getDomain())
                .originUrl(linkCreateDTO.getOriginUrl())
                .gid(linkCreateDTO.getGid())
                .createdType(linkCreateDTO.getCreatedType())
                .validDateType(linkCreateDTO.getValidDateType())
                .validDate(linkCreateDTO.getValidDate())
                .describe(linkCreateDTO.getDescribe())
                //添加短链
                .shortUri(suffix)
                .enableStatus(0)
                //完整链接(域名+生成的短链接)
                .fullShortUrl(fullShorUrl)
                .build();
        try {
            //如果数据库添加失败，抛出异常
            baseMapper.insert(tLink);
        }catch (DuplicateKeyException e){
            //进一步查询数据库判断是否真的存在
            final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(TLink::getFullShortUrl, fullShorUrl);
            final TLink istLink = baseMapper.selectOne(lambdaQueryWrapper);
            if (istLink != null) {
                log.warn("短链接：{} 重复", fullShorUrl);
                throw new ServiceException("短链接重复");
            }
        }
        //添加缓存
        shorUriCreateCachePenetrationBloomFilter.add(suffix);
        return LinkCreateRespDTO
                .builder()
                .fullShortUrl(tLink.getFullShortUrl())
                .originUrl(linkCreateDTO.getOriginUrl())
                .gid(linkCreateDTO.getGid())
                .build();
    }

    //原链接生成62进制的短链接
    private String generateSuffix(LinkCreateDTO linkCreateDTO) {
        //确保短链的唯一性
        //设置最大循环次数
        int custom = 0;
        String shorUri;
        while (true) {
            if (custom > 10) {
                throw new ServiceException("短链频繁生成，稍后再试");
            }
            //原链接
            String originUrl = linkCreateDTO.getOriginUrl();
            //添加一个当前毫秒数，尽量降低冲突
            originUrl += System.currentTimeMillis();
            shorUri = HashUtil.hashToBase(originUrl);
//            //查询是否有生成的短链
//            final LambdaQueryWrapper<TLink> lambdaQueryWrapper = new LambdaQueryWrapper<>();
//            lambdaQueryWrapper.eq(TLink::getFullShortUrl, linkCreateDTO.getDomain() + "/" + shorUri);
//            final TLink tLink = baseMapper.selectOne(lambdaQueryWrapper);
//            if (tLink == null){
//                break;
//            }
            //布隆过滤器判断 如果不存在break退出循环（添加），布隆过滤器存在误判
            if (!shorUriCreateCachePenetrationBloomFilter.contains(linkCreateDTO.getDomain() + "/" + shorUri)) {
                break;
            }
            custom++;
        }

        //返回生成的链接
        return shorUri;
    }
}
