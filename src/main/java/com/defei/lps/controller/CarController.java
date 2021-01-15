package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.CarService;
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
public class CarController {
    @Autowired
    private CarService carService;

    @RequestMapping(value = "toCar", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("car")
    public String toCar() {
        return "car";
    }

    //查询
    @RequestMapping(value = "car", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("car")
    @ResponseBody
    public Result findAll(String carNumber, int carrierId, int carTypeId,int currentPage) {
        return carService.findAll(carNumber,carrierId,carTypeId,currentPage);
    }

    //添加
    @RequestMapping(value = "carAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carAdd")
    @ResponseBody
    public Result add(String carNumber, int carrierId, int carTypeId,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth) {
        return carService.add(carNumber, carrierId,carTypeId,highLength,highHeight,lowLength,lowHeight,carWidth);
    }

    //删除
    @RequestMapping(value = "carDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carDelete")
    @ResponseBody
    public Result delete(int id) {
        return carService.delete(id);
    }

    //修改
    @RequestMapping(value = "carUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("carUpdate")
    @ResponseBody
    public Result update(int id, int carrierId, int carTypeId,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth) {
        return carService.update(id,carrierId,carTypeId,highLength,highHeight,lowLength,lowHeight,carWidth);
    }

    //根据车型名称查询
    @RequestMapping(value = "carByCartype", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result carByCartype(String carTypeName) {
        return carService.findByCarType(carTypeName);
    }

    //根据车牌号查询
    @RequestMapping(value = "carByCarnumber", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result carByCarnumber(String carNumber) {
        return carService.carByCarnumber(carNumber);
    }
}
