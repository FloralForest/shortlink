package com.project.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.admin.dao.entity.TUser;
import com.project.shortlink.admin.dao.mapper.TUserMapper;
import com.project.shortlink.admin.dto.resp.UserRespDTO;
import com.project.shortlink.admin.service.TUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author project
 * @since 2025-01-26
 */
@Service
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements TUserService {

    @Override
    public UserRespDTO getUserByUsername(String username) {
        final LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //如果参数相同就是要查询的数据，如果不用lambda就需要写表的对应字段
        lambdaQueryWrapper.eq(TUser::getUsername,username);
        final TUser tUser = baseMapper.selectOne(lambdaQueryWrapper);
        final UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(tUser, result);//把tUser复制给result 只复制相同字段，不会多余复制或添加
        return result;
    }
}
