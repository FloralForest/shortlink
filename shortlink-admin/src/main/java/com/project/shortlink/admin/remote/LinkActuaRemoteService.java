package com.project.shortlink.admin.remote;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.dto.resp.LinkCountRespDTO;
import com.project.shortlink.admin.remote.dto.req.*;
import com.project.shortlink.admin.remote.dto.resp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * 短链接中台远程调用服务(使用SpringCloud nacos 调用)
 */
//被调用应用名（project->yml）
@FeignClient("short-link-project")
public interface LinkActuaRemoteService {


    //创建
    @PostMapping("/api/shortlink/project/link/createLink")
    Result<LinkCreateRespDTO> createLink(@RequestBody LinkCreateDTO linkCreateDTO);


    // 批量创建短链接
    @PostMapping("/api/shortlink/project/link/createLink/batch")
    Result<LinkBatchCreateRespDTO> batchCreateLink(@RequestBody LinkBatchCreateDTO requestParam);

    //分页
    @GetMapping("/api/shortlink/project/link/page")
    Result<Page<LinkPageRespDTO>> pageLink(
            @RequestParam("gid") String gid,
            @RequestParam("orderTag") String orderTag,
            @RequestParam("current") Long current,
            @RequestParam("size") Long size);

    //查询分组下的短链接数量
    @GetMapping("/api/shortlink/project/link/count")
    Result<List<LinkCountRespDTO>> listLinkCount(@RequestParam("gidNumber") List<String> gidNumber);

    //修改短链接
    @PostMapping("/api/shortlink/project/link/update")
    void linkUpdate(LinkUpdateDTO linkUpdateDTO);

    //获取原链接标题
    @GetMapping("/api/shortlink/project/link/title")
    Result<String> getLinkTitle(@RequestParam("url") String url);

    //置入回收站
    @PostMapping("/api/shortlink/project/link/recycle/saveRB")
    void saveRecycleBin(@RequestBody RecycleBinSaveDTO recycleBinSaveDTO);

    //回收站分页
    @GetMapping("/api/shortlink/project/link/recycle/page")
    Result<Page<LinkPageRespDTO>> pageRecycleLink(
            @RequestParam("gidList") List<String> gidList,
            @RequestParam("current") Long current,
            @RequestParam("size") Long size);

    //短链接回收站恢复
    @PostMapping("/api/shortlink/project/link/recycle/recover")
    void recoverLink(@RequestBody RecycleBinRecoverDTO recycleBinRecoverDTO);

    //短链接回收站删除
    @PostMapping("/api/shortlink/project/link/recycle/remove")
    void removeLink(@RequestBody RecycleBinRemoveDTO recycleBinRecoverDTO);

    //访问单个短链接指定时间内监控数据
    @GetMapping("/api/shortlink/project/stats")
    Result<LinkStatsRespDTO> oneLinkStats(
            @RequestParam("gid") String gid,
            @RequestParam("fullShortUrl") String fullShortUrl,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    //短链接监控访问记录(日志) + 分页
    @GetMapping("/api/shortlink/project/stats/lar")
    Result<Page<LinkStatsAccessRecordRespDTO>> linkStatsAccessRecord(
            @RequestParam("gid") String gid,
            @RequestParam("fullShortUrl") String fullShortUrl,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate);

    //访问分组短链接指定时间内监控数据
    @GetMapping("/api/shortlink/project/stats/group")
    Result<LinkStatsRespDTO> groupShortLinkStats(
            @RequestParam("gid") String gid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );

    //短链接监控访问记录(日志) + 分页
    @GetMapping("/api/shortlink/project/stats/group/lar")
    Result<Page<LinkStatsAccessRecordRespDTO>> groupLinkStatsAccessRecord(
            @RequestParam("gid") String gid,
            @RequestParam("startDate") String startDate,
            @RequestParam("endDate") String endDate
    );
}
