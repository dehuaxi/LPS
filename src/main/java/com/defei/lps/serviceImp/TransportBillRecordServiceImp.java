package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.TransportBillRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

/**
 * @author 高德飞
 * @create 2020-12-15 13:40
 */
@Service
public class TransportBillRecordServiceImp implements TransportBillRecordService {
    @Autowired
    private TransportBillRecordMapper transportBillRecordMapper;

    /**
     * 运输单明细分页条件查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param billNumber
     * @param geelyBillNumber
     * @param dateStart
     * @param dateEnd
     * @param carNumber
     * @param carTypeName
     * @param carrierName
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber, String geelyBillNumber, String dateStart,String dateEnd,String carNumber, String carTypeName, String carrierName, int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!billNumber.matches("^[0-9A-Za-z-]{0,30}$")){
            return ResultUtil.error(1,"运输单号只能是1-30位的数字、大小写字母、特殊字符(-)");
        }else if(!geelyBillNumber.matches("^[0-9A-Za-z]{0,20}$")){
            return ResultUtil.error(1,"吉利单号只能是1-20位的数字、大小写字母");
        }
        if(!carNumber.equals("")){
            if(!carNumber.matches("^([京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][1-9DF][1-9ABCDEFGHJKLMNPQRSTUVWXYZ]\\d{3}[1-9DF]|[京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][\\dABCDEFGHJKLNMxPQRSTUVWXYZ]{5})$")){
                return ResultUtil.error(1,"车牌格式不正确");
            }
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<TransportBillRecord> list=transportBillRecordMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=transportBillRecordMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName);
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

}
