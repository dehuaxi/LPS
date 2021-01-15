package com.defei.lps.entity;

import lombok.Data;

@Data
public class Car {
    private Integer id;
    //车牌号
    private String carnumber;
    //承运商
    private Carrier carrier;
    //车型
    private CarType cartype;
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

}