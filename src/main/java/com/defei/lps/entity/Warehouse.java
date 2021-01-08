package com.defei.lps.entity;

import lombok.Data;

@Data
public class Warehouse {
    private Integer id;
    //仓库名称
    private String warehousename;
    //仓库编号
    private String warehousenumber;
    //描述
    private String describes;
    //联系人
    private String contact;
    //电话
    private String phone;
    //省
    private String province;
    //市
    private String city;
    //区县
    private String district;
    //详细地址
    private String address;
    //经度
    private String longitude;
    //纬度
    private String latitude;
}