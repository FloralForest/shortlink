package com.project.shortlink.project.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 短链接监控访问记录(日志)响应参数 + 分页
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkStatsAccessRecordRespDTO {

    /**
     * 访问时间
     * json序列化工具
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createTime;

    /**
     * IP
     */
    private String ip;

    /**
     * 地区
     */
    private String locale;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 设备
     */
    private String device;

    /**
     * 网络
     */
    private String network;

    /**
     * 访客类型
     */
    private String uvType;

    /**
     * 用户信息
     */
    private String user;

}
