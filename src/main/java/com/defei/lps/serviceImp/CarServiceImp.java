package com.defei.lps.serviceImp;

import com.defei.lps.dao.CarMapper;
import com.defei.lps.dao.CarTypeMapper;
import com.defei.lps.dao.CarrierMapper;
import com.defei.lps.entity.Car;
import com.defei.lps.entity.CarType;
import com.defei.lps.entity.Carrier;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 高德飞
 * @create 2020-12-14 15:50
 */
@Service
public class CarServiceImp implements CarService {
    @Autowired
    private CarMapper carMapper;
    @Autowired
    private CarTypeMapper carTypeMapper;
    @Autowired
    private CarrierMapper carrierMapper;

    /**
     * 添加车辆信息
     * @param carNumber
     * @param carrierId
     * @param carTypeId
     * @param driver
     * @param phone
     * @param highLength
     * @param highHeight
     * @param lowLength
     * @param lowHeight
     * @param carWidth
     * @return
     */
    @Override
    public Result add(String carNumber, int carrierId, int carTypeId, String driver, String phone, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth) {
        //参数检验
        if(!driver.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"司机姓名只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!carNumber.matches("^([京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][1-9DF][1-9ABCDEFGHJKLMNPQRSTUVWXYZ]\\d{3}[1-9DF]|[京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][\\dABCDEFGHJKLNMxPQRSTUVWXYZ]{5})$")){
            return ResultUtil.error(1,"车牌格式不正确");
        }else if(!phone.matches("^[1]{1}[0-9]{10}$")){
            return ResultUtil.error(1,"手机号格式不正确");
        }
        Car car=carMapper.selectByCarNumber(carNumber);
        if(car!=null){
            return ResultUtil.error(1,"车牌已录入");
        }
        //承运商是否存在
        Carrier carrier=carrierMapper.selectByPrimaryKey(carrierId);
        if(carrier==null){
            return ResultUtil.error(1,"承运商不存在");
        }
        //车型是否存在
        CarType carType=carTypeMapper.selectByPrimaryKey(carTypeId);
        if(carType==null){
            return ResultUtil.error(1,"车型不存在");
        }
        //填入的高板低板长高、车宽的值不能比车型的值小或者大太多，即一般的数值
        if(highLength<carType.getHighlength()/2){
            return ResultUtil.error(1,"高板长不可小于"+carType.getHighlength()/2+"(mm)");
        }else if(highLength>carType.getHighlength()*1.5){
            return ResultUtil.error(1,"高板长不可大于"+carType.getHighlength()*1.5+"(mm)");
        }else if(highHeight<carType.getHighheight()/2){
            return ResultUtil.error(1,"高板高不可小于"+carType.getHighheight()/2+"(mm)");
        }else if(highHeight>carType.getHighheight()*1.5){
            return ResultUtil.error(1,"高板高不可大于"+carType.getHighheight()*1.5+"(mm)");
        }else if(lowLength<carType.getLowlength()/2){
            return ResultUtil.error(1,"低板长不可小于"+carType.getLowlength()/2+"(mm)");
        }else if(lowLength>carType.getLowlength()*1.5){
            return ResultUtil.error(1,"低板长不可大于"+carType.getLowlength()*1.5+"(mm)");
        }else if(lowHeight<carType.getLowheight()/2){
            return ResultUtil.error(1,"低板高不可小于"+carType.getLowheight()/2+"(mm)");
        }else if(lowHeight>carType.getLowheight()*1.5){
            return ResultUtil.error(1,"低板高不可大于"+carType.getLowheight()*1.5+"(mm)");
        }else if(carWidth<carType.getCarwidth()/2){
            return ResultUtil.error(1,"车宽不可小于"+carType.getCarwidth()/2+"(mm)");
        }else if(carWidth>carType.getCarwidth()*1.5){
            return ResultUtil.error(1,"车宽不可大于"+carType.getCarwidth()*1.5+"(mm)");
        }
        //添加
        Car car1=new Car();
        car1.setCarnumber(carNumber);
        car1.setCarrier(carrier);
        car1.setCartype(carType);
        car1.setDriver(driver);
        car1.setPhone(phone);
        car1.setHighlength(highLength);
        car1.setHighheight(highHeight);
        car1.setLowlength(lowLength);
        car1.setLowheight(lowHeight);
        car1.setCarwidth(carWidth);
        carMapper.insertSelective(car1);
        return ResultUtil.success();
    }

    /**
     * 修改车辆信息
     * @param id
     * @param carrierId
     * @param carTypeId
     * @param driver
     * @param phone
     * @param highLength
     * @param highHeight
     * @param lowLength
     * @param lowHeight
     * @param carWidth
     * @return
     */
    @Override
    public Result update(int id, int carrierId, int carTypeId, String driver, String phone, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth) {
        if(!driver.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"工厂名称只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!phone.matches("^[1]{1}[0-9]{10}$")){
            return ResultUtil.error(1,"手机号格式不正确");
        }
        Car car=carMapper.selectByPrimaryKey(id);
        if(car==null){
            return ResultUtil.error(1,"车辆信息不存在，刷新页面重试");
        }
        //承运商是否存在
        Carrier carrier=carrierMapper.selectByPrimaryKey(carrierId);
        if(carrier==null){
            return ResultUtil.error(1,"承运商不存在");
        }
        //车型是否存在
        CarType carType=carTypeMapper.selectByPrimaryKey(carTypeId);
        if(carType==null){
            return ResultUtil.error(1,"车型不存在");
        }
        car.setCarrier(carrier);
        car.setCartype(carType);
        car.setDriver(driver);
        car.setPhone(phone);
        car.setHighlength(highLength);
        car.setHighheight(highHeight);
        car.setLowlength(lowLength);
        car.setLowheight(lowHeight);
        car.setCarwidth(carWidth);
        carMapper.updateByPrimaryKeySelective(car);
        return ResultUtil.success();
    }

    /**
     * 删除车辆
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        carMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param carNumber
     * @param carrierId
     * @param carTypeId
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String carNumber, int carrierId, int carTypeId, int currentPage) {
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Car> list=carMapper.selectLimitByCondition(carNumber,carrierId ,carTypeId,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=carMapper.selectCountByCondition(carNumber,carrierId ,carTypeId);
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
     * 根据车型名称查询车辆信息
     * @param carTypeName
     * @return
     */
    @Override
    public Result findByCarType(String carTypeName) {
        CarType carType=carTypeMapper.selectByName(carTypeName);
        if(carType==null){
            return ResultUtil.error(1,"车型不存在");
        }
        List<Car> carList=carMapper.selectByCartypeid(carType.getId());
        if(carList.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(carList);
    }

    /**
     * 根据车牌号查询
     * @param carNumber
     * @return
     */
    @Override
    public Result carByCarnumber(String carNumber) {
        Car car=carMapper.selectByCarNumber(carNumber);
        if (car==null){
            return ResultUtil.error(1,"系统中没有这个车辆信息");
        }
        return ResultUtil.success(car);
    }
}
