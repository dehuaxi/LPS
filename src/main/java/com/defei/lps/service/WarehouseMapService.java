package com.defei.lps.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.defei.lps.entity.Supplier;
import com.defei.lps.entity.WarehouseMapInfo;
import com.defei.lps.result.Result;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * @ClassName: WarehouseMapService
 * @description: TODO
 * @author: ChenQiao
 * @date: 2020/12/23 16:32
 * @version: 1.0
 */
public interface WarehouseMapService {

    /*
     * @Author CQ
     * @Description TODO 查询近期取货仓库
     * @Date  16:40
     * @param null
     * @return
    **/
    Result<WarehouseMapInfo> findByTime(WarehouseMapInfo warehouseMapInfo,Integer time);

    /*
     * @Author CQ
     * @Description TODO 查询供应商位置
     * @Date  15:42
     * @param null
     * @return
     **/
    Result<Supplier> findBySupplier(WarehouseMapInfo warehouseMapInfo);
}
