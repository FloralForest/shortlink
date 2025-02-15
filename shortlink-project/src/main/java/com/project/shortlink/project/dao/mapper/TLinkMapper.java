package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLink;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
