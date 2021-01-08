package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PlanHandleRecord {
    private Integer id;
    //物料信息
    private Good good;
    //计划日期
    private String date;
    //操作的内容
    private String content;
    //计划产生的时间
    private String createtime;
    //操作时间
    private Date handletime;
    //操作人
    private String username;
}