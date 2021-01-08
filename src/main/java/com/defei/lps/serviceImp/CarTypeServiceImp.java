package com.defei.lps.serviceImp;

import com.defei.lps.dao.CarTypeMapper;
import com.defei.lps.entity.CarType;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.CarTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author 高德飞
 * @create 2020-11-30 17:21
 */
@Service
public class CarTypeServiceImp implements CarTypeService {
    @Autowired
    private CarTypeMapper carTypeMapper;

    @Override
    public Result add(String carTypeName, int highLength,int highHeight,int lowLength,int lowHeight, int carWidth,String carWeight,String carVolume) {
        if(!carTypeName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_.-]{0,10}$")){
            return ResultUtil.error(1,"车型名称只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_.-)");
        }else if(!carWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1})?$")){
            return ResultUtil.error(1,"载重(t)只能是最多保留小数点后一位小数的数字");
        }else if(!carVolume.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
            return ResultUtil.error(1,"满载(m³)只能是最多保留小数点后2位小数的数字");
        }
        CarType carType=carTypeMapper.selectByName(carTypeName);
        if(carType!=null){
            return ResultUtil.error(1,"该车型已经存在");
        }
        CarType carType1=new CarType();
        carType1.setCartypename(carTypeName);
        carType1.setHighlength(highLength);
        carType1.setHighheight(highHeight);
        carType1.setLowlength(lowLength);
        carType1.setLowheight(lowHeight);
        carType1.setCarwidth(carWidth);
        carType1.setCarweight(new BigDecimal(carWeight));
        carType1.setCarvolume(new BigDecimal(carVolume));
        carTypeMapper.insertSelective(carType1);
        return ResultUtil.success();
    }

    @Override
    public Result update(int id, int highLength,int highHeight,int lowLength,int lowHeight, int carWidth,String carWeight,String carVolume) {
        if(!carWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1})?$")){
            return ResultUtil.error(1,"载重(t)只能是最多保留小数点后一位小数的数字");
        }else if(!carVolume.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
            return ResultUtil.error(1,"满载(m³)只能是最多保留小数点后2位小数的数字");
        }
        CarType carType=carTypeMapper.selectByPrimaryKey(id);
        if(carType==null){
            return ResultUtil.error(1,"车型不存在，刷新页面后重试");
        }
        carType.setHighlength(highLength);
        carType.setHighheight(highHeight);
        carType.setLowlength(lowLength);
        carType.setLowheight(lowHeight);
        carType.setCarwidth(carWidth);
        carType.setCarweight(new BigDecimal(carWeight));
        carType.setCarvolume(new BigDecimal(carVolume));
        carTypeMapper.updateByPrimaryKeySelective(carType);
        return ResultUtil.success();
    }

    @Override
    public Result delete(int id) {
        carTypeMapper.deleteByPrimaryKey(id);
        //删除对应的车辆信息
        //TODO
        return ResultUtil.success();
    }

    @Override
    public Result findAll() {
        List<CarType> carTypeList=carTypeMapper.selectAll();
        if(carTypeList.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(carTypeList);
    }

    @Override
    public Result carTypeByName(String name) {
        CarType carType=carTypeMapper.selectByName(name);
        if(carType==null){
            return ResultUtil.error(1,"没有该车型信息");
        }
        return ResultUtil.success(carType);
    }
}
