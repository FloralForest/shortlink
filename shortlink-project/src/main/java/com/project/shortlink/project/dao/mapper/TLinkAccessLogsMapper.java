package com.project.shortlink.project.dao.mapper;

import com.project.shortlink.project.dao.entity.TLinkAccessLogs;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.project.shortlink.project.dao.entity.TLinkBrowserStats;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

}
