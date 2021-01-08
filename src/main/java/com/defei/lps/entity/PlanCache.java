package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PlanCache {
    private Integer id;
    //物料信息
    private Good good;
    //计划取货数量.默认取货数量=最大取货数量
    private Integer count;
    //当前计划最大取货数量
    private Integer maxcount;
    //当前计划最小取货数量
    private Integer mincount;
    //已确认数量，指分配了装载方案的数量，只有当已确认数量=计划数量时，计划状态变为未取货
    private Integer surecount;
    //取货数量，指车辆去厂家取货的数量，只有当取货数量=计划数量时，计划状态变为在途
    private Integer takecount;
    //收货数量，指该计划最终目的地收货的数量，只有当收货数量=计划数量时，运行中计划转化为计划历史记录
    private Integer receivecount;
    //箱数=数量/收容数
    private Integer boxcount;
    //计划的日期，即哪天去取货
    private String date;
    //计划预计到达工厂日期
    private String receivedate;
    //计划的状态：未确认，未取货，在途
    private String state;
    //计划类型 1.系统 2.手工
    private String type;
    //是否紧急拉动 1.是 2.否
    private String urgent;
    //备注
    private String remarks;
    //计划创建时间
    private Date createtime;
}