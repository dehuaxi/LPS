package com.defei.lps.serviceImp;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.defei.lps.dao.WarehouseMapMapper;
import com.defei.lps.entity.Supplier;
import com.defei.lps.entity.WarehouseMapInfo;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.WarehouseMapService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @ClassName: WarehouseMapServiceImpl
 * @description: TODO
 * @author: ChenQiao
 * @date: 2020/12/23 16:32
 * @version: 1.0
 */

@Service
public class WarehouseMapServiceImpl implements WarehouseMapService {

    @Resource
    private WarehouseMapMapper warehouseMapMapper;

    @Override
    public Result<WarehouseMapInfo> findByTime(WarehouseMapInfo warehouseMapInfo,Integer time) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.apply("plantake.startnumber=warehouse.warehousenumber");
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");//设置日期格式
//        df.format(new Date());  //new Date()为获取当前系统时间
        if(time==1){
            queryWrapper.apply("TO_DAYS(plantake.date) = TO_DAYS(NOW())");
        }
        if(time==2){
            queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL -1 DAY) = DATE(plantake.date)");
        }
        if(time==3){
            queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL -2 DAY) = DATE(plantake.date)");
        }
        if(time==4){
            queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL -3 DAY) = DATE(plantake.date)");
        }
        if(time==5){
            queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL -4 DAY) = DATE(plantake.date)");
        }
        if(time==6){
            queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL -5 DAY) = DATE(plantake.date)");
        }
        if(time==7){
            queryWrapper.apply("DATE_SUB(CURDATE(), INTERVAL -6 DAY) = DATE(plantake.date)");
        }
        List<Map<String,Object>> list=warehouseMapMapper.selectByTime(queryWrapper,time);
        if (list==null){
            return ResultUtil.error(1,"查询不到相关信息");
        }
        return ResultUtil.success(list);
    }

    @Override
    public Result<Supplier> findBySupplier(WarehouseMapInfo warehouseMapInfo) {
        QueryWrapper queryWrapper=new QueryWrapper();
        queryWrapper.apply(" plantake.goodid=good.id AND good.supplierid=supplier.id ");
        List<Map<String,Object>> list=warehouseMapMapper.selectBySupplier(queryWrapper,warehouseMapInfo);
        if (list==null){
            return ResultUtil.error(1,"查询不到相关信息");
        }
        return ResultUtil.success(list);
    }
}
