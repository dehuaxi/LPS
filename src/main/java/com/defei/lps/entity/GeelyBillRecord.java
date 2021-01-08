package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;
@Data
public class GeelyBillRecord {
    private Integer id;
    //未回执吉利单据记录id
    private Integer geelybillcacheid;
    //PD单号
    private String billnumber;
    //物料信息
    private Good good;
    //数量
    private Integer count;
    //实收数量
    private Integer receivecount;
    //批次
    private String batch;
    //是否紧急
    private String urgent;
    //是否需要绑定额外的吉利单据
    private String needbind;
    //绑定的额外的吉利单据号
    private String bindbillnumber;
    //上传时间
    private String uploadtime;
    //完成时间
    private Date receivetime;
    //备注
    private String remarks;
}