package com.defei.lps.serviceImp;

import com.defei.lps.dao.CarrierMapper;
import com.defei.lps.entity.Carrier;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.CarrierService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 高德飞
 * @create 2020-11-18 16:53
 */
@Service
public class CarrierServiceImp implements CarrierService {
    @Autowired
    private CarrierMapper carrierMapper;

    /**
     * 添加承运商
     * @param carrierName
     * @param carrierNumber
     * @param contact
     * @param phone
     * @param address
     * @return
     */
    @Override
    public Result add(String carrierName, String carrierNumber, String contact, String phone, String address) {
        //校验参数
        if(!carrierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,20}$")){
            return ResultUtil.error(1,"承运商名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!carrierNumber.matches("^[0-9A-Za-z#@_-]{1,10}$")){
            return ResultUtil.error(1,"承运商编号只能是1-10位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!contact.matches("^[0-9A-Za-z\\u4e00-\\u9fa5]{0,5}$")){
            return ResultUtil.error(1,"联系人只能是1-5位的数字、大小写怎么、汉字");
        }else if(!phone.matches("^[0-9-]{0,15}$")){
            return ResultUtil.error(1,"电话只能是1-15位的数字、特殊字符(-)");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,50}$")){
            return ResultUtil.error(1,"地址只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }
        //根据承运商名称查询看是否重复
        Carrier carrier=carrierMapper.selectByCarriername(carrierName);
        if(carrier!=null){
            return ResultUtil.error(1,"承运商名称已经存在");
        }
        //根据承运商编号查询看是否重复
        Carrier carrier1=carrierMapper.selectByCarriernumber(carrierNumber);
        if(carrier1!=null){
            return ResultUtil.error(1,"承运商编号已经存在");
        }
        Carrier carrier2=new Carrier();
        carrier2.setCarriername(carrierName);
        carrier2.setCarriernumber(carrierNumber);
        carrier2.setContact(contact);
        carrier2.setPhone(phone);
        carrier2.setAddress(address);
        int result=carrierMapper.insertSelective(carrier2);
        if(result>0){
            return ResultUtil.success();
        }
        return ResultUtil.error(1,"添加过程发生错误，请联系管理员");
    }

    /**
     * 修改
     * @param id
     * @param contact
     * @param phone
     * @param address
     * @return
     */
    @Override
    public Result update(int id, String contact, String phone, String address) {
        //校验参数
        if(!contact.matches("^[0-9A-Za-z\\u4e00-\\u9fa5]{0,5}$")){
            return ResultUtil.error(1,"联系人只能是1-5位的数字、大小写怎么、汉字");
        }else if(!phone.matches("^[0-9-]{0,15}$")){
            return ResultUtil.error(1,"电话只能是1-15位的数字、特殊字符(-)");
        }else if(!address.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,50}$")){
            return ResultUtil.error(1,"地址只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }
        Carrier carrier=carrierMapper.selectByPrimaryKey(id);
        if(carrier==null){
            return ResultUtil.error(1,"承运商不存在，刷新页面后重试");
        }
        carrier.setContact(contact);
        carrier.setPhone(phone);
        carrier.setAddress(address);
        int result=carrierMapper.updateByPrimaryKeySelective(carrier);
        if(result<=0){
            return ResultUtil.error(1,"修改过程发生错误，请联系管理员");
        }
        return ResultUtil.success();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        carrierMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param carrierName
     * @param carrierNumber
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String carrierName, String carrierNumber, int currentPage) {
        //校验参数
        if(!carrierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{0,20}$")){
            return ResultUtil.error(1,"承运商名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!carrierNumber.matches("^[0-9A-Za-z#@_-]{0,10}$")){
            return ResultUtil.error(1,"承运商编号只能是1-10位的数字、大小写字母、特殊字符(@#_-)");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Carrier> list=carrierMapper.selectLimitByCondition(carrierName,carrierNumber ,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=carrierMapper.selectCountByCondition(carrierName,carrierNumber);
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
     * 查询所有
     * @return
     */
    @Override
    public Result allCarrier() {
        //定义集合存放返回页面的区域信息
        List<Carrier> carrierList=carrierMapper.selectAll();
        if(!carrierList.isEmpty()){
            return ResultUtil.success(carrierList);
        }
        return ResultUtil.success();
    }
}
