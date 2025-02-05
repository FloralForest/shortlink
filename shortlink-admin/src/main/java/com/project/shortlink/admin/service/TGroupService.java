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
     * 新曾短链接分组 可变参数 最后的值可带可不带，增加灵活性
     */
    void saveGroup(String groupName, String... username);
//    /**
//     * 新曾短链接分组 带用户名参数
//     */
//    void saveGroup(String username,String groupName);

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
