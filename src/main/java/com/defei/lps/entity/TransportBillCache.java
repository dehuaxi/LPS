package com.defei.lps.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class TransportBillCache {
    private Integer id;
    //运输单号
    private String billnumber;
    //物料信息
    private Good good;
    //PD单号
    private String geelybillnumber;
    //PD单中物料数量
    private Integer geelycount;
    //PD单中物料得批次
    private String batch;
    //运输单中物料得运输数量
    private Integer count;
    //运输单中物料得运输箱数
    private Integer boxcount;
    //运输起始地址名称，1.区域名称  2.中转仓名称
    private String startname;
    //运输起始地址编号，1.区域编号  2.中转仓编号
    private String startnumber;
    //运输起目的地址名称，1.工厂名称  2.中转仓名称
    private String endname;
    //运输起目的地址编号，1.工厂编号  2.中转仓编号
    private String endnumber;
    //线路类型：1.区域-中转仓  2.区域-工厂  3.中转仓-中转仓  4.中转仓-工厂
    private String routetype;
    //车牌号
    private String carnumber;
    //司机名称
    private String driver;
    //司机手机号
    private String phone;
    //承运商
    private String carriername;
    //车型
    private String cartypename;
    //高板长。17.5米的车有高低板，所以要分为2部分记录车的高度和长度
    private Integer highlength;
    //高板高
    private Integer highheight;
    //低板长。17.5米的车有高低板，所以要分为2部分记录车的高度和长度
    private Integer lowlength;
    //低板高
    private Integer lowheight;
    //车宽
    private Integer carwidth;
    //运输费
    private BigDecimal money;
    //备注
    private String remarks;
    //运输单创建人账号名称
    private String username;
    //运输单创建时间
    private Date createtime;
}