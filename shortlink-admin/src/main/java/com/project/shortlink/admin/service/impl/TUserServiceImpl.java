package com.project.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.admin.common.convention.exception.ClientException;
import com.project.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.project.shortlink.admin.dao.entity.TUser;
import com.project.shortlink.admin.dao.mapper.TUserMapper;
import com.project.shortlink.admin.dto.req.UserRegisterDTO;
import com.project.shortlink.admin.dto.resp.UserRespDTO;
import com.project.shortlink.admin.service.TUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import static com.project.shortlink.admin.common.constant.RedisCacheConstant.LOCK_USER_REGISTER_KEY;
import static com.project.shortlink.admin.common.enums.UserErrorCodeEnum.*;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author project
 * @since 2025-01-26
 */
@Service
@RequiredArgsConstructor//配合Lombok的构造器注入
public class TUserServiceImpl extends ServiceImpl<TUserMapper, TUser> implements TUserService {

    //布隆过滤器 com.project.shortlink.admin.common.config;
    private final RBloomFilter<String> userRegisterCachePenetrationBloomFilter;
    //redis分布式锁
    private final RedissonClient redissonClient;

    /**
     * 根据用户名返回信息
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        final LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //如果参数相同就是要查询的数据，如果不用lambda就需要写表的对应字段
        lambdaQueryWrapper.eq(TUser::getUsername, username);
        final TUser tUser = baseMapper.selectOne(lambdaQueryWrapper);
        final UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(tUser, result);//把tUser复制给result 只复制相同字段，不会多余复制或添加
        return result;
    }

    /**
     * 查看用户名是否可用 存在：true(用户名不可用) 不存在：false(用户名可用)
     */
    @Override
    public Boolean hasUsername(String username) {
        //常规
//        final LambdaQueryWrapper<TUser> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(TUser::getUsername, username);
//        //为空说明没有查询到数据，用户名可用
//        return baseMapper.selectOne(queryWrapper) == null;

        //布隆过滤器 如果布隆过滤器存在username（返回true），说明用户名不可以用
        //布隆过滤器 存在一定程度的误判
        //这里取反是反给前端看的，有值true，取反前端看到的是false说明用户名不可用
        return !userRegisterCachePenetrationBloomFilter.contains(username);
    }

    /**
     * 用户注册
     */
    @Override
    public void register(UserRegisterDTO userRegisterDTO) {
        //判断用户名
        if (!hasUsername(userRegisterDTO.getUsername())) {
            throw new ClientException(USER_EXIST);
        }
        //redis 分布式锁 预防大量恶意请求注册用户 （该场景为多用户同时注册同一个用户名）
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + userRegisterDTO.getUsername());
        try {
            //注册时一个用户名只由一个用户操作
            if (lock.tryLock()) {
                // 插入数据库（依赖唯一索引）
                baseMapper.insert(BeanUtil.toBean(userRegisterDTO, TUser.class));
                // 更新布隆过滤器
                userRegisterCachePenetrationBloomFilter.add(userRegisterDTO.getUsername());
            }
        } catch (DuplicateKeyException e) {
            // 捕获唯一键冲突异常（兜底）
            throw new ClientException(USER_SAVE_NAME_ERROR);
        }finally {
            //操作完成后释放锁
            lock.unlock();
        }
    }
}
