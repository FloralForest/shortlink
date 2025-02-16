package com.project.shortlink.project.dto.resp;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.write.style.ColumnWidth;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短链接基础信息响应参数
 */
@Data
//可以自动生成一个建造者模式相关的代码 使用生成的建造者模式来创建对象
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LinkBaseInfoRespDTO {
    /**
     * 描述信息
     */
    @ExcelProperty("标题")
    @ColumnWidth(40)
    private String describe;

    /**
     * 短链接
     */
    @ExcelProperty("短链接")
    @ColumnWidth(40)
    private String fullShortUrl;

    /**
     * 原始链接
     */
    @ExcelProperty("原始链接")
    @ColumnWidth(80)
    private String originUrl;
}
