package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.PlanTakeService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@Controller
public class PlanTakeController {
    @Autowired
    private PlanTakeService planTakeService;
    //--------------------------------取货计划页面--------------------------------
    //跳转到取货计划页面
    @RequestMapping(value = "toPlanTake", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planTake")
    public String toPlanTake() {
        return "planTake";
    }

    //条件分页查询
    @RequestMapping(value = "planTake", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTake")
    @ResponseBody
    public Result findAll(String planNumber,String supplierCode,String supplierName,int routeId,String date,String startName,String endName,String userName,int currentPage) {
        return planTakeService.findLimitByCondition(planNumber,supplierCode,supplierName,routeId,date,startName,endName,userName,currentPage);
    }


    //根据计划编号查询详情
    @RequestMapping(value = "planTakeDetail", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeDetail")
    @ResponseBody
    public Result planTakeDetail(String planNumber){
        return planTakeService.findDetailByPlannumber(planNumber);
    }

    //下载
    @RequestMapping(value = "planTakeDownload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planTakeDownload")
    public void download(String planNumber,String supplierCode,String supplierName,int routeId,String date,String startName,String endName,String userName, HttpServletResponse response) {
        planTakeService.download(planNumber,supplierCode,supplierName,routeId,date,startName,endName,userName,response);
    }

    //--------------------------------生成取货计划页面--------------------------------
    //跳转到生成取货计划页面
    @RequestMapping(value = "toPlanTakeAdd", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    public String toPlanTakeAdd() {
        return "planTakeAdd";
    }

    //添加取货计划
    @RequestMapping(value = "planTakeAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result add(int startId,int endId,String endType,String date,String carType,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String planCacheInfos) {
        return planTakeService.add(startId, endId,endType,date,carType,highLength,highHeight,lowLength,lowHeight,carWidth,planCacheInfos);
    }

    //--------------------------------拼拆货计划页面--------------------------------
    //跳转到拼拆取货计划页面
    @RequestMapping(value = "toPlanTakeUpdate", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planTakeUpdate")
    public String toPlanTakeUpdate(String planNumbers, Model model) {
        //获取所有计划详情，返回给页面
        model.addAttribute("planNumbers",planNumbers);
        return "planTakeUpdate";
    }

    //根据计划编号获取详细信息
    @RequestMapping(value = "planTakeByNumbers", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpdate")
    @ResponseBody
    public Result planTakeByNumbers(String planNumbers) {
        return planTakeService.planTakeByNumbers(planNumbers);
    }

    //传入取货计划id、取货数量、箱数、车宽、车高来计算长度、体积、重量
    @RequestMapping(value = "planTakeCalculate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpdate")
    @ResponseBody
    public Result planTakeCalculate(int id,int count,int boxCount,int lowHeight,int carWidth) {
        return planTakeService.planTakeCalculate(id,count,boxCount,lowHeight,carWidth);
    }

    //传入信息生成新的取货计划
    @RequestMapping(value = "planTakeAddRepeat", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpdate")
    @ResponseBody
    public Result planTakeAddRepeat(String carType,int highLength,int highHeight,int lowLength,int lowHeight,int carWidth,String planTakeInfos) {
        return planTakeService.planTakeAddRepeat(carType,highLength,highHeight,lowLength,lowHeight,carWidth,planTakeInfos);
    }

    //--------------------------------绑定吉利单据页面--------------------------------
    //跳转到拼拆取货计划页面
    @RequestMapping(value = "toPlanTakeUpload", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planTakeUpload")
    public String toPlanTakeUpload(String planNumber, Model model) {
        //获取所有计划详情，返回给页面
        model.addAttribute("planNumber",planNumber);
        return "planTakeUpload";
    }

    //根据计划编号查询内容，同物料id合并，并根据供应商编号排序再根据物料编号排序
    @RequestMapping(value = "planTakeByNumber", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpload")
    @ResponseBody
    public Result planTakeByNumber(String planNumber) {
        return planTakeService.planTakeByNumber(planNumber);
    }

    //上传PD单绑定
    @RequestMapping(value = "planTakeUpload", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeUpload")
    @ResponseBody
    public Result planTakeUpload(@RequestParam(value="pdFiles",required=false) MultipartFile[] files,String planNumber) {
        return planTakeService.planTakeUpload(files,planNumber);
    }

}
