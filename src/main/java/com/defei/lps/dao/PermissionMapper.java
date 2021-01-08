package com.defei.lps.dao;

import com.defei.lps.entity.Permission;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PermissionMapper {
    //根据id删除
    int deleteByPrimaryKey(Integer id);
    //根据Pid删除
    void deleteByPid(@Param("pid")int pid);

    //添加
    int insertSelective(Permission record);

    //修改
    int updateByPrimaryKeySelective(Permission record);

    //根据id查询
    Permission selectByPrimaryKey(Integer id);
    //根据权限名称查询
    Permission selectByPermissionname(@Param("permissionname")String permissionName);
    //根据角色id查询
    List<Permission> selectByRoleid(@Param("roleid")int roleId);
    //根据url查询
    Permission selectByUrl(@Param("url")String url);
    //根据url+权限名称查询
    Permission selectByUrlAndPermissionname(@Param("url")String url,
                                            @Param("permissionname")String permissionName);
    //根据角色id查询，结果以url为集合
    Set<String> selectUrlByRolename(@Param("rolename")String roleName);
    //根据id查询所有的子权限，不包含自己
    List<Permission> selectChildById(@Param("id")int id);
}