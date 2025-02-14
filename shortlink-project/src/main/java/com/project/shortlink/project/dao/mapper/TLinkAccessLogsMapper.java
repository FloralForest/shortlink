package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkAccessLogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dto.req.LinkStatsAccessRecordQueryDTO;
import com.project.shortlink.project.dto.req.LinkStatsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 高频访问IP记录 Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-13
 */
@Mapper
public interface TLinkAccessLogsMapper extends BaseMapper<TLinkAccessLogs> {
    //根据短链接获取指定日期内高频访问IP数据 COUNT() -- 返回某列行数
    @Select("SELECT ip, COUNT(ip) AS cnt " +
            "FROM t_link_access_logs " +
            "WHERE full_short_url = #{param.fullShortUrl} AND gid = #{param.gid} AND create_time BETWEEN CONCAT(#{param.startDate},'00:00:00') and CONCAT(#{param.endDate},'23:59:59') " +
            "GROUP BY " +
            "full_short_url, gid, ip " +
            "ORDER BY " +
            "cnt DESC " +
            "LIMIT 5;")
    List<HashMap<String, Object>> listTopIpByShortLink(@Param("param") LinkStatsDTO requestParam);

    //根据短链接获取指定日期内新旧访客数据 SUM() -- 返回某列值之和
    @Select("SELECT SUM(old_user) AS oldUserCnt, SUM(new_user) AS newUserCnt " +
            "FROM ( " +
            "    SELECT " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) > 1 THEN 1 ELSE 0 END AS old_user, " +
            "        CASE WHEN COUNT(DISTINCT DATE(create_time)) = 1 AND MAX(create_time) >= #{param.startDate} AND MAX(create_time) <= #{param.endDate} THEN 1 ELSE 0 END AS new_user " +
            "    FROM " +
            "        t_link_access_logs " +
            "    WHERE " +
            "        full_short_url = #{param.fullShortUrl} " +
            "        AND gid = #{param.gid} " +
            "    GROUP BY " +
            "        user " +
            ") AS user_counts;")
    HashMap<String, Object> findUvTypeCntByShortLink(@Param("param") LinkStatsDTO requestParam);

    //根据日期查询用户是新访客还是老访客
    @Select("<script> " +
            "SELECT " +
            "    user, " +
            "    CASE " +
            "        WHEN MIN(create_time) BETWEEN #{param.startDate} AND #{param.endDate} THEN '新访客' " +
            "        ELSE '老访客' " +
            "    END AS uvType " +
            "FROM " +
            "    t_link_access_logs " +
            "WHERE " +
            "    full_short_url = #{param.fullShortUrl} " +
            "    AND gid = #{param.gid} " +
            "    AND user IN " +
            "    <foreach item='item' index='index' collection='param.userList' open='(' separator=',' close=')'> " +
            "        #{item} " +
            "    </foreach> " +
            "GROUP BY " +
            "    user;" +
            "    </script>"
    )
    List<Map<String, Object>> selectUvTypeByUsers(@Param("param") LinkStatsAccessRecordQueryDTO accessLogsQueryDTO);
}
