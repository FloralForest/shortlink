package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkLocaleStats;
import com.project.shortlink.project.dao.entity.TLinkOsStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dto.req.LinkGroupStatsDTO;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;

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

    //根据短链接获取指定日期内操作系统监控数据 根据短链接、gid、操作系统 合计cnt在同一操作系统的数值 SUM() -- 返回某列值之和
    @Select("SELECT os, SUM(cnt) AS cnt " +
            "FROM t_link_os_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, os;")
    List<HashMap<String, Object>> listOsStatsByShortLink(@Param("param") LinkStatsDTO requestParam);

    //根据分组获取指定日期内操作系统监控数据
    @Select("SELECT " +
            "    os, " +
            "    SUM(cnt) AS count " +
            "FROM " +
            "    t_link_os_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, os;")
    List<HashMap<String, Object>> listOsStatsByGroup(@Param("param") LinkGroupStatsDTO linkGroupStatsDTO);
}
