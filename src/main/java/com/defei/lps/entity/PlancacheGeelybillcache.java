package com.defei.lps.entity;

import lombok.Data;

@Data
public class PlancacheGeelybillcache {
    private Integer id;
    //缺件计划id
    private Integer plancacheid;
    //未回执吉利单据id
    private Integer geelybillcacheid;
    //吉利单据是否回执：默认否
    private String returnstate;
}