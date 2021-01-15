package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.CarTypeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author 高德飞
 * @create 2020-11-30 17:40
 */
@Controller
public class CarTypeController {
    @Autowired
    private CarTypeService carTypeService;

    @RequestMapping(value = "toCarType", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("carType")
    public String toCarType() {
        return "carType";
    }

    //查询所有
    @RequestMapping(value = "carType", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carType")
    @ResponseBody
    public Result findAll() {
        return carTypeService.findAll();
    }

    //添加
    @RequestMapping(value = "carTypeAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carTypeAdd")
    @ResponseBody
    public Result add(String carTypeName, int highLength,int highHeight,int lowLength,int lowHeight, int carWidth,String carWeight,String carVolume) {
        return carTypeService.add(carTypeName, highLength,highHeight,lowLength,lowHeight,carWidth,carWeight,carVolume);
    }

    //删除
    @RequestMapping(value = "carTypeDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carTypeDelete")
    @ResponseBody
    public Result delete(int id) {
        return carTypeService.delete(id);
    }

    //修改
    @RequestMapping(value = "carTypeUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carTypeUpdate")
    @ResponseBody
    public Result update(int id, int highLength,int highHeight,int lowLength,int lowHeight, int carWidth,String carWeight,String carVolume) {
        return carTypeService.update(id,highLength,highHeight,lowLength,lowHeight,carWidth,carWeight,carVolume);
    }

    //根据车型名称查询
    @RequestMapping(value = "carTypeByName", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result carTypeByName(String name) {
        return carTypeService.carTypeByName(name);
    }
}
