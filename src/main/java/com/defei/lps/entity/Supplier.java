package com.defei.lps.entity;

import lombok.Data;

@Data
public class Supplier {
    private Integer id;
    //供应商编号
    private String suppliercode;
    //供应商名称
    private String suppliername;
    //供应商名称简称
    private String abbreviation;
    //联系人
    private String contact;
    //电话
    private String phone;
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
    //线路实体类，对应表中的routeid字段
    private Route route;
    //运输在途天数
    private String transitday;
}