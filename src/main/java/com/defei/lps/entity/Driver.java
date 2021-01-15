package com.defei.lps.entity;

import lombok.Data;

@Data
public class Driver {
    private Integer id;
    //司机姓名
    private String name;
    //手机号
    private String phone;
    //驾驶证号
    private String licensenumber;

}