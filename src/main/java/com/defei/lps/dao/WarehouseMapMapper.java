package com.defei.lps.dao;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.defei.lps.entity.WarehouseMapInfo;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: WarehouseMapMapper
 * @description: TODO
 * @author: ChenQiao
 * @date: 2020/12/23 15:27
 * @version: 1.0
 */
public interface WarehouseMapMapper {

    /*
     * @Author CQ
     * @Description TODO 查询近期取货仓库
     * @Date  16:36
     * @param null
     * @return
    **/
    @Select("SELECT * FROM plantake,warehouse "+
            "${ew.CustomSqlSegment}")
    List<Map<String,Object>> selectByTime(@Param(Constants.WRAPPER) Wrapper queryWrapper,Integer time);


    /*
     * @Author CQ
     * @Description TODO 查询供应商位置
     * @Date  15:41 
     * @param null 
     * @return  
    **/
    @Select("SELECT * FROM supplier,plantake,good ")
    List<Map<String,Object>> selectBySupplier(@Param(Constants.WRAPPER) Wrapper queryWrapper, WarehouseMapInfo warehouseMapInfo);
}
