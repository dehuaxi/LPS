package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.TransportBillCacheService;
import org.apache.poi.ss.formula.functions.T;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 高德飞
 * @create 2020-12-15 13:40
 */
@Service
public class TransportBillCacheServiceImp implements TransportBillCacheService {
    @Autowired
    private TransportBillCacheMapper transportBillCacheMapper;
    @Autowired
    private PlanTakeMapper planTakeMapper;
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private PlanTakeRecordMapper planTakeRecordMapper;
    @Autowired
    private GoodMapper goodMapper;
    @Autowired
    private GeelyBillCacheMapper geelyBillCacheMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private TransportBillRecordMapper transportBillRecordMapper;

    /**
     * 取货计划绑定吉利单号页面，生成运输单
     * @param planNumber 取货计划编号
     * @param carNumber 车牌号
     * @param driver 司机姓名
     * @param phone 司机手机号
     * @param carTypeName 车型名称
     * @param highLength 高板长
     * @param highHeight 高板高
     * @param lowLength 低板长
     * @param lowHeight 低板高
     * @param carWidth 车宽
     * @param carrierName 承运商名称
     * @param money 自定义运输费
     * @param remarks 备注
     * @param geelyRealInfo PD单以及实收数据，格式：goodid,吉利PD单号,实收数量;goodid,吉利PD单号,实收数量;...
     * @return
     */
    @Override
    @Transactional
    public synchronized Result addPlan(String planNumber, String carNumber, String driver, String phone, String carTypeName, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks, String geelyRealInfo) {
        //根据取货计划编号获取取货计划详情。因为同物料可能使用的车高不一样，所以会有2条取货计划，所以查询时要以物料id分组，并把数量求和
        List<PlanTake> planTakeList=planTakeMapper.selectGroupByPlannumber(planNumber);
        if(planTakeList.isEmpty()){
            return ResultUtil.error(1,"选择的取货计划不存在，无法生成运输单");
        }
        String[] pdList=geelyRealInfo.split(";");
        //验证：上传的物料是否全部都是取货计划中的物料
        for(PlanTake planTake:planTakeList){
            //是否上次，默认为未上传
            boolean isUpload=false;
            for(String info:pdList){
                if(planTake.getGood().getId()==Integer.parseInt(info.split(",")[0])){
                    isUpload=true;
                    //跳出里面循环，继续验证下一个物料是否上传了
                    break;
                }
            }
            if(isUpload==false){
                return ResultUtil.error(1,"物料"+planTake.getGood().getGoodcode()+"的吉利单据未上传，无法生成运输单");
            }
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now=new Date();
        //当前用户
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        //生成运输单
        List<TransportBillCache> transportBillCacheList=new ArrayList<>();
        //计算运费
        String moneyC="0";
        if(!money.equals("")){
            //如果传入的运费不为空，说明是改动过运费，需要以改动过的运费为准
            moneyC=money;
        }else {
            //系统根据设置好的自动计算运费

        }
        for(String info:pdList){
            //PD单号
            String geelyBillNumber=info.split(",")[1];
            if(geelyBillNumber==null||geelyBillNumber.equals("")){
                return ResultUtil.error(1,"吉利单据号参数缺失，无法生成运输单");
            }
            //物料id
            String goodId=info.split(",")[0];
            if(!goodId.matches("^[1-9]{1}[0-9]{0,10}$")){
                return ResultUtil.error(1,"吉利单据"+geelyBillNumber+"的物料信息参数缺失，无法生成运输单");
            }
            String realCount=info.split(",")[2];
            if(!realCount.matches("^[1-9]{1}[0-9]{0,10}$")){
                return ResultUtil.error(1,"吉利单据"+geelyBillNumber+"的实收数没填，无法生成运输单");
            }
            TransportBillCache transportBillCache=new TransportBillCache();
            //运输单号就为取货计划编号
            transportBillCache.setBillnumber(planNumber);
            Good good=goodMapper.selectByPrimaryKey(Integer.parseInt(goodId));
            if(good==null){
                transportBillCacheList=null;
                return ResultUtil.error(1,"吉利单据"+geelyBillNumber+"中实收为"+realCount+"的物料在系统中不存在");
            }
            transportBillCache.setGood(good);
            transportBillCache.setGeelybillnumber(info.split(",")[1]);
            GeelyBillCache geelyBillCache=geelyBillCacheMapper.selectByGoodidAndBillnumber(Integer.parseInt(goodId),geelyBillNumber);
            if(geelyBillCache==null){
                transportBillCacheList=null;
                return ResultUtil.error(1,"吉利单据"+geelyBillNumber+"在系统中不存在");
            }
            transportBillCache.setGeelycount(geelyBillCache.getCount());
            transportBillCache.setBatch(geelyBillCache.getBatch());
            transportBillCache.setCount(Integer.parseInt(realCount));
            //计算箱数
            int boxCount=0;
            if(Integer.parseInt(realCount)%good.getOneboxcount()==0){
                boxCount=Integer.parseInt(realCount)/good.getOneboxcount();
            }else {
                boxCount=Integer.parseInt(realCount)/good.getOneboxcount()+1;
            }
            transportBillCache.setBoxcount(boxCount);
            transportBillCache.setStartname(planTakeList.get(0).getStartname());
            transportBillCache.setStartnumber(planTakeList.get(0).getStartnumber());
            transportBillCache.setEndname(planTakeList.get(0).getEndname());
            transportBillCache.setEndnumber(planTakeList.get(0).getEndnumber());
            transportBillCache.setRoutetype(planTakeList.get(0).getRoutetype());
            transportBillCache.setCarnumber(carNumber);
            transportBillCache.setDriver(driver);
            transportBillCache.setPhone(phone);
            transportBillCache.setCarriername(carrierName);
            transportBillCache.setCartypename(carTypeName);
            transportBillCache.setHighlength(highLength);
            transportBillCache.setHighheight(highHeight);
            transportBillCache.setLowlength(lowLength);
            transportBillCache.setLowheight(lowHeight);
            transportBillCache.setCarwidth(carWidth);
            transportBillCache.setMoney(new BigDecimal(moneyC));
            transportBillCache.setRemarks(remarks);
            transportBillCache.setUsername(userName);
            transportBillCache.setCreatetime(now);
            transportBillCacheList.add(transportBillCache);
        }
        //批量添加运输单记录
        transportBillCacheMapper.insertBatch(transportBillCacheList);
        //添加取货计划完成记录、删除取货计划记录、修改缺件计划的取货数量。根据goodid分组求取货数量的和.
        for(PlanTake planTake:planTakeList){
            //根据物料id查询上传的PD单的实收数量是多少
            int realCount=0;
            for(String info:pdList){
                if(info.split(",")[0].equals(String.valueOf(planTake.getGood().getId()))){
                    realCount+=Integer.parseInt(info.split(",")[2]);
                }
            }
            //获取该物料对应的取货计划，看是1条还是多条。修改取货计划的实收数量
            List<PlanTake> planTakeList1=planTakeMapper.selectByPlannumberAndGoodid(planNumber,planTake.getGood().getId());
            if(!planTakeList1.isEmpty()){
                int lastCount=realCount;
                //修改取货计划的实收数量。然后把这些取货计划删掉，保存为取货计划完成记录
                for(PlanTake planTake1:planTakeList1){
                    //1.获取当前取货计划的实收数
                    int currentRealCount=0;
                    if(lastCount<=planTake1.getCount()){
                        currentRealCount=lastCount;
                        //修改剩余数量，以便下次循环使用
                        lastCount=0;
                    }else {
                        //如果剩余数量>当前取货计划的取货数量，那么当前取货计划的实收数量=取货数量
                        currentRealCount=planTake1.getCount();
                        //修改剩余数量，以便下次循环使用
                        lastCount=lastCount-planTake1.getCount();
                    }
                    //2.保存取货计划完成记录
                    PlanTakeRecord planTakeRecord=new PlanTakeRecord();
                    planTakeRecord.setPlannumber(planNumber);
                    planTakeRecord.setGood(planTake1.getGood());
                    planTakeRecord.setCount(planTake1.getCount());
                    planTakeRecord.setRealcount(currentRealCount);
                    planTakeRecord.setBoxcount(planTake1.getBoxcount());
                    planTakeRecord.setLength(planTake1.getLength());
                    planTakeRecord.setVolume(planTake1.getVolume());
                    planTakeRecord.setWeight(planTake1.getWeight());
                    planTakeRecord.setDate(planTake1.getDate());
                    planTakeRecord.setStartname(planTake1.getStartname());
                    planTakeRecord.setStartnumber(planTake1.getStartnumber());
                    planTakeRecord.setEndname(planTake1.getEndname());
                    planTakeRecord.setEndnumber(planTake1.getEndnumber());
                    planTakeRecord.setRoutetype(planTake1.getRoutetype());
                    planTakeRecord.setCartype(planTake1.getCartype());
                    planTakeRecord.setHighlength(planTake1.getHighlength());
                    planTakeRecord.setHighheight(planTake1.getHighheight());
                    planTakeRecord.setLowlength(planTake1.getLowlength());
                    planTakeRecord.setLowheight(planTake1.getLowheight());
                    planTakeRecord.setCarwidth(planTake1.getCarwidth());
                    planTakeRecord.setUsername(planTake1.getUsername());
                    planTakeRecord.setCreatetime(simpleDateFormat.format(planTake1.getCreatetime()));
                    planTakeRecord.setOvertime(now);
                    planTakeRecordMapper.insertSelective(planTakeRecord);
                    //3.删除取货计划
                    planTakeMapper.deleteByPrimaryKey(planTake1.getId());
                }
            }
            //修改该物料对应的缺件计划的取货数量
            PlanCache planCache=planCacheMapper.selectByGoodidAndDate(planTake.getGood().getId(),planTake.getDate().substring(0,10));
            if(planCache!=null){
                planCache.setTakecount(planCache.getTakecount()+realCount);
                planCacheMapper.updateByPrimaryKeySelective(planCache);
            }
        }
        //返回运输单号
        return ResultUtil.success(planNumber);
    }

    /**
     * 中转仓在库物料拼车，生成运输单
     * @param carNumber
     * @param driver
     * @param phone
     * @param carTypeName
     * @param highLength
     * @param highHeight
     * @param lowLength
     * @param lowHeight
     * @param carWidth
     * @param carrierName
     * @param money
     * @param remarks
     * @param geelyBillInfo
     * @return
     */
    @Override
    public Result addBillStock(String carNumber, String driver, String phone, String carTypeName, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks, String geelyBillInfo) {

        return null;
    }

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
        List<TransportBillCache> list=transportBillCacheMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=transportBillCacheMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName);
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
     * 运输单条件分页查询，以运输单号分组
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
    public Result findAllByBillnumber(String billNumber, String geelyBillNumber, String dateStart,String dateEnd, String carNumber, String carTypeName, String carrierName, int currentPage) {
        //校验参数
        if(!billNumber.matches("^[0-9A-Za-z-]{0,30}$")){
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
        List<TransportBillCache> list=transportBillCacheMapper.selectBillLimitByCondition(billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=transportBillCacheMapper.selectBillCountByCondition(billNumber,geelyBillNumber,dateStart,dateEnd,carNumber,carTypeName,carrierName);
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
     * 据运输单号获取明细
     * @param billNumber
     * @return
     */
    @Override
    public Result findDetailByBillnumber(String billNumber) {
        List<TransportBillCache> transportBillCacheList=transportBillCacheMapper.selectByBillnumber(billNumber);
        if(transportBillCacheList.isEmpty()){
            return ResultUtil.error(1,"该运输单不存在");
        }
        return ResultUtil.success(transportBillCacheList);
    }

    /**
     * 根据传入的在途运输单单号、想要入库的中转仓id，来进行判断，是否可以入库，如果可以就返回运输单详情
     * @param billNumber
     * @param warehouseId
     * @return
     */
    @Override
    public Result transportBillCacheBillDetail2(String billNumber, int warehouseId) {
        List<TransportBillCache> transportBillCacheList=transportBillCacheMapper.selectByBillnumber(billNumber);
        if(transportBillCacheList.isEmpty()){
            return ResultUtil.error(1,"该运输单不存在");
        }
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(warehouseId);
        if(warehouse==null){
            return ResultUtil.error(1,"选择的中转仓不存在");
        }
        if(!transportBillCacheList.get(0).getEndnumber().equals(warehouse.getWarehousenumber())){
            return ResultUtil.error(1,"选择的中转仓不是运输单的目的地中转仓");
        }
        return ResultUtil.success(transportBillCacheList);
    }

    /**
     * 运输单确认到达目的地操作
     * @param billNumber
     * @return
     */
    @Override
    public Result transportBillRecordAdd(String billNumber) {
        List<TransportBillCache> transportBillCacheList=transportBillCacheMapper.selectByBillnumber(billNumber);
        if(transportBillCacheList.isEmpty()){
            return ResultUtil.error(1,"该运输单不存在");
        }
        String userName=(String)SecurityUtils.getSubject().getPrincipal();
        Date now=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        List<TransportBillRecord> transportBillRecords=new ArrayList<>();
        for(TransportBillCache transportBillCache:transportBillCacheList){
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
        //1.添加运输单完成记录
        transportBillRecordMapper.insertBatch(transportBillRecords);
        //2.删除运输单在途记录
        transportBillCacheMapper.deleteBatch(transportBillCacheList);
        return ResultUtil.success();
    }
}
