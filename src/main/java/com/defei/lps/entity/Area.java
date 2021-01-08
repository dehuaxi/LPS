package com.defei.lps.entity;

import lombok.Data;

/**
 * 区域
 * 多家供应商化为一个片区，以便取货
 * 一个区域与多个供应商绑定，一个供应商可以与多个区域绑定
 */
@Data
public class Area {
    private Integer id;
    //区域名称
    private String areaname;
    //编号
    private String areanumber;
    //描述
    private String describes;
}