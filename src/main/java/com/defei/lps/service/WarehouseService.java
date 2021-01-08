package com.defei.lps.service;

import com.defei.lps.result.Result;

public interface WarehouseService {
    //添加
    public Result add(String warehouseName, String warehouseNumber, String describes, String contact,String phone,String province, String city, String district, String address, String longitude, String latitude);
    //根据id修改
    public Result update(int id, String warehouseName, String warehouseNumber, String describes, String contact,String phone,String province, String city, String district, String address, String longitude, String latitude);
    //根据id删除
    public Result delete(int id);
    //条件分页查询
    public Result findAll(String warehouseName, String warehouseNumber, String province, String city, String district, int currentPage);
    //根据id查询
    public Result warehouseById(int id);
    //查询所有的中转仓，并以Ztree结构返回
    public Result warehouseZtree();
    //查询当前账号能看到的所有中转仓
    public Result currentWarehouse();
}
