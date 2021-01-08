package com.defei.lps.entity;

import lombok.Data;

@Data
public class Params {
    private Integer id;
    //参数名称
    private String paramname;
    //参数值
    private String paramvalue;
    //参数类型
    private String paramtype;
    //描述
    private String describes;
}