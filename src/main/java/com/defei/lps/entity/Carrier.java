package com.defei.lps.entity;

import lombok.Data;

@Data
public class Carrier {
    private Integer id;
    //承运商名称
    private String carriername;
    //承运商编号
    private String carriernumber;
    //联系人
    private String contact;
    //电话
    private String phone;
    //地址
    private String address;
}