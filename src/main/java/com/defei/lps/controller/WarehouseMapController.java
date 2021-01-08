package com.defei.lps.controller;

import com.defei.lps.entity.WarehouseMapInfo;
import com.defei.lps.result.Result;
import com.defei.lps.service.WarehouseMapService;
import com.defei.lps.service.WarehouseService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * @ClassName: WarehouseMapController
 * @description: TODO
 * @author: ChenQiao
 * @date: 2020/12/22 16:43
 * @version: 1.0
 */
@Controller
public class WarehouseMapController {
    @Resource
    private WarehouseMapService warehouseMapService;
    @Resource
    private WarehouseService warehouseService;
    /*
     * @Author CQ
     * @Description TODO 跳转至中转仓地图页面
     * @Date  14:48
     * @param null
     * @return
     **/
    @RequestMapping("/toWarehouseMap")
    public String toWarehouseMap() {
        return "warehouseMap";
    }

    /*
     * @Author CQ
     * @Description TODO 查询中转仓
     * @Date  14:48
     * @param null
     * @return
     **/
    @RequestMapping("/warehouseMap")
    @ResponseBody
    public Result warehouseMap(){
        return warehouseService.warehouseZtree();
    }



    /*
     * @Author CQ
     * @Description TODO 根据天数查询中转仓及取货信息
     * @Date  16:48
     * @param null
     * @return
     **/
    @RequestMapping("/warehouseMapBy")
    @ResponseBody
    public Result warehouseMapBy(WarehouseMapInfo warehouseMapInfo,Integer time){
        return warehouseMapService.findByTime(warehouseMapInfo,time);
    }

    /*
     * @Author CQ
     * @Description TODO 查询供应商位置
     * @Date  15:45
     * @param null
     * @return
    **/
    @RequestMapping("/mapSupplier")
    @ResponseBody
    public Result warehouseMapBySupplier(WarehouseMapInfo warehouseMapInfo){
        return warehouseMapService.findBySupplier(warehouseMapInfo);
    }
}
