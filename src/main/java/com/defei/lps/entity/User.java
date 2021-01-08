package com.defei.lps.entity;

import lombok.Data;

/**
 * 用户类
 */
@Data
public class User {
    private Integer id;

    private String username;//用户名

    private String password;//密码

    private String rolename;//角色名称
}