package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;
@Data
public class WarehouseOut {
    private Integer id;
    //运输单号
    private String billnumber;
    //物料信息
    private Good good;
    //吉利单号
    private String geelybillnumber;
    //吉利单据中该物料的数量
    private Integer geelycount;
    //吉利单据中该物料的批次
    private String batch;
    //实际在库数量
    private Integer count;
    //实际在库箱数
    private Integer boxcount;
    //备注
    private String remarks;
    //收货人
    private String username;
    //收货时间
    private Date createtime;
    //所属中转仓
    private Warehouse warehouse;
}