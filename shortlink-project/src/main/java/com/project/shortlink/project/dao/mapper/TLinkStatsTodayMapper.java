package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkOsStats;
import com.project.shortlink.project.dao.entity.TLinkStatsToday;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 每日统计表 Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-15
 */
@Mapper
public interface TLinkStatsTodayMapper extends BaseMapper<TLinkStatsToday> {

    //”今日统计“
    @Insert("INSERT INTO t_link_stats_today ( full_short_url, gid, date, today_pv, today_uv, today_uip, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.todayPv}, #{ls.todayUv}, #{ls.todayUip}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "today_pv = today_pv + #{ls.todayPv}, " +
            "today_uv = today_uv + #{ls.todayUv}, " +
            "today_uip = today_uip + #{ls.todayUip};")
    void shortLinkStatsToday(@Param("ls") TLinkStatsToday linkStats);

}
