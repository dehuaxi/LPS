package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface RoleService {
    //添加角色
    public Result addRole(String roleName, String describes, String permissionName);
    //通过角色id删除
    public Result deleteRoleById(int id);
    //条件查询所有角色
    public Result findAll(String roleName,int currentPage);
    //修改角色。根据id修改其他除角色名称之外的信息
    public Result updateRole(int id, String roleName, String describes, String permissionName);
    //查询所有角色，除开系统管理员角色systemManager
    public Result currentRole();
}
