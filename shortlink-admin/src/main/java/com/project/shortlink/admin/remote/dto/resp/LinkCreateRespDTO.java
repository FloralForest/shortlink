package com.project.shortlink.admin.remote.dto.resp;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接响应参数
 */
@Data
//可以自动生成一个建造者模式相关的代码 使用生成的建造者模式来创建对象
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkCreateRespDTO {

    /**
     * 短链接分组信息
     */
    private String gid;

    /**
     * 域名
     */
    private String domain;

    /**
     * 原始链接
     */
    private String originUrl;

    /**
     * 短链接
     */
    private String fullShortUrl;

}
