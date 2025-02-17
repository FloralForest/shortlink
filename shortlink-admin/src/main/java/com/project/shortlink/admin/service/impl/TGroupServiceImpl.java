package com.project.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.admin.common.biz.user.UserContext;
import com.project.shortlink.admin.common.convention.exception.ClientException;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.dao.entity.TGroup;
import com.project.shortlink.admin.dao.mapper.TGroupMapper;
import com.project.shortlink.admin.dto.req.GroupSaveDTO;
import com.project.shortlink.admin.dto.req.GroupSortDTO;
import com.project.shortlink.admin.dto.req.GroupUpdateDTO;
import com.project.shortlink.admin.dto.resp.GroupRespDTO;
import com.project.shortlink.admin.dto.resp.LinkCountRespDTO;
import com.project.shortlink.admin.remote.LinkRemoteService;
import com.project.shortlink.admin.service.TGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.shortlink.admin.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

import static com.project.shortlink.admin.common.constant.RedisCacheConstant.LOCK_GROUP_CREATE_KEY;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author project
 * @since 2025-01-29
 */
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class TGroupServiceImpl extends ServiceImpl<TGroupMapper, TGroup> implements TGroupService {

    final LinkRemoteService linkRemoteService = new LinkRemoteService(){};
    private final RedissonClient redissonClient;
    @Value("${short-link.group.max}")
    private Integer groupMax;
    private String name;
    /**
     * 新增短链接分组 兼顾用户注册时系统默认分组和用户自定义分组使用可变参数优化
     */
    @Override
    public void saveGroup(String groupName, String... username) {
        String gidRan;
        name = (username != null && username.length > 0)
                ? username[0]
                : UserContext.getUsername();
        //添加分布式锁，限制用户创建分组数（20个）防止同一用户在多个设备同时操作
        final RLock lock = redissonClient.getLock(String.format(LOCK_GROUP_CREATE_KEY, name));
        lock.lock();
        try{
            final LambdaQueryWrapper<TGroup> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(TGroup::getUsername, name).eq(TGroup::getDelFlag, 0);
            final List<TGroup> groups = baseMapper.selectList(wrapper);
            if (CollUtil.isNotEmpty(groups) && groups.size() == groupMax){
                throw new ClientException(String.format("以达到最大分组数：%d", groupMax));
            }
            do {
                //查询生成的随机数是否重复
                gidRan = RandomGenerator.generateRandom(6);
            } while (!ifGid(gidRan));

            //TGroup使用@Builder注解
            final TGroup group = TGroup.builder()
                    //工具类生成6位随机数
                    .gid(gidRan)
                    .name(groupName)
                    .username(name)
                    .sortOrder(0)
                    .build();
            baseMapper.insert(group);
        }finally {
            lock.unlock();
        }
    }

    private boolean ifGid(String gidRan) {
        final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TGroup::getGid, gidRan)
                .eq(TGroup::getUsername, name)
                .eq(TGroup::getDelFlag, 0);
        //存在返回false
        return baseMapper.selectOne(lambdaQueryWrapper) == null;
    }

    /**
     * 查询分组
     */
    @Override
    public List<GroupRespDTO> listGroup() {
        final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TGroup::getDelFlag, 0)
                .eq(TGroup::getUsername, UserContext.getUsername())
                .orderByDesc(TGroup::getSortOrder, TGroup::getUpdateTime);
        final List<TGroup> groupList = baseMapper.selectList(lambdaQueryWrapper);
        //获取分组下的短链接个数
        final Result<List<LinkCountRespDTO>> listResult =
                linkRemoteService.listLinkCount(groupList.stream().map(TGroup::getGid).toList());
        //将查询到的list拷贝到GroupRespDTO对象
        final List<GroupRespDTO> copyToList = BeanUtil.copyToList(groupList, GroupRespDTO.class);
        copyToList.forEach(each ->{
            final Optional<LinkCountRespDTO> first = listResult.getData().stream()
                    .filter(item -> Objects.equals(item.getGid(), each.getGid()))
                    .findFirst();
            first.ifPresent(item -> each.setLinkCount(first.get().getLinkCount()));
        });

        return copyToList;
    }

    /**
     * 修改组名
     */
    @Override
    public void updateGroup(GroupUpdateDTO groupUpdateDTO) {
        final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                //当前登录用户名和目标组ID
                .eq(TGroup::getUsername, UserContext.getUsername())
                .eq(TGroup::getGid, groupUpdateDTO.getGid())
                .eq(TGroup::getDelFlag, 0);
        final TGroup tGroup = new TGroup();
        tGroup.setName(groupUpdateDTO.getName());
        baseMapper.update(tGroup, lambdaQueryWrapper);
    }

    /**
     * 删除组
     */
    @Override
    public void deleteGroup(String gid) {
        //逻辑删除(软删除)
        final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                //当前登录用户名和目标组ID
                .eq(TGroup::getUsername, UserContext.getUsername())
                .eq(TGroup::getGid, gid)
                .eq(TGroup::getDelFlag, 0);
        final TGroup tGroup = new TGroup();
        tGroup.setDelFlag(1);
        baseMapper.update(tGroup, lambdaQueryWrapper);
    }

    /**
     * 排序(前端做排序，这里本质上还是修改sortOrder的值)
     */
    @Override
    public void sortGroup(List<GroupSortDTO> groupSortDTOList) {
        groupSortDTOList.forEach(sort -> {
            TGroup tGroup = TGroup.builder()
                    .sortOrder(sort.getSortOrder())
                    .build();
            final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper
                    .eq(TGroup::getUsername, UserContext.getUsername())
                    .eq(TGroup::getGid, sort.getGid())
                    .eq(TGroup::getDelFlag, 0);
            baseMapper.update(tGroup, lambdaQueryWrapper);
        });
    }
}
