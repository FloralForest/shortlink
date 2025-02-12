package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkLocaleStats;
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
 * @since 2025-02-12
 */
@Mapper
public interface TLinkLocaleStatsMapper extends BaseMapper<TLinkLocaleStats> {

    //短链接跳转统计SQL
    @Insert("INSERT INTO t_link_locale_stats ( full_short_url, gid, date, cnt, province, city, adcode, country, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.cnt}, #{ls.province}, #{ls.city}, #{ls.adcode}, #{ls.country}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{ls.cnt};")
    void shortLinkLocalStats(@Param("ls") TLinkLocaleStats localeStats);
}
