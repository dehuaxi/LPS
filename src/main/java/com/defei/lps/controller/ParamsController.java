package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.ParamService;
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
public class ParamsController {
    @Autowired
    private ParamService paramService;

    @RequestMapping(value = "toParams", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("params")
    public String toParams() {
        return "params";
    }

    //查询
    @RequestMapping(value = "params", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("params")
    @ResponseBody
    public Result findAll() {
        return paramService.findAll();
    }

    //添加
    @RequestMapping(value = "paramsAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("paramsAdd")
    @ResponseBody
    public Result add(String paramName,String paramValue,String paramType,String describes) {
        return paramService.add(paramName, paramValue,paramType,describes);
    }

    //删除
    @RequestMapping(value = "paramsDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("paramsDelete")
    @ResponseBody
    public Result delete(int id) {
        return paramService.delete(id);
    }

    //修改
    @RequestMapping(value = "paramsUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("paramsUpdate")
    @ResponseBody
    public Result update(int id,String paramValue,String paramType,String describes) {
        return paramService.update(id,paramValue,paramType,describes);
    }
}
