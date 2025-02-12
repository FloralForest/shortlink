package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkBrowserStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dao.entity.TLinkOsStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 统计浏览器 Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-13
 */
@Mapper
public interface TLinkBrowserStatsMapper extends BaseMapper<TLinkBrowserStats> {
    //短链接跳转统计SQL
    @Insert("INSERT INTO t_link_browser_stats ( full_short_url, gid, date, cnt, browser, create_time, update_time, del_flag ) " +
            "VALUES " +
            "( #{ls.fullShortUrl}, #{ls.gid}, #{ls.date}, #{ls.cnt}, #{ls.browser}, NOW(), NOW(), 0 ) " +
            "ON DUPLICATE KEY UPDATE " +
            "cnt = cnt + #{ls.cnt};")
    void shortLinkBrowserState(@Param("ls") TLinkBrowserStats localeStats);
}
