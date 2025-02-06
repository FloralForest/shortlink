package com.project.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.dto.resp.LinkCountRespDTO;
import com.project.shortlink.admin.remote.dto.req.LinkCreateDTO;
import com.project.shortlink.admin.remote.dto.req.LinkPageDTO;
import com.project.shortlink.admin.remote.dto.req.LinkUpdateDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 短链接中台远程调用服务
 */
public interface LinkRemoteService {


    //创建
    default Result<LinkCreateRespDTO> createLink(LinkCreateDTO linkCreateDTO){
        final String resultBodyStr = HttpUtil.post("http://localhost:8001/api/shortlink/project/link/createLink",
                JSON.toJSONString(linkCreateDTO));
        //解析成json字符串 隐式转换
        return JSON.parseObject(resultBodyStr,new TypeReference<>() {
        });
    }

    //分页
    default Result<IPage<LinkPageRespDTO>> pageLink(LinkPageDTO linkPageDTO){
        Map<String, Object> map = new HashMap<>();
        map.put("gid", linkPageDTO.getGid());
        //当前页
        map.put("current", linkPageDTO.getCurrent());
        //每页数
        map.put("size", linkPageDTO.getSize());
        final String resultPage = HttpUtil.get("http://localhost:8001/api/shortlink/project/link/page", map);
        //解析成json字符串 隐式转换
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    //查询分组下的短链接数量
    default Result<List<LinkCountRespDTO>> listLinkCount(List<String> gidNumber){
        Map<String, Object> map = new HashMap<>();
        //分组id（gid）
        map.put("number", gidNumber);
        final String resultPage = HttpUtil.get("http://localhost:8001/api/shortlink/project/link/count", map);
        //解析成json字符串 隐式转换
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    //修改短链接
    default void linkUpdate(LinkUpdateDTO linkUpdateDTO){
        HttpUtil.post("http://localhost:8001/api/shortlink/project/link/update", JSON.toJSONString(linkUpdateDTO));
    }
}
