package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkAccessStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dto.req.LinkGroupStatsDTO;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
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

    //根据指定日期获取监控数据 根据短链接、gid、时间 合计pv、uv、uip在同一时间(同一天)的数值 SUM() -- 返回某列值之和
    @Select("SELECT date, SUM(pv) AS pv, SUM(uv) AS uv, SUM(uip) AS uip " +
            "FROM t_link_access_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, date;")
    List<TLinkAccessStats> listStatsByShortLink(@Param("param") LinkStatsDTO linkStatsDTO);

    //根据指定日期获取监控数据 根据短链接、gid、小时 合计pv、uv、uip在同一时间(同一小时)的数值 SUM() -- 返回某列值之和
    @Select("SELECT hour, SUM(pv) AS pv " +
            "FROM t_link_access_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, hour;")
    List<TLinkAccessStats> listHourStatsByShortLink(@Param("param") LinkStatsDTO requestParam);

    //根据指定日期获取监控数据 根据短链接、gid、周几 合计pv、uv、uip在同一时间(同一周几)的数值 SUM() -- 返回某列值之和
    @Select("SELECT weekday, SUM(pv) AS pv " +
            "FROM t_link_access_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, weekday;")
    List<TLinkAccessStats> listWeekdayStatsByShortLink(@Param("param") LinkStatsDTO requestParam);

    //根据分组获取指定日期内基础监控数据
    @Select("SELECT " +
            "    date, " +
            "    SUM(pv) AS pv, " +
            "    SUM(uv) AS uv, " +
            "    SUM(uip) AS uip " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, date;")
    List<TLinkAccessStats> listStatsByGroup(@Param("param") LinkGroupStatsDTO linkGroupStatsDTO);

    //根据分组获取指定日期内小时基础监控数据
    @Select("SELECT " +
            "    hour, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, hour;")
    List<TLinkAccessStats> listHourStatsByGroup(@Param("param") LinkGroupStatsDTO linkGroupStatsDTO);

    //根据分组获取指定日期内小时基础监控数据
    @Select("SELECT " +
            "    weekday, " +
            "    SUM(pv) AS pv " +
            "FROM " +
            "    t_link_access_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, weekday;")
    List<TLinkAccessStats> listWeekdayStatsByGroup(@Param("param") LinkGroupStatsDTO linkGroupStatsDTO);
}
