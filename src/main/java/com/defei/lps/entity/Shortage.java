package com.defei.lps.entity;

import lombok.Data;

@Data
public class Shortage {
    private Integer id;
    //物料信息表中的物料id
    private Good good;
    //计划日期
    private String date;
    //需求数量
    private Integer needcount;
    //上次上传缺件报表时的需求数量
    private Integer lastneedcount;
    //扣除当天消耗后的物料库存
    private Integer stock;
    //上次上传缺件报表时的扣除当天消耗后的物料库存
    private Integer laststock;
}