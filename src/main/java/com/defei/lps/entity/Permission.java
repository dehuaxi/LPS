package com.defei.lps.entity;

import lombok.Data;

/**
 * 权限类
 */
@Data
public class Permission {
    private Integer id;

    private String url;//url地址

    private String permissionname;//权限名称

    private Integer pid;//父id,一级节点的父id为0
}