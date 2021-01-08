package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.PermissionService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PermissionController {
    @Autowired
    private PermissionService permissionService;

    //跳转页面
    @RequestMapping(value = "toPermission", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("permission")//具有查询用户的权限的用户才能访问这个方法
    public String toPermission() {
        return "permission";
    }

    //删除
    @RequestMapping(value = "permissionDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("permissionDelete")
    @ResponseBody
    public Result deletePermission(String permissionName){
        return permissionService.deletePermission(permissionName);
    }

    //添加
    @RequestMapping(value = "permissionAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("permissionAdd")
    @ResponseBody
    public Result addPermission(String url, String permissionName, int pid) {
        return permissionService.addPermission(url, permissionName, pid);
    }

    //修改
    @RequestMapping(value = "permissionUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("permissionUpdate")
    @ResponseBody
    public Result updatePermission(int id,String url, String permissionName){
        return permissionService.updatePermission(id,url, permissionName);
    }

    //查询当前登录用户的所有权限,以zTree需要的数据格式返回
    @RequestMapping(value = "permissionZTree", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result permissionZTree() {
        return permissionService.permissionZTree();
    }

    //根据角色的id查询角色对应的权限
    @RequestMapping(value = "permissionByRoleid", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result permissionByRoleid(int roleId) {
        return permissionService.permissionByRoleid(roleId);
    }

    //根据权限名称查询
    @RequestMapping(value = "permissionByName", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result permissionByName(String permissionName) {
        return permissionService.permissionByName(permissionName);
    }
}
