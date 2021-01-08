package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;

@Data
public class GeelyBillCache {
    private Integer id;
    //PD单号
    private String billnumber;
    //物料信息
    private Good good;
    //数量
    private Integer count;
    //批次
    private String batch;
    //是否紧急
    private String urgent;
    //上传时间
    private Date uploadtime;

}