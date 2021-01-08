package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.WarehouseEntryService;
import com.defei.lps.service.WarehouseOutService;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author 高德飞
 * @create 2020-12-17 15:16
 */
@Service
public class WarehouseOutServiceImp implements WarehouseOutService {
    @Autowired
    private WarehouseOutMapper warehouseOutMapper;
    @Autowired
    private WarehouseCacheMapper warehouseCacheMapper;
    @Autowired
    private TransportBillCacheMapper transportBillCacheMapper;
    @Autowired
    private WarehouseTakeMapper warehouseTakeMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private FactoryMapper factoryMapper;

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
        List<WarehouseOut> list=warehouseOutMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,warehouseId,date,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=warehouseOutMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,geelyBillNumber,warehouseId,date);
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
     * 出库操作。选择装载方案，填入车辆信息，修改方案内容后，点击确认按钮。
     * 1.保存出库记录
     * 2.删除或修改在库记录
     * 3.删除制装载方案记录
     * 4.添加在途运输单记录
     * @param billNumber 装载方案编号
     * @param carNumber 车牌号
     * @param driver 司机姓名
     * @param phone 手机号
     * @param carTypeName 车型
     * @param highLength 高板长
     * @param highHeight 高板高
     * @param lowLength 低板长
     * @param lowHeight 低板高
     * @param carWidth 车宽
     * @param carrierName 承运商
     * @param money 运输费
     * @param remarks 备注，一般是运输费有改动时填入说明
     * @return
     */
    @Override
    @Transactional
    public synchronized Result add(String billNumber, String carNumber, String driver, String phone, String carTypeName, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks) {
        if(billNumber.equals("")){
            return ResultUtil.error(1,"请先选择装载方案再出库");
        }
        //验证1.装载方案是否存在
        List<WarehouseTake> warehouseTakeList=warehouseTakeMapper.selectGroupGoodidByBillnumber(billNumber);
        if(warehouseTakeList.isEmpty()){
            return ResultUtil.error(1,"该装载方案未创建");
        }
        //验证2：装载方案是否已经出库了
        List<WarehouseOut> warehouseOutList=warehouseOutMapper.selectByBillnumber(billNumber);
        if(!warehouseOutList.isEmpty()){
            return ResultUtil.error(1,"该装载方案已经出库了");
        }
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now=new Date();
        //要删除的在库集合
        List<WarehouseCache> warehouseCacheListDelete=new ArrayList<>();
        //要修改的在库集合
        List<WarehouseCache> warehouseCacheListUpdate=new ArrayList<>();
        //添加的出库记录集合
        List<WarehouseOut> warehouseOutList1=new ArrayList<>();
        //添加的在途运输单集合
        List<TransportBillCache> transportBillCacheList=new ArrayList<>();
        for(WarehouseTake warehouseTake:warehouseTakeList){
            //添加出库记录
            WarehouseOut warehouseOut=new WarehouseOut();
            warehouseOut.setBillnumber(warehouseTake.getBillnumber());
            warehouseOut.setGood(warehouseTake.getGood());
            warehouseOut.setGeelybillnumber(warehouseTake.getGeelybillnumber());
            warehouseOut.setGeelycount(warehouseTake.getGeelycount());
            warehouseOut.setBatch(warehouseTake.getBatch());
            warehouseOut.setCount(warehouseTake.getCount());
            //箱数
            int boxCount=0;
            if(warehouseTake.getCount()%warehouseTake.getGood().getOneboxcount()==0){
                boxCount=warehouseTake.getCount()/warehouseTake.getGood().getOneboxcount();
            }else {
                boxCount=warehouseTake.getCount()/warehouseTake.getGood().getOneboxcount()+1;
            }
            warehouseOut.setBoxcount(boxCount);
            warehouseOut.setRemarks(remarks);
            warehouseOut.setUsername(userName);
            warehouseOut.setCreatetime(now);
            warehouseOut.setWarehouse(warehouseTake.getWarehouse());
            warehouseOutList1.add(warehouseOut);
            //添加在途运输单记录
            TransportBillCache transportBillCache=new TransportBillCache();
            transportBillCache.setBillnumber(transportBillCache.getBillnumber());
            transportBillCache.setGood(transportBillCache.getGood());
            transportBillCache.setGeelybillnumber(transportBillCache.getGeelybillnumber());
            transportBillCache.setGeelycount(transportBillCache.getGeelycount());
            transportBillCache.setBatch(transportBillCache.getBatch());
            transportBillCache.setCount(transportBillCache.getCount());
            transportBillCache.setBoxcount(transportBillCache.getBoxcount());
            transportBillCache.setStartname(transportBillCache.getStartname());
            transportBillCache.setStartnumber(transportBillCache.getStartnumber());
            transportBillCache.setEndname(transportBillCache.getEndname());
            transportBillCache.setEndnumber(transportBillCache.getEndnumber());
            transportBillCache.setRoutetype(transportBillCache.getRoutetype());
            transportBillCache.setCarnumber(transportBillCache.getCarnumber());
            transportBillCache.setDriver(transportBillCache.getDriver());
            transportBillCache.setPhone(transportBillCache.getPhone());
            transportBillCache.setCarriername(transportBillCache.getCarriername());
            transportBillCache.setCartypename(transportBillCache.getCartypename());
            transportBillCache.setHighlength(transportBillCache.getHighlength());
            transportBillCache.setHighheight(transportBillCache.getHighheight());
            transportBillCache.setLowlength(transportBillCache.getLowlength());
            transportBillCache.setLowheight(transportBillCache.getLowheight());
            transportBillCache.setCarwidth(transportBillCache.getCarwidth());
            transportBillCache.setMoney(transportBillCache.getMoney());
            transportBillCache.setRemarks(transportBillCache.getRemarks());
            transportBillCache.setUsername(userName);
            transportBillCache.setCreatetime(now);
            transportBillCacheList.add(transportBillCache);
            //修改或删除在库记录
            WarehouseCache warehouseCache=warehouseCacheMapper.selectByGeelybillnumberAndGoodidAndBatchAndOneboxcount(warehouseTake.getGeelybillnumber(),warehouseTake.getGood().getId(),warehouseTake.getOneboxcount(),warehouseTake.getBatch());
            if(warehouseCache==null){
                //如果在库记录数量-方案中数量<=0，那么就删除
                if(warehouseCache.getCount()-warehouseTake.getCount()<=0){
                    warehouseCacheListDelete.add(warehouseCache);
                }else {
                    //修改在库记录的数量、方案数量
                    warehouseCache.setCount(warehouseCache.getCount()-warehouseTake.getCount());
                    warehouseCache.setPlancount(warehouseCache.getPlancount()-warehouseTake.getCount());
                    warehouseCacheListUpdate.add(warehouseCache);
                }
            }
        }
        //1.保存出库记录
        warehouseOutMapper.insertBatch(warehouseOutList1);
        //2.删除在库记录
        warehouseCacheMapper.deleteBatch(warehouseCacheListDelete);
        //3.修改在库记录
        warehouseCacheMapper.updateBatch(warehouseCacheListUpdate);
        //4.添加在途运输单记录
        transportBillCacheMapper.insertBatch(transportBillCacheList);
        //5.删除装载方案记录
        warehouseTakeMapper.deteteBatch(warehouseTakeList);
        return ResultUtil.success();
    }

    /**
     * 生成出库记录、生成运输单
     * @param carTypeName
     * @param carNumber
     * @param driver
     * @param phone
     * @param highLength
     * @param highHeight
     * @param lowLength
     * @param lowHeight
     * @param carWidth
     * @param carrierName
     * @param money
     * @param remarks
     * @param goodInfo 要出库的物料信息，格式：warehouseCacheId,出库数量;warehouseCacheId,出库数量;...
     * @return
     */
    @Override
    public Result warehouseOutAdd(int startId,int endId,String routeType,String carTypeName, String carNumber, String driver, String phone, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String carrierName, String money, String remarks,String goodInfo) {
        //参数校验
        if(!driver.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_-]{1,10}$")){
            return ResultUtil.error(1,"司机姓名只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!carNumber.matches("^([京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][1-9DF][1-9ABCDEFGHJKLMNPQRSTUVWXYZ]\\d{3}[1-9DF]|[京津晋冀蒙辽吉黑沪苏浙皖闽赣鲁豫鄂湘粤桂琼渝川贵云藏陕甘青宁新][ABCDEFGHJKLMNPQRSTUVWXY][\\dABCDEFGHJKLNMxPQRSTUVWXYZ]{5})$")){
            return ResultUtil.error(1,"车牌格式不正确");
        }else if(!phone.matches("^[1]{1}[0-9]{10}$")){
            return ResultUtil.error(1,"手机号格式不正确");
        }else if(highLength<0){
            return ResultUtil.error(1,"高板长(mm)必须为0或正整数");
        }else if(highHeight<0){
            return ResultUtil.error(1,"高板高(mm)必须为0或正整数");
        }else if(lowLength<=0){
            return ResultUtil.error(1,"低板长(mm)必须为正整数");
        }else if(lowHeight<=0){
            return ResultUtil.error(1,"低板高(mm)必须为正整数");
        }else if(carWidth<=0){
            return ResultUtil.error(1,"车宽(mm)必须为正整数");
        }else if(carrierName.equals("")){
            return ResultUtil.error(1,"承运商必填");
        }else if(carTypeName.equals("")){
            return ResultUtil.error(1,"请选择车型");
        }else if(!money.equals("")){
            if(!money.matches("^\\d+(.\\d{1,2})?$")){
                return ResultUtil.error(1,"配送费用必须是最多保留2位小数的数字");
            }else if(remarks.equals("")){
                return ResultUtil.error(1,"填入了配送费，必须备注原因");
            }
        }
        //验证出发中转仓是否存在。出发地是中转仓
        Warehouse warehouse=warehouseMapper.selectByPrimaryKey(startId);
        if(warehouse==null){
            return ResultUtil.error(1,"出发地中转仓不存在，刷新后再试");
        }
        String startName=warehouse.getWarehousename();
        String startNumber=warehouse.getWarehousenumber();
        String endName="";
        String endNumber="";
        //验证目的地是否存在，验证出发地和目的地之间是否分配了线路
        if(routeType.equals("中转仓-工厂")){
            Factory factory=factoryMapper.selectByPrimaryKey(endId);
            if(factory==null){
                return ResultUtil.error(1,"目的地工厂不存在，刷新后再试");
            }
            endName=factory.getFactoryname();
            endNumber=factory.getFactorynumber();
        }else {
            Warehouse warehouse1=warehouseMapper.selectByPrimaryKey(endId);
            if(warehouse1==null){
                return ResultUtil.error(1,"目的地中转仓不存在，刷新后再试");
            }
            endName=warehouse1.getWarehousename();
            endNumber=warehouse1.getWarehousenumber();
        }
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        Date now=new Date();
        SimpleDateFormat simpleDateFormat1=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        //要删除的在库集合
        List<WarehouseCache> warehouseCacheListDelete=new ArrayList<>();
        //要修改的在库集合
        List<WarehouseCache> warehouseCacheListUpdate=new ArrayList<>();
        //添加的出库记录集合
        List<WarehouseOut> warehouseOutList=new ArrayList<>();
        //添加的在途运输单集合
        List<TransportBillCache> transportBillCacheList=new ArrayList<>();
        //获取运输单号：出发地编号+目的地编号+"-"+年4位+2位月+2位日+时分秒6位+3位毫秒
        String billNumber=startNumber+endNumber+"-"+simpleDateFormat1.format(now);
        //计算运输费
        BigDecimal moneyBig=BigDecimal.ZERO;
        //TODO
        if(!money.equals("")){
            //如果传入了配送费，那么就用传入的
            moneyBig=new BigDecimal(money);
        }
        //出库物料数量内容集合
        String[] infoList=goodInfo.split(";");
        for(int i=0;i<infoList.length;i++){
            if(infoList[i]==null){
                continue;
            }else if(infoList[i].equals("")){
                continue;
            }
            //中转仓在库记录ID
            int warehouseCacheId=Integer.parseInt(infoList[i].split(",")[0]);
            WarehouseCache warehouseCache=warehouseCacheMapper.selectByPrimaryKey(warehouseCacheId);
            if(warehouseCache==null){
                return ResultUtil.error(1,"选择的第"+(i+1)+"行中转仓在库记录不存在，请刷新页面后重试");
            }
            //出库的数量
            int count=Integer.parseInt(infoList[i].split(",")[1]);
            if(count>warehouseCache.getCount()){
                return ResultUtil.error(1,"选择的第"+(i+1)+"行中转仓在库记录的出库数量"+count+"大于了物料的在库数量"+warehouseCache.getCount());
            }
            //添加出库记录
            WarehouseOut warehouseOut=new WarehouseOut();
            warehouseOut.setBillnumber(billNumber);
            warehouseOut.setGood(warehouseCache.getGood());
            warehouseOut.setGeelybillnumber(warehouseCache.getGeelybillnumber());
            warehouseOut.setGeelycount(warehouseCache.getGeelycount());
            warehouseOut.setBatch(warehouseCache.getBatch());
            warehouseOut.setCount(count);
            //箱数
            int boxCount=0;
            if(count%warehouseCache.getOneboxcount()==0){
                boxCount=count/warehouseCache.getOneboxcount();
            }else {
                boxCount=count/warehouseCache.getOneboxcount()+1;
            }
            warehouseOut.setBoxcount(boxCount);
            warehouseOut.setRemarks(remarks);
            warehouseOut.setUsername(userName);
            warehouseOut.setCreatetime(now);
            warehouseOut.setWarehouse(warehouse);
            warehouseOutList.add(warehouseOut);
            //添加在途运输单记录
            TransportBillCache transportBillCache=new TransportBillCache();
            transportBillCache.setBillnumber(billNumber);
            transportBillCache.setGood(warehouseCache.getGood());
            transportBillCache.setGeelybillnumber(warehouseCache.getGeelybillnumber());
            transportBillCache.setGeelycount(warehouseCache.getGeelycount());
            transportBillCache.setBatch(warehouseCache.getBatch());
            transportBillCache.setCount(count);
            transportBillCache.setBoxcount(boxCount);
            transportBillCache.setStartname(startName);
            transportBillCache.setStartnumber(startNumber);
            transportBillCache.setEndname(endName);
            transportBillCache.setEndnumber(endNumber);
            transportBillCache.setRoutetype(routeType);
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
            transportBillCache.setMoney(moneyBig);
            transportBillCache.setRemarks(remarks);
            transportBillCache.setUsername(userName);
            transportBillCache.setCreatetime(now);
            transportBillCacheList.add(transportBillCache);
            //修改或删除在库记录
            //如果在库记录数量-方案中数量<=0，那么就删除
            if((warehouseCache.getCount()-count)<=0){
                warehouseCacheListDelete.add(warehouseCache);
            }else {
                //修改在库记录的数量
                warehouseCache.setCount(warehouseCache.getCount()-count);
                warehouseCacheListUpdate.add(warehouseCache);
            }
        }
        //1.保存出库记录
        warehouseOutMapper.insertBatch(warehouseOutList);
        //2.删除在库记录
        if(!warehouseCacheListDelete.isEmpty()){
            warehouseCacheMapper.deleteBatch(warehouseCacheListDelete);
        }
        //3.修改在库记录
        if(!warehouseCacheListUpdate.isEmpty()){
            warehouseCacheMapper.updateBatch(warehouseCacheListUpdate);
        }
        //4.添加在途运输单记录
        transportBillCacheMapper.insertBatch(transportBillCacheList);
        return ResultUtil.success(billNumber);
    }
}
