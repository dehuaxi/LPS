package com.defei.lps.serviceImp;

import com.defei.lps.dao.CarTypeMapper;
import com.defei.lps.dao.DriverMapper;
import com.defei.lps.entity.CarType;
import com.defei.lps.entity.Carrier;
import com.defei.lps.entity.Driver;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.CarTypeService;
import com.defei.lps.service.DriverService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 高德飞
 * @create 2020-11-30 17:21
 */
@Service
public class DriverServiceImp implements DriverService {
    @Autowired
    private DriverMapper driverMapper;

    /**
     * 添加
     * @param driverName
     * @param phone
     * @param licenseNumber
     * @return
     */
    @Override
    public Result add(String driverName, String phone,String licenseNumber) {
        if(!driverName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"司机姓名只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!phone.matches("^[1]{1}[0-9]{10}$")){
            return ResultUtil.error(1,"手机号格式不正确");
        }else if(!licenseNumber.matches("^[0-9A-Za-z]{0,20}$")){
            return ResultUtil.error(1,"驾驶证号只能是1-20位的数字、大小写字母");
        }
        Driver driver=driverMapper.selectByPhone(phone);
        if(driver!=null){
            return ResultUtil.error(1,"该手机号已经录入过");
        }
        Driver driver1=new Driver();
        driver1.setName(driverName);
        driver1.setPhone(phone);
        driver1.setLicensenumber(licenseNumber);
        driverMapper.insert(driver1);
        return ResultUtil.success();
    }

    /**
     * 修改
     * @param id
     * @param driverName
     * @param phone
     * @param licenseNumber
     * @return
     */
    @Override
    public Result update(int id, String driverName, String phone,String licenseNumber) {
        if(!driverName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"司机姓名只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!phone.matches("^[1]{1}[0-9]{10}$")){
            return ResultUtil.error(1,"手机号格式不正确");
        }else if(!licenseNumber.matches("^[0-9A-Za-z]{0,20}$")){
            return ResultUtil.error(1,"驾驶证号只能是1-20位的数字、大小写字母");
        }
        Driver driver=driverMapper.selectByPrimaryKey(id);
        if(driver==null){
            return ResultUtil.error(1,"修改的司机信息不存在");
        }
        if(!phone.equals(driver.getPhone())){
            //看修改后的手机号是否存在
            Driver oldDriver=driverMapper.selectByPhone(phone);
            if(oldDriver!=null){
                return ResultUtil.error(1,"修改后的手机号已经存在了");
            }
        }
        driver.setName(driverName);
        driver.setPhone(phone);
        driver.setLicensenumber(licenseNumber);
        driverMapper.updateByPrimaryKey(driver);
        return ResultUtil.success();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        driverMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 分页查询
     * @return
     */
    @Override
    public Result findAll(String driverName, String phone,int currentPage) {
        //校验参数
        if(!driverName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,10}$")){
            return ResultUtil.error(1,"司机姓名只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!phone.matches("^[0-9]{0,11}$")){
            return ResultUtil.error(1,"手机号格式不正确");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Driver> list=driverMapper.selectLimitByCondition(driverName,phone ,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=driverMapper.selectCountByCondition(driverName,phone);
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
     * 查询所有，用于下拉框生成
     * @return
     */
    @Override
    public Result driverAll() {
        List<Driver> drivers=driverMapper.selectAll();
        if(drivers.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(drivers);
    }

}
