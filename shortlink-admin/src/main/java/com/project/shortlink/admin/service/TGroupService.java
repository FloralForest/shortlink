package com.project.shortlink.admin.service;

import com.project.shortlink.admin.dao.entity.TGroup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.project.shortlink.admin.dto.req.GroupSaveDTO;
import com.project.shortlink.admin.dto.req.GroupSortDTO;
import com.project.shortlink.admin.dto.req.GroupUpdateDTO;
import com.project.shortlink.admin.dto.resp.GroupRespDTO;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author project
 * @since 2025-01-29
 */
public interface TGroupService extends IService<TGroup> {
    /**
     * 新曾短链接分组
     */
    void saveGroup(String groupName);

    /**
     * 查询分组
     */
    List<GroupRespDTO> listGroup();

    /**
     * 修改组名
     */
    void updateGroup(GroupUpdateDTO groupUpdateDTO);

    /**
     * 删除组
     */
    void deleteGroup(String gid);

    /**
     * 排序(前端做排序，这里本质上还是修改sortOrder的值)
     */
    void sortGroup(List<GroupSortDTO> groupSortDTOList);
}
