package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.RoleService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class RoleController {
    @Autowired
    private RoleService roleService;

    //跳转到角色页面
    @RequestMapping(value = "toRole", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("role")
    public String toRole() {
        return "role";
    }

    //条件查询所有角色
    @RequestMapping(value = "role", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("role")
    @ResponseBody
    public Result findAll(String roleName, int currentPage) {
        return roleService.findAll(roleName,currentPage);
    }

    //添加角色
    @RequestMapping(value = "roleAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("roleAdd")
    @ResponseBody
    public Result addRole(String roleName, String describes, String permissionName) {
        return roleService.addRole(roleName, describes, permissionName);
    }

    //修改角色
    @RequestMapping(value = "roleUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("roleUpdate")
    @ResponseBody
    public Result updateRole(int id, String roleName, String describes, String permissionName){
        return roleService.updateRole(id, roleName, describes, permissionName);
    }

    //删除角色
    @RequestMapping(value = "roleDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("roleDelete")
    @ResponseBody
    public Result deleteRoleById(int id) {
        return roleService.deleteRoleById(id);
    }

    //查询所有角色，除开系统管理员角色systemManager
    @RequestMapping(value = "currentRole", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result currentRole(){
        return roleService.currentRole();
    }
}
