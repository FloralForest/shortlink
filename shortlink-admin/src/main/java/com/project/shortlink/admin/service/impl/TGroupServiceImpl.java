package com.project.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.admin.common.biz.user.UserContext;
import com.project.shortlink.admin.common.convention.exception.ClientException;
import com.project.shortlink.admin.dao.entity.TGroup;
import com.project.shortlink.admin.dao.mapper.TGroupMapper;
import com.project.shortlink.admin.dto.req.GroupSaveDTO;
import com.project.shortlink.admin.dto.req.GroupSortDTO;
import com.project.shortlink.admin.dto.req.GroupUpdateDTO;
import com.project.shortlink.admin.dto.resp.GroupRespDTO;
import com.project.shortlink.admin.service.TGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.project.shortlink.admin.util.RandomGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

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

    /**
     * 新增短链接分组
     */
    @Override
    public void saveGroup(String groupName) {
        String gidRan;
        do {
            //查询生成的随机数是否重复
            gidRan = RandomGenerator.generateRandom(6);
        } while (!ifGid(gidRan));

        //TGroup使用@Builder注解
        final TGroup group = TGroup.builder()
                //工具类生成6位随机数
                .gid(gidRan)
                .name(groupName)
                .username(UserContext.getUsername())
                .sortOrder(0)
                .build();
        baseMapper.insert(group);
    }

    private boolean ifGid(String gidRan) {
        final LambdaQueryWrapper<TGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TGroup::getGid, gidRan)
                .eq(TGroup::getUsername, UserContext.getUsername())
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
        //将查询到的list拷贝到GroupRespDTO对象
        return BeanUtil.copyToList(groupList, GroupRespDTO.class);
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
