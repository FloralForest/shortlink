package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkAccessStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 *  Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-11
 */
@Mapper
public interface TLinkAccessStatsMapper extends BaseMapper<TLinkAccessStats> {

    //短链接跳转统计SQL
    @Insert("INSERT INTO t_link_access_stats ( full_short_url, gid, date, pv, uv, uip, hour, weekday, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.pv}, #{ls.uv}, #{ls.uip}, #{ls.hour}, #{ls.weekday}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "pv = pv + #{ls.pv}, " +
            "uv = uv + #{ls.uv}, " +
            "uip = uip + #{ls.uip};")
    void shortLinkStats(@Param("ls") TLinkAccessStats tLinkAccessStats);
}
