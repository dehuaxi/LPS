package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.WarehouseEntryService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 高德飞
 * @create 2020-12-17 15:16
 */
@Service
public class WarehouseEntryServiceImp implements WarehouseEntryService {
    @Autowired
    private WarehouseEntryMapper warehouseEntryMapper;
    @Autowired
    private WarehouseCacheMapper warehouseCacheMapper;
    @Autowired
    private TransportBillCacheMapper transportBillCacheMapper;
    @Autowired
    private TransportBillRecordMapper transportBillRecordMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;

    /**
     * 条件分页查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param geelyBillNumber
     * @param warehouseId
     * @param date
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber,String geelyBillNumber, int warehouseId, String date, int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!geelyBillNumber.matches("^[0-9A-Za-z]{0,20}$")){
            return ResultUtil.error(1,"吉利单号只能是1-20位的数字、大小写字母");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<WarehouseEntry> list=warehouseEntryMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,warehouseId,date,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=warehouseEntryMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,warehouseId,date);
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
     * 入库操作。扫描运输单号，显示内容，填入每个物料的实际收货数量后，点击收货按钮。
     * 1.保存入库记录
     * 2.保存在库记录
     * 3.在途运输单变为完结运输单记录
     * @param warehouseId 中转仓id
     * @param billNumber
     * @param goodInfos 物料实收情况,格式：在途运输单记录id,实收数量;在途运输单记录id,实收数量;...
     * @return
     */
    @Override
    @Transactional
    public synchronized Result add(int warehouseId,String billNumber, String goodInfos) {
        if(billNumber.equals("")){
            return ResultUtil.error(1,"请先扫描运输单再入库");
        }
        //验证1.在途运输单是否存在
        List<TransportBillCache> transportBillCacheList=transportBillCacheMapper.selectByBillnumber(billNumber);
        if(transportBillCacheList.isEmpty()){
            return ResultUtil.error(1,"该运输单未创建");
        }
        //验证：运输单是否已经入库了
        List<WarehouseEntry> warehouseEntryList=warehouseEntryMapper.selectByBillnumber(billNumber);
        if(!warehouseEntryList.isEmpty()){
            return ResultUtil.error(1,"该运输单已经入库了");
        }
        //验证2.中转仓是否存在
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(warehouseId);
        if(warehouse==null){
            return ResultUtil.error(1,"选择的中转仓不存在");
        }
        String[] infos=goodInfos.split(";");
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now=new Date();
        //在途运输单记录获取对应的实收数量。把在途运输单记录变为完结记录
        List<TransportBillRecord> transportBillRecords=new ArrayList<>();
        List<Map<String,Object>> list=new ArrayList<>();
        for(TransportBillCache transportBillCache:transportBillCacheList){
            //获取在途记录的对应数量，并检验实收数量是否大于PD单中数量
            Map<String,Object> map=new HashMap<>();
            map.put("transportBillCache",transportBillCache);
            int realCount=0;
            for(String info:infos){
                //看是否有实收数量
                if(info.split(",").length!=2){
                    list=null;
                    transportBillRecords=null;
                    return ResultUtil.error(1,"有物料没有填写实收数量");
                }else {
                    //有实收数量，看填入的实收数量是否是0或正整数
                    if(!info.split(",")[1].matches("^[0-9]{1,11}$")){
                        list=null;
                        transportBillRecords=null;
                        return ResultUtil.error(1,"有物料的实收数量不是大于等于0的数字");
                    }else {
                        //验证实收数量是否大于PD单数量和运输单数量
                        if(info.split(",")[0].equals(String.valueOf(transportBillCache.getId()))){
                            realCount=Integer.parseInt(info.split(",")[1]);
                            if(realCount>transportBillCache.getGeelycount()){
                                list=null;
                                transportBillRecords=null;
                                return ResultUtil.error(1,"吉利单据"+transportBillCache.getGeelybillnumber()+"中物料"+transportBillCache.getGood().getGoodcode()+"的实收数量大于了吉利单据中的数量，请把多余的数量补一张吉利单据绑定到当前运输单后再收货");
                            }
                            if(realCount!=transportBillCache.getCount()){
                                //实收数量不等于上次实收数，那么备注中添加说明
                                transportBillCache.setRemarks("运输单数量为"+transportBillCache.getCount()+"，本次实收为"+realCount+"。"+transportBillCache.getRemarks());
                            }
                        }
                    }
                }
            }
            map.put("realCount",realCount);
            list.add(map);
            //添加完结记录
            TransportBillRecord transportBillRecord=new TransportBillRecord();
            transportBillRecord.setBillnumber(transportBillCache.getBillnumber());
            transportBillRecord.setGood(transportBillCache.getGood());
            transportBillRecord.setGeelybillnumber(transportBillCache.getGeelybillnumber());
            transportBillRecord.setGeelycount(transportBillCache.getGeelycount());
            transportBillRecord.setBatch(transportBillCache.getBatch());
            transportBillRecord.setCount(transportBillCache.getCount());
            transportBillRecord.setBoxcount(transportBillCache.getBoxcount());
            transportBillRecord.setStartname(transportBillCache.getStartname());
            transportBillRecord.setStartnumber(transportBillCache.getStartnumber());
            transportBillRecord.setEndname(transportBillCache.getEndname());
            transportBillRecord.setEndnumber(transportBillCache.getEndnumber());
            transportBillRecord.setRoutetype(transportBillCache.getRoutetype());
            transportBillRecord.setCarnumber(transportBillCache.getCarnumber());
            transportBillRecord.setDriver(transportBillCache.getDriver());
            transportBillRecord.setPhone(transportBillCache.getPhone());
            transportBillRecord.setCarriername(transportBillCache.getCarriername());
            transportBillRecord.setCartypename(transportBillCache.getCartypename());
            transportBillRecord.setHighlength(transportBillCache.getHighlength());
            transportBillRecord.setHighheight(transportBillCache.getHighheight());
            transportBillRecord.setLowlength(transportBillCache.getLowlength());
            transportBillRecord.setLowheight(transportBillCache.getLowheight());
            transportBillRecord.setCarwidth(transportBillCache.getCarwidth());
            transportBillRecord.setMoney(transportBillCache.getMoney());
            transportBillRecord.setRemarks(transportBillCache.getRemarks());
            transportBillRecord.setCreateusername(transportBillCache.getUsername());
            transportBillRecord.setCreatetime(simpleDateFormat.format(transportBillCache.getCreatetime()));
            transportBillRecord.setOverusername(userName);
            transportBillRecord.setOvertime(now);
            transportBillRecords.add(transportBillRecord);
        }
        //批量保存完结记录
        transportBillRecordMapper.insertBatch(transportBillRecords);
        //保存入库、在库记录、删除在途运输单记录
        for(Map<String,Object> map:list){
            TransportBillCache transportBillCache=(TransportBillCache)map.get("transportBillCache");
            int realCount=(Integer) map.get("realCount");
            WarehouseEntry warehouseEntry=new WarehouseEntry();
            warehouseEntry.setBillnumber(transportBillCache.getBillnumber());
            warehouseEntry.setGood(transportBillCache.getGood());
            warehouseEntry.setGeelybillnumber(transportBillCache.getGeelybillnumber());
            warehouseEntry.setGeelycount(transportBillCache.getGeelycount());
            warehouseEntry.setBatch(transportBillCache.getBatch());
            warehouseEntry.setCount(realCount);
            //箱数
            int boxCount=0;
            if(realCount%transportBillCache.getGood().getOneboxcount()==0){
                boxCount=realCount/transportBillCache.getGood().getOneboxcount();
            }else {
                boxCount=realCount/transportBillCache.getGood().getOneboxcount()+1;
            }
            warehouseEntry.setBoxcount(boxCount);
            warehouseEntry.setRemarks(transportBillCache.getRemarks());
            warehouseEntry.setUsername(userName);
            warehouseEntry.setCreatetime(now);
            warehouseEntry.setWarehouse(warehouse);
            warehouseEntryMapper.insertSelective(warehouseEntry);
            //在库记录：如果PD单号、物料id、批次、收容数都一样，则合并
            WarehouseCache oldWarehouseCache=warehouseCacheMapper.selectByGeelybillnumberAndGoodidAndBatchAndOneboxcount(transportBillCache.getGeelybillnumber(),transportBillCache.getGood().getId(),transportBillCache.getGood().getOneboxcount(),transportBillCache.getBatch());
            if(oldWarehouseCache!=null){
                //合并数量
                oldWarehouseCache.setCount(oldWarehouseCache.getCount()+realCount);
                warehouseCacheMapper.updateByPrimaryKeySelective(oldWarehouseCache);
            }else {
                //新增记录
                WarehouseCache warehouseCache=new WarehouseCache();
                warehouseCache.setGood(transportBillCache.getGood());
                warehouseCache.setGeelybillnumber(transportBillCache.getGeelybillnumber());
                warehouseCache.setGeelycount(transportBillCache.getGeelycount());
                warehouseCache.setBatch(transportBillCache.getBatch());
                warehouseCache.setCount(realCount);
                warehouseCache.setPlancount(0);
                warehouseCache.setOneboxcount(transportBillCache.getGood().getOneboxcount());
                warehouseCache.setBoxcount(boxCount);
                warehouseCache.setPackstate("未翻包");
                warehouseCache.setCreatetime(now);
                warehouseCache.setWarehouse(warehouse);
                warehouseCacheMapper.insertSelective(warehouseCache);
            }
            //删除在途运输单记录
            transportBillCacheMapper.deleteByPrimaryKey(transportBillCache.getId());
        }
        return ResultUtil.success();
    }
}
