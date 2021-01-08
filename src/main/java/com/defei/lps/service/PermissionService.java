package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface PermissionService {
    //增加，权限描述不能一样即权限名称。系统管理员才能用
    public Result addPermission(String url, String permissionName, int pid);
    //删除
    public Result deletePermission(String permissionName);
    //修改
    public Result updatePermission(int id,String url, String permissionName);
    //查询当前登录用户的所有权限,以zTree需要的数据格式返回
    public Result permissionZTree();
    //根据角色的id查询角色对应的权限
    public Result permissionByRoleid(int roleId);
    //根据权限名称查询
    public Result permissionByName(String permissionName);
}
