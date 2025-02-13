package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkDeviceStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dao.entity.TLinkOsStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 统计访问设备 Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-13
 */
@Mapper
public interface TLinkDeviceStatsMapper extends BaseMapper<TLinkDeviceStats> {

    //短链接跳转统计SQL
    @Insert("INSERT INTO t_link_device_stats ( full_short_url, gid, date, cnt, device, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.cnt}, #{ls.device}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{ls.cnt};")
    void shortLinkDeviceStats(@Param("ls") TLinkDeviceStats localeStats);
}
