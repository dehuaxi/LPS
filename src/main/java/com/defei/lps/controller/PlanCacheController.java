package com.defei.lps.controller;

import com.defei.lps.result.Result;
import com.defei.lps.service.PlanCacheService;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class PlanCacheController {
    @Autowired
    private PlanCacheService planCacheService;
    //--------------------------------在途缺件计划页面--------------------------------
    //跳转到页面
    @RequestMapping(value = "toPlanCache", produces = {"text/html;charset=UTF-8"})
    @RequiresPermissions("planCache")
    public String toPlanCache() {
        return "planCache";
    }

    //条件分页查询
    @RequestMapping(value = "planCache", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planCache")
    @ResponseBody
    public Result findAll(String goodCode,String goodName,String supplierCode, String supplierName, int routeId, int factoryId, String date,String state,String urgent,String type,int currentPage) {
        return planCacheService.findAll(goodCode,goodName,supplierCode,supplierName,routeId,factoryId,date,state,urgent,type,currentPage);
    }

    //删除
    @RequestMapping(value = "planCacheDelete", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planCacheDelete")
    @ResponseBody
    public Result delete(int id) {
        return planCacheService.delete(id);
    }

    //修改
    @RequestMapping(value = "planCacheUpdate", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planCacheUpdate")
    @ResponseBody
    public Result update(int id, int count,String remarks) {
        return planCacheService.update(id,count,remarks);
    }

    //修改缺件计划的最大取货数量
    @RequestMapping(value = "planCacheUpdateMaxcount", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result planCacheUpdateMaxcount(int planCacheId, int maxCount) {
        return planCacheService.planCacheUpdateMaxcount(planCacheId,maxCount);
    }


    //根据id查询
    @RequestMapping(value = "planCacheById", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planCacheUpdate")
    @ResponseBody
    public Result planCacheUpdate(int id) {
        return planCacheService.planCacheUpdate(id);
    }

    //根据供应商编号查询所有的取货数量不等于计划数量的未取货计划
    @RequestMapping(value = "untakePlanCacheBySupplierCode", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    public Result untakePlanCacheBySupplierCode(String supplierCode) {
        return planCacheService.untakePlanCacheBySupplierCode(supplierCode);
    }

//----------------------------------生成取货计划页面----------------------------------
    //生成取货计划页面，根据出发地id和目的地id查询所有未确认缺件计划
    @RequestMapping(value = "planCacheByRoute", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result planCacheByRoute(int startId,int endId,String endType) {
        return planCacheService.planCacheByRoute(startId,endId,endType);
    }

    //复选框选中后，根据计划Id查询计划详情
    @RequestMapping(value = "planCacheDetail", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result planCacheDetail(int planCacheId) {
        return planCacheService.planCacheDetail(planCacheId);
    }

    //选择某个缺件计划后，修改计划的日期后重新加载该物料的计划，并重新计算未确认计划，更新到页面
    @RequestMapping(value = "planCacheChoose", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result planCacheChoose(int planCacheId, String takeDate,int chooseCount,int lowHeight,int carWidth) {
        return planCacheService.planCacheChoose(planCacheId,takeDate,chooseCount,lowHeight,carWidth);
    }

    //取消选择某个缺件计划后，根据计划id重新加载该物料的缺件记录、缺件计划信息
    @RequestMapping(value = "planCacheChooseCancel", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result planCacheChooseCancel(int planCacheId) {
        return planCacheService.planCacheChooseCancel(planCacheId);
    }


    //传入缺件报表记录的日期、物料id，修改该日期中该物料的缺件报表记录的结存数量
    @RequestMapping(value = "updateShortageByGoodAndDate", produces = {"application/json;charset=UTF-8"})
    @ResponseBody
    @RequiresPermissions("planTakeAdd")
    public Result updateShortageByGoodAndDate(int goodId,int stock,String date) {
        return planCacheService.updateShortageByGoodAndDate(goodId,stock,date);
    }

    //生成取货计划页面，添加缺件计划
    @RequestMapping(value = "planCacheAdd", produces = {"application/json;charset=UTF-8"})
    @RequiresPermissions("planTakeAdd")
    @ResponseBody
    public Result add(int goodId, int count, String date,String remarks) {
        return planCacheService.add(goodId, count,date,remarks);
    }
}
