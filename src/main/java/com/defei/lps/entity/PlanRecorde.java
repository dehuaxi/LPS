package com.defei.lps.entity;

import lombok.Data;

import java.util.Date;

@Data
public class PlanRecorde {
    private Integer id;
    //计划在途id
    private Integer plancacheid;
    //物料信息，对应的就是表中的goodid字段
    private Good good;
    //数量
    private Integer count;
    //当前计划最大取货数量
    private Integer maxcount;
    //当前计划最小取货数量
    private Integer mincount;
    //箱数=数量/收容数
    private Integer boxcount;
    //计划日期
    private String date;
    //计划预计到达工厂日期
    private String receivedate;
    //计划类型 1.系统 2.手工
    private String type;
    //是否紧急拉动 1.是 2.否
    private String urgent;
    //备注
    private String remarks;
    //计划创建时间
    private String createtime;
    //计划完成时间
    private Date overtime;
}