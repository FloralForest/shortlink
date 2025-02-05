package com.project.shortlink.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.project.shortlink.admin.common.convention.exception.ClientException;
import com.project.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.project.shortlink.admin.dao.entity.TUser;
import com.project.shortlink.admin.dao.mapper.TUserMapper;
import com.project.shortlink.admin.dto.req.UserLoginDTO;
import com.project.shortlink.admin.dto.req.UserRegisterDTO;
import com.project.shortlink.admin.dto.req.UserUpdateDTO;
import com.project.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.project.shortlink.admin.dto.resp.UserRespDTO;
import com.project.shortlink.admin.service.TGroupService;
import com.project.shortlink.admin.service.TUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RBloomFilter;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

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
    //redis 依赖
    private final StringRedisTemplate stringRedisTemplate;
    private final TGroupService tGroupService;

    /**
     * 根据用户名返回信息
     */
    @Override
    public UserRespDTO getUserByUsername(String username) {
        try {
            final LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            //如果参数相同就是要查询的数据，如果不用lambda就需要写表的对应字段
            lambdaQueryWrapper.eq(TUser::getUsername, username);
            //查询
            final TUser tUser = baseMapper.selectOne(lambdaQueryWrapper);
            final UserRespDTO result = new UserRespDTO();
            //把tUser复制给result 只复制相同字段，不会多余复制或添加
            BeanUtils.copyProperties(tUser, result);
            return result;
        } catch (IllegalArgumentException e) {
            throw new ClientException(USER_SUCCESS_ERROR);
        }
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
        if (!hasUsername(userRegisterDTO.getUsername()))
            throw new ClientException(USER_EXIST);

        //redis 分布式锁 预防大量恶意请求注册用户 （该场景为多用户同时注册同一个用户名）
        RLock lock = redissonClient.getLock(LOCK_USER_REGISTER_KEY + userRegisterDTO.getUsername());
        try {
            //注册时一个用户名只由一个用户操作
            if (lock.tryLock()) {
                // 插入数据库（依赖唯一索引）
                baseMapper.insert(BeanUtil.toBean(userRegisterDTO, TUser.class));
                // 更新布隆过滤器
                userRegisterCachePenetrationBloomFilter.add(userRegisterDTO.getUsername());
                //创建用户后默认添加短链接分组
                tGroupService.saveGroup("新建分组", userRegisterDTO.getUsername());
            }
        } catch (DuplicateKeyException e) {
            // 捕获唯一键冲突异常（兜底）
            throw new ClientException(USER_SAVE_NAME_ERROR);
        } finally {
            //操作完成后释放锁
            lock.unlock();
        }
    }

    /**
     * 修改用户
     */
    @Override
    public void updateUser(UserUpdateDTO userUpdateDTO) {
        final LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        //根据对象查询到对应用户
        lambdaQueryWrapper.eq(TUser::getUsername, userUpdateDTO.getUsername());
        //将修改对象转为 TUser实体，然后修改lambdaQueryWrapper查询到的TUser
        baseMapper.update(BeanUtil.toBean(userUpdateDTO, TUser.class), lambdaQueryWrapper);
    }

    /**
     * 用户登录
     */
    @Override
    public UserLoginRespDTO login(UserLoginDTO userLoginDTO) {
        //根据用户名和密码查询用户，并且未逻辑删除
        final LambdaQueryWrapper<TUser> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper
                .eq(TUser::getUsername, userLoginDTO.getUsername())
                .eq(TUser::getPassword, userLoginDTO.getPassword())
                .eq(TUser::getDelFlag, 0);
        final TUser tUser = baseMapper.selectOne(lambdaQueryWrapper);
        //查询 判断
        if (tUser == null)
            throw new ClientException("用户不存在或密码错误");

        //重复登录判断
        final Boolean aBoolean = stringRedisTemplate.hasKey("login_" + userLoginDTO.getUsername());
        if (aBoolean != null && aBoolean)
            throw new ClientException("已登录，请勿重复操作");
        /* 解决重复登录的问题
         * Hash
         * Key: login_用户名
         * Value:
         *      Key: token 标识
         *      Value: JSON 用户信息
         */
        //用户存在 存入redis 生成一个UUID唯一值当作token 设置5小时的有效期
        final String uuid = UUID.randomUUID().toString();
        stringRedisTemplate.opsForHash().put(
                "login_" + userLoginDTO.getUsername(),
                uuid,
                JSON.toJSONString(tUser));
        //设置过期时间
        stringRedisTemplate.expire("login_" + userLoginDTO.getUsername(), 5L, TimeUnit.HOURS);
        //生成的uuid作为token
        return new UserLoginRespDTO(uuid);

    }

    /**
     * 检查用户是否登录
     */
    @Override
    public Boolean checkLogin(String username, String token) {
        //检查redis是否有该key
        return stringRedisTemplate.opsForHash().get("login_" + username, token) != null;
    }

    /**
     * 退出登录
     */
    @Override
    public void logout(String username, String token) {
        if (!checkLogin(username, token))
            throw new ClientException("用户未登录或用户异常");
        stringRedisTemplate.delete("login_" + username);
    }
}
