package com.defei.lps.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PlanTakeRecord {
    private Integer id;
    //计划编号
    private String plannumber;
    //物料信息
    private Good good;
    //取货数量
    private Integer count;
    //实际取货数量
    private Integer realcount;
    //箱数
    private Integer boxcount;
    //计算出来的占车的长度m
    private BigDecimal length;
    //计算出来的物料体积立方米
    private BigDecimal volume;
    //计算出来的物料重量t
    private BigDecimal weight;
    //取货日期
    private String date;
    //出发地名称，即区域名称
    private String startname;
    //出发地编号，即区域编号
    private String startnumber;
    //目的地名称。1.中转仓名称  2.工厂名称
    private String endname;
    //目的地编号。1.中转仓编号  2.工厂编号
    private String endnumber;
    //线路类型：1.区域-中转仓  2.区域-工厂
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
    //车宽mm
    private Integer carwidth;
    //生成人
    private String username;
    //创建时间
    private String createtime;
    //完成时间
    private Date overtime;
}