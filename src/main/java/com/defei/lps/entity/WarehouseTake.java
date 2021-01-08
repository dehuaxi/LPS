package com.defei.lps.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
@Data
public class WarehouseTake {
    private Integer id;
    //装载方案编号
    private String billnumber;
    //物料信息
    private Good good;
    //吉利单号
    private String geelybillnumber;
    //吉利单中的物料数量
    private Integer geelycount;
    //吉利批次
    private String batch;
    //方案数量
    private Integer count;
    //收容数
    private Integer oneboxcount;
    //方案箱数
    private Integer boxcount;
    //翻包状态
    private String packstate;
    //计算出来的长度
    private BigDecimal length;
    //体积
    private BigDecimal volume;
    //重量
    private BigDecimal weight;
    //出发地名称
    private String startname;
    //出发地编号
    private String startnumber;
    //目的地名称
    private String endname;
    //目的地编号
    private String endnumber;
    //线路类型
    private String routetype;
    //车型
    private String cartype;
    //高板长mm
    private Integer highlength;
    //高板高mm
    private Integer highheight;
    //低板长mm
    private Integer lowlength;
    //低板高mm
    private Integer lowheight;
    //计算的车高mm
    private Integer carheight;
    //车宽mm
    private Integer carwidth;
    //创建人
    private String username;
    //创建时间
    private Date createtime;
    //中转仓
    private Warehouse warehouse;
}