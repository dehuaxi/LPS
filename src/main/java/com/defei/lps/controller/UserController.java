package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.UserService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {
    @Autowired
    private UserService userService;
    //---------------------------用户管理页面-----------------------------
    //跳转到用户管理界面
    @RequestMapping(value = "toUser", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("user")//具有查询用户的权限的用户才能访问这个方法
    public String toUser() {
        return "user";
    }

    //条件查询用户，可查询全部用户
    @RequestMapping(value = "user", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("user")
    @ResponseBody
    public Result findAll(String userName, String roleName, int currentPage) {
        return userService.findAll(userName,roleName, currentPage);
    }

    //添加
    @RequestMapping(value = "userAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("userAdd")
    @ResponseBody
    public Result addUser(String userName, String password, String roleName,String routeNames){
        return userService.addUser(userName, password, roleName, routeNames);
    }

    //删除
    @RequestMapping(value = "userDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("userDelete")
    @ResponseBody
    public Result deleteUserById(int id) {
        return userService.deleteUserById(id);
    }

    //修改用户
    @RequestMapping(value = "userUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("userUpdate")
    @ResponseBody
    public Result updateUser(int id, String password, String roleName,String routeNames) {
        return userService.updateUser(id, password,roleName, routeNames);
    }
    //---------------------------首页-----------------------------
    //自身修改密码
    @RequestMapping(value = "updatePassword", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result updatePassword(String oldPassword, String newPassword){
        return userService.updatePassword(oldPassword, newPassword);
    }

    //根据用户id获取用户信息和用户的线路信息
    @RequestMapping(value = "userById", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result userById(int id){
        return userService.userById(id);
    }
}
