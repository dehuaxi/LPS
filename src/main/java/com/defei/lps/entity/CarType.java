package com.defei.lps.entity;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CarType {
    private Integer id;
    //车型名称
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
    //载重t
    private BigDecimal carweight;
    //体积
    private BigDecimal carvolume;
}