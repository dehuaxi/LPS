package com.defei.lps.entity;

import lombok.Data;

/**
 * 权限角色关系
 */
@Data
public class RolePermission {
    private Integer id;

    private Integer roleid;//角色id

    private Integer permissionid;//权限id
}