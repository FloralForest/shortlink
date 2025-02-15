package com.project.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.project.dao.entity.TLink;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dto.req.LinkPageDTO;
import com.project.shortlink.project.dto.resp.LinkPageRespDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author project
 * @since 2025-02-02
 */
@Mapper
public interface TLinkMapper extends BaseMapper<TLink> {

    //分页统计
    @Select("<script>" +
            "SELECT l.*, " +
            "COALESCE(lt.today_pv, 0) AS todayPv, " +
            "COALESCE(lt.today_uv, 0) AS todayUv, " +
            "COALESCE(lt.today_uip, 0) AS todayUip " +
            "FROM t_link l " +
            "LEFT JOIN t_link_stats_today lt " +
            "ON l.gid = lt.gid AND l.full_short_url = lt.full_short_url AND lt.date = CURDATE() " +
            "WHERE l.gid = #{lp.gid} AND l.enable_status = 0 AND l.del_flag = 0 " +
            "<choose>" +
            "  <when test='lp.orderTag != null and lp.orderTag == \"todayPv\"'>ORDER BY todayPv DESC</when>" +
            "  <when test='lp.orderTag != null and lp.orderTag == \"todayUv\"'>ORDER BY todayUv DESC</when>" +
            "  <when test='lp.orderTag != null and lp.orderTag == \"todayUip\"'>ORDER BY todayUip DESC</when>" +
            "  <when test='lp.orderTag != null and lp.orderTag == \"totalPv\"'>ORDER BY l.total_pv DESC</when>" +
            "  <when test='lp.orderTag != null and lp.orderTag == \"totalUv\"'>ORDER BY l.total_uv DESC</when>" +
            "  <when test='lp.orderTag != null and lp.orderTag == \"totalUip\"'>ORDER BY l.total_uip DESC</when>" +
            "  <otherwise>ORDER BY l.create_time DESC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<TLink> pageLink(@Param("lp") LinkPageDTO linkPageDTO);

    //TLink表修改历史pv、uv、uip
    @Update("UPDATE t_link " +
            "SET total_pv = total_pv + #{totalPv}, total_uv = total_uv + #{totalUv}, total_uip = total_uip + #{totalUip} " +
            "WHERE gid = #{gid} AND full_short_url = #{fullShortUrl}")
    void incrementStats(
            @Param("gid") String gid,
            @Param("fullShortUrl") String fullShortUrl,
            @Param("totalPv") Integer totalPv,
            @Param("totalUv") Integer totalUv,
            @Param("totalUip") Integer totalUip);
}
