package com.defei.lps.entity;

import lombok.Data;

/**
 * 角色类
 */
@Data
public class Role {
    private Integer id;

    private String rolename;//角色名称

    private String describes;//角色描述
}