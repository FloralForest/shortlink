package com.project.shortlink.admin.remote;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.remote.dto.req.RecycleBinSaveDTO;
import com.project.shortlink.admin.dto.resp.LinkCountRespDTO;
import com.project.shortlink.admin.remote.dto.req.*;
import com.project.shortlink.admin.remote.dto.resp.LinkCreateRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkPageRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkStatsAccessRecordRespDTO;
import com.project.shortlink.admin.remote.dto.resp.LinkStatsRespDTO;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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
        //排序字段
        map.put("orderTag", linkPageDTO.getOrderTag());
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

    //获取原链接标题
    default Result<String> getLinkTitle(@RequestParam("url") String url){
        String resultStr = HttpUtil.get("http://localhost:8001/api/shortlink/project/link/title?url=" + url);
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }

    //置入回收站
    default void saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO){
        HttpUtil.post("http://localhost:8001/api/shortlink/project/link/recycle/saveRB",
                JSON.toJSONString(recycleBinSaveDTO));
    }

    //回收站分页
    default Result<IPage<LinkPageRespDTO>> pageRecycleLink(LinkRecycleBinPageDTO linkPageDTO){
        Map<String, Object> map = new HashMap<>();
        //分组集合
        map.put("gidList", linkPageDTO.getGidList());
        //当前页
        map.put("current", linkPageDTO.getCurrent());
        //每页数
        map.put("size", linkPageDTO.getSize());
        final String resultPage = HttpUtil.get("http://localhost:8001/api/shortlink/project/link/recycle/page", map);
        //解析成json字符串 隐式转换
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    //短链接回收站恢复
    default void recoverLink(RecycleBinRecoverDTO recycleBinRecoverDTO){
        HttpUtil.post("http://localhost:8001/api/shortlink/project/link/recycle/recover",
                JSON.toJSONString(recycleBinRecoverDTO));
    }

    //短链接回收站删除
    default void removeLink(RecycleBinRemoveDTO recycleBinRecoverDTO){
        HttpUtil.post("http://localhost:8001/api/shortlink/project/link/recycle/remove",
                JSON.toJSONString(recycleBinRecoverDTO));
    }

     //访问单个短链接指定时间内监控数据
    default Result<LinkStatsRespDTO> oneLinkStats(LinkStatsDTO linkStatsDTO) {
        String resultBodyStr = HttpUtil.get("http://localhost:8001/api/shortlink/project/stats", BeanUtil.beanToMap(linkStatsDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    //短链接监控访问记录(日志) + 分页
    default Result<IPage<LinkStatsAccessRecordRespDTO>> linkStatsAccessRecord(LinkStatsAccessRecordDTO linkPageDTO){
        Map<String, Object> map = BeanUtil.beanToMap(linkPageDTO,false,true);
        map.remove("orders");
        map.remove("records");
        String resultPage = HttpUtil.get("http://localhost:8001/api/shortlink/project/stats/lar", map);
        //解析成json字符串 隐式转换
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }

    //访问分组短链接指定时间内监控数据
    default Result<LinkStatsRespDTO> groupShortLinkStats(LinkGroupStatsDTO linkGroupStatsDTO) {
        String resultBodyStr = HttpUtil.get("http://localhost:8001/api/shortlink/project/stats/group", BeanUtil.beanToMap(linkGroupStatsDTO));
        return JSON.parseObject(resultBodyStr, new TypeReference<>() {
        });
    }

    //短链接监控访问记录(日志) + 分页
    default Result<IPage<LinkStatsAccessRecordRespDTO>> groupLinkStatsAccessRecord(LinkGroupStatsAccessRecordDTO linkPageDTO){
        Map<String, Object> map = BeanUtil.beanToMap(linkPageDTO,false,true);
        map.remove("orders");
        map.remove("records");
        String resultPage = HttpUtil.get("http://localhost:8001/api/shortlink/project/stats/group/lar", map);
        //解析成json字符串 隐式转换
        return JSON.parseObject(resultPage, new TypeReference<>() {
        });
    }
}
