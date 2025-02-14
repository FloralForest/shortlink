package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkBrowserStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dao.entity.TLinkOsStats;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

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

    //根据短链接获取指定日期内浏览器监控数据 根据短链接、gid、浏览器 合计count在同一浏览器的数值 SUM() -- 返回某列值之和
    @Select("SELECT browser, SUM(cnt) AS cnt " +
            "FROM t_link_browser_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, browser;")
    List<HashMap<String, Object>> listBrowserStatsByShortLink(@Param("param") LinkStatsDTO requestParam);
}
