package com.defei.lps.entity;

import com.defei.lps.entity.Warehouse;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @ClassName: WarehouseMapInfo
 * @description: TODO
 * @author: ChenQiao
 * @date: 2020/12/23 10:37
 * @version: 1.0
 */
@Data
public class WarehouseMapInfo extends Warehouse implements Serializable {
    //中转仓
    private String warehousename;
    private String province;
    private String city;
    private String district;
    private String address;
    private String longitude;
    private String latitude;
    private String contact;
    private String phone;
    //取货日期（厂家）
    private String date;
    //供应商名称
    private String suppliername;
    //供应商简称
    private String abbreviation;
    //计算出来的物料体积立方米
    private BigDecimal volume;
    //计算出来的物料重量t
    private BigDecimal weight;
    //物料id
    private int goodid;
}
