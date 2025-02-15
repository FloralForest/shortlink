package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkDeviceStats;
import com.project.shortlink.project.dao.entity.TLinkNetworkStats;
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

    //根据短链接获取指定日期内访问网络监控数据 根据短链接、gid、网络类型 合计cnt在同一网络类型的数值 SUM() -- 返回某列值之和
    @Select("SELECT network, SUM(cnt) AS cnt " +
            "FROM t_link_network_stats " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "full_short_url, gid, network;")
    List<TLinkNetworkStats> listNetworkStatsByShortLink(@Param("param") LinkStatsDTO requestParam);

    //根据分组获取指定日期内访问网络监控数据
    @Select("SELECT " +
            "    network, " +
            "    SUM(cnt) AS cnt " +
            "FROM " +
            "    t_link_network_stats " +
            "WHERE " +
            "    gid = #{param.gid} " +
            "    AND date BETWEEN #{param.startDate} and #{param.endDate} " +
            "GROUP BY " +
            "    gid, network;")
    List<TLinkNetworkStats> listNetworkStatsByGroup(@Param("param") LinkGroupStatsDTO linkGroupStatsDTO);
}
