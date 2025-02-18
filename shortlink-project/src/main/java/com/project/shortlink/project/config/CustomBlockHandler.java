package com.project.shortlink.project.config;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.project.shortlink.project.common.convention.result.Result;
import com.project.shortlink.project.dto.req.LinkCreateDTO;
import com.project.shortlink.project.dto.resp.LinkCreateRespDTO;

/**
 * 自定义流控策略
 */
public class CustomBlockHandler {

    public static Result<LinkCreateRespDTO> createShortLinkBlockHandlerMethod(LinkCreateDTO requestParam, BlockException exception) {
        return new Result<LinkCreateRespDTO>().setCode("B100000").setMessage("当前创建数过多，请稍后再试...");
    }
}
