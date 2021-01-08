package com.defei.lps.entity;

import lombok.Data;

/**
 * 路线。即哪些供应商走一条运输路线
 * 一条线路只对应一个工厂，一个工厂可对应多条线路
 * 一个供应商对应某个工厂时只能走一条线路
 */
@Data
public class Route {
    private Integer id;
    //路线名称
    private String routename;
    //路线编号
    private String routenumber;
    //描述
    private String describes;
    //区域，即本线路最初的出发地
    private Area area;
    //工厂，即本线路的最终目的地
    private Factory factory;
}