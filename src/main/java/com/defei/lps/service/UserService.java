package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface UserService {
    //-------------------------用户管理页面-----------------------
    //添加用户
    public Result addUser(String userName, String password, String roleName,String routeNames);
    //删除用户通过id
    public Result deleteUserById(int id);
    //管理员修改用户
    public Result updateUser(int id, String password, String roleName,String routeNames);
    //条件分页查询
    public Result findAll(String userName, String roleName, int currentPage);
    //-----------------------------首页-------------------------
    //用户自己重置密码
    public Result updatePassword(String oldPassword, String newPassword);
    //----------------------------用户管理页面--------------------
    //根据用户id获取用户信息和用户的线路信息
    public Result userById(int id);

}
