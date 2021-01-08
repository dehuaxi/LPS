package com.defei.lps.entity;

import lombok.Data;

/**
 * 工厂，即客户。每条线路的运输终点
 * 比如：吉利的宁波春晓工厂，编号为1081
 */
@Data
public class Factory {
    private Integer id;
    //工厂名称
    private String factoryname;
    //工厂编号
    private String factorynumber;
    //描述
    private String describes;
    //省
    private String province;
    //市
    private String city;
    //区(县)
    private String district;
    //详细地址
    private String address;
    //经度
    private String longitude;
    //纬度
    private String latitude;
}