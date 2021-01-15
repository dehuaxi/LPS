package com.defei.lps.serviceImp;

import com.defei.lps.dao.RouteMapper;
import com.defei.lps.dao.UserMapper;
import com.defei.lps.dao.WarehouseMapper;
import com.defei.lps.entity.Route;
import com.defei.lps.entity.User;
import com.defei.lps.entity.Warehouse;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.WarehouseService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class WarehouseServiceImp implements WarehouseService {
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private RouteMapper routeMapper;

    /**
     * 添加中转仓
     * @param warehouseName 名称
     * @param warehouseNumber 编号
     * @param describes 描述
     * @param contact 联系人
     * @param phone 电话
     * @param province 省
     * @param city 市
     * @param district 区县
     * @param address 详细地址
     * @param longitude 经度
     * @param latitude 纬度
     * @return
     */
    @Override
    public Result add(String warehouseName, String warehouseNumber, String describes, String contact, String phone, String province, String city, String district, String address, String longitude, String latitude) {
        //参数检查
        if(!warehouseName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!warehouseNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!describes.matches("^[\\u4e00-\\u9fa5]{0,50}$")){
            return ResultUtil.error(1,"描述只能是50个以内的汉字");
        }else if(!contact.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"联系人为1-10位的汉字");
        }else if(!phone.matches("^[0-9-]{0,15}$")){
            return ResultUtil.error(1,"电话必须为手机号或者是包含区号的座机号");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"区县为1-10位的汉字");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-（）()]{1,100}$")){
            return ResultUtil.error(1,"地址只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_()（）-)");
        }else if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"经度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"纬度取值范围为-180到180之间的最多保留6位小数的数字");
        }
        //看是否名称重复
        Warehouse warehouse=warehouseMapper.selectByWarehousename(warehouseName);
        if(warehouse!=null){
            return ResultUtil.error(1,"中转仓名称已存在");
        }
        Warehouse warehouse1=warehouseMapper.selectByWarehousenumber(warehouseNumber);
        if(warehouse1!=null){
            return ResultUtil.error(1,"中转仓编号已存在");
        }
        //添加
        Warehouse warehouse2=new Warehouse();
        warehouse2.setWarehousename(warehouseName);
        warehouse2.setWarehousenumber(warehouseNumber);
        warehouse2.setDescribes(describes);
        warehouse2.setContact(contact);
        warehouse2.setPhone(phone);
        warehouse2.setProvince(province);
        warehouse2.setCity(city);
        warehouse2.setDistrict(district);
        warehouse2.setAddress(address);
        warehouse2.setLongitude(longitude);
        warehouse2.setLatitude(latitude);
        warehouseMapper.insertSelective(warehouse2);
        return ResultUtil.success();
    }

    /**
     * 修改中转仓
     * @param id
     * @param warehouseName
     * @param warehouseNumber
     * @param describes
     * @param contact
     * @param phone
     * @param province
     * @param city
     * @param district
     * @param address
     * @param longitude
     * @param latitude
     * @return
     */
    @Override
    public Result update(int id, String warehouseName, String warehouseNumber, String describes, String contact, String phone, String province, String city, String district, String address, String longitude, String latitude) {
        //参数检查
        if(!warehouseName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!warehouseNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!describes.matches("^[\\u4e00-\\u9fa5]{0,50}$")){
            return ResultUtil.error(1,"描述只能是50个以内的汉字");
        }else if(!contact.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"联系人为1-10位的汉字");
        }else if(!phone.matches("^[0-9-]{0,15}$")){
            return ResultUtil.error(1,"电话必须为手机号或者是包含区号的座机号");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{1,10}$")){
            return ResultUtil.error(1,"区县为1-10位的汉字");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,100}$")){
            return ResultUtil.error(1,"区县只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!longitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"经度取值范围为-180到180之间的最多保留6位小数的数字");
        }else if(!latitude.matches("^(([\\+ \\-]?([1-9]{1}[0-9]{0,2})|([0]{1})))([.]\\d{0,6})?$")){
            return ResultUtil.error(1,"纬度取值范围为-180到180之间的最多保留6位小数的数字");
        }
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(id);
        if(warehouse==null){
            return ResultUtil.error(1,"中转仓不存在，刷新后重试");
        }
        //检查修改后仓库名称是否重复
        if(!warehouseName.equals(warehouse.getWarehousename())){
            Warehouse warehouse1=warehouseMapper.selectByWarehousename(warehouseName);
            if(warehouse1!=null){
                return ResultUtil.error(1,"修改后的仓库名称已存在");
            }
        }
        //检查修改后的仓库编号是否重复
        if(!warehouseNumber.equals(warehouse.getWarehousenumber())){
            Warehouse warehouse1=warehouseMapper.selectByWarehousenumber(warehouseNumber);
            if(warehouse1!=null){
                return ResultUtil.error(1,"修改后的仓库编号已存在");
            }
        }
        //修改
        warehouse.setWarehousename(warehouseName);
        warehouse.setWarehousenumber(warehouseNumber);
        warehouse.setDescribes(describes);
        warehouse.setContact(contact);
        warehouse.setPhone(phone);
        warehouse.setProvince(province);
        warehouse.setCity(city);
        warehouse.setDistrict(district);
        warehouse.setAddress(address);
        warehouse.setLongitude(longitude);
        warehouse.setLatitude(latitude);
        warehouseMapper.updateByPrimaryKeySelective(warehouse);
        return ResultUtil.success();
    }

    /**
     * 删除中转仓
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(id);
        if(warehouse==null){
            return ResultUtil.error(1,"中转仓不存在，刷新后重试");
        }
        //先删除中转仓-线路关系表中记录

        //再删除中转仓记录
        warehouseMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param warehouseName
     * @param warehouseNumber
     * @param province
     * @param city
     * @param district
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String warehouseName, String warehouseNumber, String province, String city, String district, int currentPage) {
        //参数检查
        if(!warehouseName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!warehouseNumber.matches("^[0-9A-Za-z#@_-]{0,10}$")){
            return ResultUtil.error(1,"工厂编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!province.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"省为1-10位的汉字");
        }else if(!city.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"市为1-10位的汉字");
        }else if(!district.matches("^[\\u4e00-\\u9fa5]{0,10}$")){
            return ResultUtil.error(1,"区县为1-10位的汉字");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Warehouse> list=warehouseMapper.selectLimitByCondition(warehouseName,warehouseNumber ,province,city,district,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=warehouseMapper.selectCountByCondition(warehouseName,warehouseNumber,province,city,district);
            int totalPage=0;
            if(totalCount%30==0) {
                totalPage=totalCount/30;
            }else {
                totalPage=totalCount/30+1;
            }
            //集合内容
            Map map=new HashMap();
            map.put("currentPage",currentPage);
            map.put("totalPage",totalPage);
            map.put("totalCount",totalCount);
            map.put("list",list);
            return ResultUtil.success(map);
        }
        return ResultUtil.success();
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public Result warehouseById(int id) {
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(id);
        if(warehouse==null){
            return ResultUtil.error(1,"中转仓不存在，刷新页面后重试");
        }
        return ResultUtil.success(warehouse);
    }

    /**
     * 查询所有的中转仓，并以Ztree结构返回
     * @return
     */
    @Override
    public Result warehouseZtree() {
        List<Warehouse> warehouseList=warehouseMapper.selectAll();
        if(warehouseList.isEmpty()){
            return ResultUtil.error(1,"没有中转仓信息");
        }
        List<Map<String,Object>> list=new ArrayList<>();
        for(Warehouse warehouse:warehouseList){
            Map<String,Object> map=new HashMap<>();
            map.put("id",warehouse.getId());
            map.put("pid",0);
            map.put("name",warehouse.getWarehousename());
            list.add(map);
        }
        return ResultUtil.success(list);
    }

    /**
     * 查询当前账号能看到的所有中转仓
     * @return
     */
    @Override
    public Result currentWarehouse() {
        String userName=(String) SecurityUtils.getSubject().getPrincipal();
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            return ResultUtil.error(1,"未登录，请登陆后再试");
        }
        List<Route> routeList=routeMapper.selectByUserid(user.getId());
        if(routeList.isEmpty()){
            return ResultUtil.error(1,"当前账号未分配线路");
        }
        //根据线路查询所有中转仓
        List<Warehouse> warehouseList=new ArrayList<>();
        for(Route route:routeList){
            List<Warehouse> warehouseList1=warehouseMapper.selectByRouteid(route.getId());
            if(!warehouseList1.isEmpty()){
                warehouseList.addAll(warehouseList1);
            }
        }
        //去重复
        List<Warehouse> warehouses=warehouseList.stream().distinct().collect(Collectors.toList());
        return ResultUtil.success(warehouses);
    }

    /**
     * 查询所有,为下拉框生成
     * @return
     */
    @Override
    public Result warehouseAll() {
        List<Warehouse> warehouses=warehouseMapper.selectAll();
        if(warehouses.isEmpty()){
            return ResultUtil.error(1,"无中转仓数据");
        }
        return ResultUtil.success(warehouses);
    }
}
