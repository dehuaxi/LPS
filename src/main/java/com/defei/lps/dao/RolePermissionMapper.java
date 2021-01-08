package com.defei.lps.dao;

import com.defei.lps.entity.RolePermission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePermissionMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);
    //根据角色id删除
    void deleteByRoleid(@Param("roleid")int roleId);
    //根据权限id删除
    void deleteByPermissionid(@Param("permissionid")int permissionId);

    //添加
    int insertSelective(RolePermission record);

    //修改
    int updateByPrimaryKeySelective(RolePermission record);

    //根据id查询
    RolePermission selectByPrimaryKey(Integer id);
}