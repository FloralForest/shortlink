package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkDeviceStats;
import com.project.shortlink.project.dao.entity.TLinkNetworkStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 统计网络类型 Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-13
 */
@Mapper
public interface TLinkNetworkStatsMapper extends BaseMapper<TLinkNetworkStats> {

    //短链接跳转统计SQL
    @Insert("INSERT INTO t_link_network_stats ( full_short_url, gid, date, cnt, network, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.cnt}, #{ls.network}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{ls.cnt};")
    void shortLinkNetworkStats(@Param("ls") TLinkNetworkStats localeStats);
}
