package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkLocaleStats;
import com.project.shortlink.project.dao.entity.TLinkOsStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 统计操作系统	 Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-12
 */
@Mapper
public interface TLinkOsStatsMapper extends BaseMapper<TLinkOsStats> {

    //短链接跳转统计SQL
    @Insert("INSERT INTO t_link_os_stats ( full_short_url, gid, date, cnt, os, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.cnt}, #{ls.os}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{ls.cnt};")
    void shortLinkOsStats(@Param("ls") TLinkOsStats localeStats);
}
