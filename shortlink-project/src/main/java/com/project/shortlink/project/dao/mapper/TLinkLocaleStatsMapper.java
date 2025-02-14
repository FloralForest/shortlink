package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkLocaleStats;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

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

    //根据短链接获取指定日期内基础监控数据 根据短链接、gid、省份 合计cnt在同一省份的数值 SUM() -- 返回某列值之和
    @Select("SELECT province, SUM(cnt) AS cnt " +
            "FROM t_link_locale_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, province;")
    List<TLinkLocaleStats> listLocaleByShortLink(@Param("param") LinkStatsDTO requestParam);
}
