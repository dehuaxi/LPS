package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;
@Data
public class WarehouseCache {
    private Integer id;
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
    //生成了装载方案的数量
    private Integer plancount;
    //收容数。1.厂家收容数  2.吉利上线工装数
    private Integer oneboxcount;
    //实际在库箱数
    private Integer boxcount;
    //翻包状态
    private String packstate;
    //入库时间
    private Date createtime;
    //所属中转仓
    private Warehouse warehouse;
}