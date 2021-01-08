package com.defei.lps.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Good {
    private Integer id;
    //物料名称
    private String goodname;
    //物料编号
    private String goodcode;
    //供应商，物料所属的供应商
    private Supplier supplier;
    //单箱收容数
    private Integer oneboxcount;
    //单箱工厂上线工装数
    private Integer bincount;
    //单台车的消耗数量
    private Integer onecarcount;
    //最大的库存
    private Integer maxstock;
    //拉动库存，只有物料的库存到达该库存了才执行拉动需求
    private Integer triggerstock;
    //配额比例，即多轨件每家供应商的月度配额占比：取值范围0-100之间整数
    private Integer quotaratio;
    //包装箱类型，比如：塑料箱、纸箱、围板箱、铁箱
    private String boxtype;
    //箱子的长 单位 毫米mm
    private Integer boxlength;
    //箱子的宽 单位 毫米mm
    private Integer boxwidth;
    //箱子的高：单位 毫米mm
    private Integer boxheight;
    //翻包后，上线的箱子的长 单位 毫米mm
    private Integer packboxlength;
    //翻包后，上线的箱子的宽 单位 毫米mm
    private Integer packboxwidth;
    //翻包后，上线的箱子的高：单位 毫米mm
    private Integer packboxheight;
    //翻包后，上线的装满物料后箱子的总重量：单位KG
    private BigDecimal packboxweight;
    //装满物料后箱子的总重量：单位KG
    private BigDecimal boxweight;
    //返空比例 ：数值0-100之间的整数
    private Integer returnratio;
    //单个托盘上放置的箱数
    private Integer onetrayboxcount;
    //单托上放几层
    private Integer onetraylayerscount;
    //托盘体积占比,托盘件每托总体积中托盘的体积占比是多少，单位%,数值为0-100
    private Integer trayratio;
    //托盘的长 单位 毫米mm
    private Integer traylength;
    //托盘的宽 单位 毫米mm
    private Integer traywidth;
    //托盘的高：单位 毫米mm
    private Integer trayheight;
    //包装描述：比如  塑料箱 1000*1200木托盘
    private String packremarks;
    //接收方，同一个工厂会有不同三方RDC或者是主机厂来收货
    private String receiver;
}