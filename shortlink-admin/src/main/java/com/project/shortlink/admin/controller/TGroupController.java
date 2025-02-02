package com.project.shortlink.admin.controller;

import com.project.shortlink.admin.common.convention.result.Result;
import com.project.shortlink.admin.common.convention.result.Results;
import com.project.shortlink.admin.dto.req.GroupSaveDTO;
import com.project.shortlink.admin.dto.req.GroupSortDTO;
import com.project.shortlink.admin.dto.req.GroupUpdateDTO;
import com.project.shortlink.admin.dto.resp.GroupRespDTO;
import com.project.shortlink.admin.service.TGroupService;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.annotations.Delete;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author project
 * @since 2025-01-29
 */
@RestController
@RequiredArgsConstructor//配合Lombok的构造器注入
@RequestMapping("/api/shortlink/admin/")
public class TGroupController {
    //@Resource JDK17后推荐使用final修饰加RequiredArgsConstructor构造器注入
    private final TGroupService tGroupService;

    //新增分组
    @PostMapping("group/saveName")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> saveGroup(@RequestBody GroupSaveDTO groupSaveDTO){
        tGroupService.saveGroup(groupSaveDTO.getName());
        return Results.success()
                .setCode("20000")
                .setMessage("添加分组成功");
    }

    //查询分组
    @GetMapping("group/findAll")
    public Result<List<GroupRespDTO>> listGroup(){
        return Results.success(tGroupService.listGroup())
                .setCode("20000")
                .setMessage("分组已置入");
    }

    //修改组名
    @PutMapping("group/update")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> updateGroup(@RequestBody GroupUpdateDTO groupUpdateDTO){
        tGroupService.updateGroup(groupUpdateDTO);
        return Results.success()
                .setCode("20000")
                .setMessage("修改成功");
    }

    //删除组
    @DeleteMapping("group/delete")
    //@RequestParam 从URL的查询字符串（如 ?name=John&age=20）或 POST 表单数据中获取参数值。
    public Result<Void> deleteGroup(@RequestParam String gid){
        tGroupService.deleteGroup(gid);
        return Results.success()
                .setCode("20000")
                .setMessage("删除成功");
    }

    //组排序(前端做排序，这里本质上还是修改sortOrder的值)
    @PostMapping("group/sort")
    //@RequestBody 将 HTTP 请求体（如 JSON、XML）中的数据转换为 Java 对象。
    public Result<Void> sortGroup(@RequestBody List<GroupSortDTO> groupSortDTOList){
        tGroupService.sortGroup(groupSortDTOList);
        return Results.success()
                .setCode("20000")
                .setMessage("排序成功");
    }
}
