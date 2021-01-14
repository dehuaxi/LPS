package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.PlanTakeService;
import com.defei.lps.uploadUtil.PdfBoxUtilLinHai;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 高德飞
 * @create 2020-12-08 17:14
 */
@Service
public class PlanTakeServiceImp implements PlanTakeService {
    @Autowired
    private PlanTakeMapper planTakeMapper;
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private GeelyBillCacheMapper geelyBillCacheMapper;
    @Autowired
    private ShortageMapper shortageMapper;
//---------------------------取货计划生成页面---------------------------
    /**
     * 取货计划装载方案生成页面，生成方案
     * 1.添加取货计划
     * 2.添加在途缺件计划。即把原未确认缺件计划改为在途计划
     * @param startId
     * @param endId
     * @param endType
     * @param date
     * @param carType
     * @param highLength
     * @param highHeight
     * @param lowLength
     * @param lowHeight
     * @param carWidth
     * @param planCacheInfos 方案内容，格式：计划id,车高mm,数量,箱数,长度,体积,重量;计划id,车高mm,数量,箱数,长度,体积,重量;...
     * @return
     */
    @Override
    @Transactional
    public synchronized Result add(int startId, int endId, String endType, String date, String carType, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String planCacheInfos) {
        //验证出发区域是否存在。出发地是区域
        Area area=areaMapper.selectByPrimaryKey(startId);
        if(area==null){
            return ResultUtil.error(1,"区域不存在，刷新后再试");
        }
        String endName="";
        String endNumber="";
        String routeType="";
        //验证目的地是否存在，验证出发地和目的地之间是否分配了线路
        if(endType.equals("工厂")){
            Factory factory=factoryMapper.selectByPrimaryKey(endId);
            if(factory==null){
                return ResultUtil.error(1,"工厂不存在，刷新后再试");
            }
            Route route=routeMapper.selectByFactoryidAndAreaid(endId,startId);
            if(route==null){
                return ResultUtil.error(1,"区域和工厂未创建线路");
            }
            endName=factory.getFactoryname();
            endNumber=factory.getFactorynumber();
            routeType="区域-工厂";
        }else {
            Warehouse warehouse=warehouseMapper.selectByPrimaryKey(endId);
            if(warehouse==null){
                return ResultUtil.error(1,"中转仓不存在，刷新后再试");
            }
            //查询线路集合
            List<Route> routeList1=routeMapper.selectByAreaidAndWarehouseid(startId,endId);
            if(routeList1.isEmpty()){
                return ResultUtil.error(1,"中转仓未分配到任何线路中");
            }
            endName=warehouse.getWarehousename();
            endNumber=warehouse.getWarehousenumber();
            routeType="区域-中转仓";
        }
        //获取方案内容,并验证每个缺件计划是否都存在
        String[] infos=planCacheInfos.split(";");
        List<Map<String,Object>> infoList=new ArrayList<>();
        for(String info:infos){
            Map<String,Object> map=new HashMap<>();
            //获取缺件计划
            PlanCache planCache=planCacheMapper.selectByPrimaryKey(Integer.parseInt(info.split(",")[0]));
            if(planCache==null){
                return ResultUtil.error(1,"有选中的计划不存在，刷新页面后再试");
            }else {
                map.put("planCache",planCache);
                //车高
                map.put("carHeight",Integer.parseInt(info.split(",")[1]));
                //数量
                map.put("count",Integer.parseInt(info.split(",")[2]));
                //箱数
                map.put("boxCount",Integer.parseInt(info.split(",")[3]));
                //长度m
                map.put("length",info.split(",")[4]);
                //体积立方米
                map.put("volume",info.split(",")[5]);
                //重量t
                map.put("weight",info.split(",")[6]);
                infoList.add(map);
            }
        }
        //生成计划编号：出发地编号+目的地编号+"-"+年4位+2位月+2位日+时分秒6位+3位毫秒
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now=new Date();
        String planNumber=area.getAreanumber()+endNumber+"-"+simpleDateFormat.format(now);
        //获取当前账号
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        //String today=new SimpleDateFormat("yyyy-MM-dd").format(now);
        //1.保存取货计划  2.添加在途缺件计划
        for(Map map:infoList){
            PlanCache planCache1=(PlanCache)map.get("planCache");
            //1.添加在途缺件计划,就相当于修改了原未确认计划。
            planCache1.setCount((Integer)map.get("count"));
            planCache1.setState("未取货");
            planCache1.setDate(date);
            planCache1.setSurecount((Integer)map.get("count"));
            //确认的箱号
            int boxCount=0;
            if((Integer)map.get("count")%planCache1.getGood().getOneboxcount()!=0){
                boxCount=((Integer)map.get("count")/planCache1.getGood().getOneboxcount()+1)*planCache1.getGood().getOneboxcount();
            }else {
                boxCount=(Integer)map.get("count")/planCache1.getGood().getOneboxcount();
            }
            planCache1.setBoxcount(boxCount);
            planCacheMapper.updateByPrimaryKeySelective(planCache1);
            //2.添加取货计划
            PlanTake planTake=new PlanTake();
            planTake.setPlancacheid(planCache1.getId());
            planTake.setPlannumber(planNumber);
            planTake.setGood(planCache1.getGood());
            planTake.setCount((Integer)map.get("count"));
            planTake.setRealcount(0);
            planTake.setBoxcount((Integer)map.get("boxCount"));
            planTake.setLength(new BigDecimal((String)map.get("length")));
            planTake.setVolume(new BigDecimal((String)map.get("volume")));
            planTake.setWeight(new BigDecimal((String)map.get("weight")));
            planTake.setDate(date);
            planTake.setStartname(area.getAreaname());
            planTake.setStartnumber(area.getAreanumber());
            planTake.setEndname(endName);
            planTake.setEndnumber(endNumber);
            planTake.setRoutetype(routeType);
            planTake.setCartype(carType);
            planTake.setHighlength(highLength);
            planTake.setHighheight(highHeight);
            planTake.setLowlength(lowLength);
            planTake.setLowheight(lowHeight);
            planTake.setCarheight((Integer)map.get("carHeight"));
            planTake.setCarwidth(carWidth);
            planTake.setUsername(userName);
            planTake.setCreatetime(now);
            planTakeMapper.insertSelective(planTake);
        }
        /*//1.添加取货计划、2.添加在途缺件计划、3.删除未确认缺件计划根据添加的在途缺件计划重新生成未确认缺件计划
        //取货计划集合
        List<PlanTake> planTakeList=new ArrayList<>();
        //重新计算后新增的缺件计划
        List<PlanCache> planCacheList=new ArrayList<>();
        for(Map map:infoList){
            PlanCache planCache1=(PlanCache)map.get("planCache");
            //1.根据修改后的在途计划重新计算未确认缺件计划
            //先把从今天到最大日期中，每天的到货在途计划的到货数量加到缺件报表记录的结存中，再拿添加了到货的缺件报表来做计划计算
            List<Shortage> shortageList=shortageMapper.selectByGoodidAndDatestartAndDateend(planCache1.getGood().getId(),today,"");
            if(shortageList.isEmpty()){
                continue;
            }
            //2.添加在途缺件计划,就相当于修改了原未确认计划。
            planCache1.setCount((Integer)map.get("count"));
            planCache1.setState("未取货");
            planCache1.setDate(date);
            planCache1.setSurecount((Integer)map.get("count"));
            //确认的箱号
            int boxCount=0;
            if((Integer)map.get("count")%planCache1.getGood().getOneboxcount()!=0){
                boxCount=((Integer)map.get("count")/planCache1.getGood().getOneboxcount()+1)*planCache1.getGood().getOneboxcount();
            }else {
                boxCount=(Integer)map.get("count")/planCache1.getGood().getOneboxcount();
            }
            planCache1.setBoxcount(boxCount);
            planCacheMapper.updateByPrimaryKeySelective(planCache1);
            //添加在途计划后，删除未确认计划
            planCacheMapper.deleteByGoodidAndState(planCache1.getGood().getId(),"未确认");
            //把到货数量更新到缺件报表记录中
            for(int i=0;i<shortageList.size();i++){
                //查询是否有当天到货的在途记录
                List<PlanCache> planCacheList1=planCacheMapper.selectByGoodidAndReceivedateAndExcludeState(planCache1.getGood().getId(),"未确认",shortageList.get(i).getDate());
                if(!planCacheList1.isEmpty()){
                    int receiveTotalCount=planCacheList1.stream().collect(Collectors.summingInt(PlanCache::getCount));
                    //更新接下来几天的结存
                    //如果当天到货，且当天的结存小于物料拉动库存，那么就说明时紧急拉动，紧急拉动更新时是从到货当天开始。非紧急是从到货第二天更新库存
                    if(shortageList.get(i).getStock()<planCache1.getGood().getTriggerstock()){
                        for(int k=i;k<shortageList.size();k++){
                            shortageList.get(k).setStock(shortageList.get(k).getStock()+receiveTotalCount);
                        }
                    }else {
                        //非紧急拉动，从第二天开始更新结存
                        if(i!=(shortageList.size()-1)){
                            for(int k=i+1;k<shortageList.size();k++){
                                shortageList.get(k).setStock(shortageList.get(k).getStock()+receiveTotalCount);
                            }
                        }
                    }
                }
            }
            //开始正常计算缺件计划
            //获取当前循环物料的配送周期天数
            int transitDay=new BigDecimal(planCache1.getGood().getSupplier().getTransitday()).intValue();
            if(planCache1.getGood().getSupplier().getTransitday().contains(".")){
                //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
                transitDay++;
            }
            //第1：先判断是否有第一个正常缺件计划。第一个正常缺件计划标准：以上传的第二天为发货日期，往后推运输周期天数得到到货日期，看到货日期后一天的结存是否小于等于物料拉动库存
            //自定义：该物料第一个正常计划的应该到货日期在缺件集合中的下标.默认为运输周期天数+1
            int index=transitDay+1;
            //如果第一个正常计划的到货日期第二天的下标不超过缺件集合最后一个记录的下标，那么就可以生成正常计划
            if((transitDay+2)<=(shortageList.size()-1)){
                //自定义：前一个正常缺件计划到货后结存.由于前面没有正常缺件计划，所以就默认为第一个正常缺件计划到货当天的上传的缺件报表记录的结存，以此来判断第一个正常缺件计划是否应该生成
                int planStock=shortageList.get(transitDay+1).getStock();
                //自定义：前一次正常缺件计划的到货数量,以计划的最大取货数量为准。由于前面没有，所以默认为0
                int planCount=0;
                //从第一个正常缺件计划到货日期后一天开始循环，生成正常缺件计划
                for(int i=(transitDay+2);i<shortageList.size();i++){
                    //当前循环的收货后结存=前一个计划到货数量-当前循环的需求数量
                    int currentStock=planStock-shortageList.get(i).getNeedcount();
                    if(currentStock<=planCache1.getGood().getTriggerstock()){
                        //生成正常缺件计划
                        //最小取货数量。到货日期后一天数的补足拉动库存数+扣除到货日期后一天剩余的运输周期天数的缺件报表记录的需求数
                        int minCount=0;
                        for(int g=0;g<transitDay;g++){
                            Shortage shortage=shortageList.get(i+g);
                            if(shortage!=null){
                                minCount+=shortage.getNeedcount();
                            }
                        }
                        //修正，箱数向上取整
                        if(minCount%planCache1.getGood().getOneboxcount()!=0){
                            minCount=(minCount/planCache1.getGood().getOneboxcount()+1)*planCache1.getGood().getOneboxcount();
                        }
                        //最大取货数量
                        int maxCount=0;
                        //获取前一天的到货后结存
                        int yestodayPlanStock=planStock;
                        if(yestodayPlanStock<0){
                            yestodayPlanStock=0;
                        }
                        if((minCount+yestodayPlanStock+shortageList.get(i-1).getNeedcount())<planCache1.getGood().getMaxstock()){
                            maxCount=planCache1.getGood().getMaxstock()-shortageList.get(i-1).getNeedcount()-yestodayPlanStock;
                            //箱数向下取整
                            if(maxCount%planCache1.getGood().getOneboxcount()!=0){
                                maxCount=(maxCount/planCache1.getGood().getOneboxcount()-1)*planCache1.getGood().getOneboxcount();
                            }
                        }else {
                            maxCount=minCount;
                        }
                        //更新前一次计划到货数量
                        planCount=maxCount;
                        //更新前一次正常缺件计划的到货后结存。
                        planStock=currentStock+maxCount;
                        //如果有在途计划的发货日期和到货日期与当前计划一样，就要合并
                        PlanCache planCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(planCache1.getGood().getId(),shortageList.get(i-1-transitDay).getDate(),shortageList.get(i-1).getDate());
                        if(planCache==null){
                            //新增计划
                            PlanCache newPlanCache=new PlanCache();
                            newPlanCache.setGood(planCache1.getGood());
                            newPlanCache.setCount(maxCount);
                            newPlanCache.setMaxcount(maxCount);
                            newPlanCache.setMincount(minCount);
                            newPlanCache.setSurecount(0);
                            newPlanCache.setTakecount(0);
                            newPlanCache.setReceivecount(0);
                            //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                            newPlanCache.setBoxcount(maxCount/planCache1.getGood().getOneboxcount());
                            //发货日期:从当前日期回推运输周期+1天
                            newPlanCache.setDate(shortageList.get(i-1-transitDay).getDate());
                            //到货日期为当前的前一天
                            newPlanCache.setReceivedate(shortageList.get(i-1).getDate());
                            newPlanCache.setState("未确认");
                            newPlanCache.setType("系统");
                            newPlanCache.setUrgent("否");
                            newPlanCache.setRemarks("");
                            newPlanCache.setCreatetime(now);
                            planCacheList.add(newPlanCache);
                        }else {
                            //合并计划:1.取货数量+当前计划的最小数量
                            planCache.setCount(planCache.getCount()+minCount);
                            planCacheMapper.updateByPrimaryKeySelective(planCache);
                        }
                    }
                }
            }else {
                //无需生成第一个正常缺件计划，那么说明第一个正常缺件集合到货日期大于了缺件报表中最大日期
                if(index>(shortageList.size()-1)){
                    index=shortageList.size()-1;
                }
            }
            //循环今天到第一个正常计划的到货日期下标，看是否有紧急计划。由于前面已经把在途计划的到货数量更新到缺件报表中了，
            //所有此处直接重新每天的结存，看是否小于物料拉动库存，有就要生成一条紧急拉动计划把今天到第一个正常计划到货日期之间所有的
            //缺口补上
            for(int i=0;i<=index;i++){
                //如果当天结存<=物料拉动库存，就生成计划
                if(shortageList.get(i).getStock()<=planCache1.getGood().getTriggerstock()){
                    System.out.println("更新后结存："+shortageList.get(i).getStock()+"小于了拉动库存"+planCache1.getGood().getTriggerstock());
                    //生成紧急计划
                    //发货日期
                    String sendDate="";
                    //到货日期
                    String receiveDate="";
                    //找到集合中结存最小的记录,结存升序排序
                    List<Shortage> sortList=shortageList.stream().sorted(Comparator.comparing(Shortage::getStock)).collect(Collectors.toList());
                    //最小取货数量=拉动库存-最小结存记录的结存
                    int minCount=shortageList.get(i).getGood().getTriggerstock()-sortList.get(0).getStock();
                    //最小取货数量，箱数向上取整。如果是没有零头箱，那么总箱数之上再加上一箱。
                    if(minCount%shortageList.get(i).getGood().getOneboxcount()!=0){
                        minCount=(minCount/shortageList.get(i).getGood().getOneboxcount()+1)*shortageList.get(i).getGood().getOneboxcount();
                    }else {
                        minCount=minCount+shortageList.get(i).getGood().getOneboxcount();
                    }
                    //推算到货日期就为当前循环的前一天
                    if((i-1)<=0){
                        //到货日期为上传当天或当天之前，那么紧急计划，发货日期和到货日期都为上传当天
                        sendDate=today;
                        receiveDate=today;
                    }else {
                        //到货日期在集合中，那么获取到货日期
                        receiveDate=shortageList.get(i-1).getDate();
                        //回推发货日期
                        if((i-1-transitDay)<=0){
                            sendDate=today;
                        }else {
                            sendDate=shortageList.get(i-1-transitDay).getDate();
                        }
                    }
                    //看生成的紧急计划是否有在途计划于其发货日期、到货日期都一样，如果有，就把紧急计划合并到在途之中。并把在途计划改为紧急
                    PlanCache planCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(shortageList.get(i).getGood().getId(),sendDate,receiveDate);
                    if(planCache!=null){
                        //如果有紧急计划，那么就不添加当前紧急计划
                    }else {
                        //没有发货日期和到货日期都一样的在途记录，那么就添加本次生成的紧急缺件计划
                        PlanCache newPlanCache=new PlanCache();
                        newPlanCache.setGood(shortageList.get(i).getGood());
                        newPlanCache.setCount(minCount);
                        //紧急拉动计划的最大取货数量=最小取货数量。因为这个计划是补第一个正常计划到货期间不足的数量，不能送太多
                        newPlanCache.setMaxcount(minCount);
                        newPlanCache.setMincount(minCount);
                        newPlanCache.setSurecount(0);
                        newPlanCache.setTakecount(0);
                        newPlanCache.setReceivecount(0);
                        //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                        newPlanCache.setBoxcount(minCount/shortageList.get(i).getGood().getOneboxcount());
                        newPlanCache.setDate(sendDate);
                        newPlanCache.setReceivedate(receiveDate);
                        newPlanCache.setState("未确认");
                        newPlanCache.setType("系统");
                        newPlanCache.setUrgent("是");
                        newPlanCache.setRemarks(today+"新增"+minCount+"的紧急需求，请及时配送");
                        newPlanCache.setCreatetime(now);
                        planCacheList.add(newPlanCache);
                    }
                    //产生了一条紧急拉动，那么就跳出循环
                    break;
                }
            }
            //3.添加取货计划
            PlanTake planTake=new PlanTake();
            planTake.setPlancacheid(planCache1.getId());
            planTake.setPlannumber(planNumber);
            planTake.setGood(planCache1.getGood());
            planTake.setCount((Integer)map.get("count"));
            planTake.setRealcount(0);
            planTake.setBoxcount((Integer)map.get("boxCount"));
            planTake.setLength(new BigDecimal((String)map.get("length")));
            planTake.setVolume(new BigDecimal((String)map.get("volume")));
            planTake.setWeight(new BigDecimal((String)map.get("weight")));
            planTake.setDate(date);
            planTake.setStartname(area.getAreaname());
            planTake.setStartnumber(area.getAreanumber());
            planTake.setEndname(endName);
            planTake.setEndnumber(endNumber);
            planTake.setRoutetype(routeType);
            planTake.setCartype(carType);
            planTake.setHighlength(highLength);
            planTake.setHighheight(highHeight);
            planTake.setLowlength(lowLength);
            planTake.setLowheight(lowHeight);
            planTake.setCarheight((Integer)map.get("carHeight"));
            planTake.setCarwidth(carWidth);
            planTake.setUsername(userName);
            planTake.setCreatetime(now);
            planTakeList.add(planTake);
        }
        if(!planTakeList.isEmpty()){
            planTakeMapper.insertBatch(planTakeList);
        }
        if(!planCacheList.isEmpty()){
            planCacheMapper.insertBatch(planCacheList);
        }*/
        return ResultUtil.success();
    }

    //---------------------------取货计划详情页面---------------------------
    /**
     * 条件分页查询，以计划编号分组统计
     * @param planNumber
     * @param supplierCode
     * @param routeId
     * @param date
     * @param userName
     * @param currentPage
     * @return
     */
    @Override
    public Result findLimitByCondition(String planNumber, String supplierCode,String supplierName, int routeId, String date,String startName,String endName, String userName,int currentPage) {
        //校验参数
        if(!planNumber.matches("^[0-9A-Z]{0,30}$")){
            return ResultUtil.error(1,"计划编号只能是1-30位的数字、大小写字母");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!startName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,20}$")){
            return ResultUtil.error(1,"出发地名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!endName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,20}$")){
            return ResultUtil.error(1,"目的地名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!userName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,10}$")){
            return ResultUtil.error(1,"创建人只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<PlanTake> list=planTakeMapper.selectLimitByCondition(planNumber,supplierCode ,supplierName,routeId,date,startName,endName,userName,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=planTakeMapper.selectCountByCondition(planNumber,supplierCode,supplierName ,routeId,date,startName,endName,userName);
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
     * 根据计划编号查询详情
     * @param planNumber
     * @return
     */
    @Override
    public Result findDetailByPlannumber(String planNumber) {
        List<PlanTake> planTakeList=planTakeMapper.selectByPlannumber(planNumber);
        if(planTakeList.isEmpty()){
            return ResultUtil.error(1,"该计划不存在");
        }
        return ResultUtil.success(planTakeList);
    }

    /**
     * 根据取货计划id删除，修改对应的缺件计划的计划数量
     * @param id
     * @return
     */
    @Override
    public Result planTakeDelete(int id) {
        PlanTake planTake=planTakeMapper.selectByPrimaryKey(id);
        if(planTake==null){
            return ResultUtil.error(1,"取货计划不存在");
        }
        //获取并修改缺件计划的计划数量
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(planTake.getPlancacheid());
        if(planCache!=null){
            if((planCache.getSurecount()-planTake.getCount())==0){
                planCache.setState("未确认");
            }
            planCache.setSurecount(planCache.getSurecount()-planTake.getCount());
            planCacheMapper.updateByPrimaryKeySelective(planCache);
        }
        //删除取货计划记录
        planTakeMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 下载
     * @param planNumber
     * @param supplierCode
     * @param supplierName
     * @param routeId
     * @param date
     * @param startName
     * @param endName
     * @param userName
     * @return
     */
    @Override
    public void download(String planNumber, String supplierCode, String supplierName, int routeId, String date, String startName, String endName, String userName, HttpServletResponse response) {
        //获取结果
        List<PlanTake> list=planTakeMapper.selectByCondition(planNumber,supplierCode,supplierName,routeId,date,startName,endName,userName);
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"物料信息表");
        //创建标题行,标题行为第一行，第一行下标为0
        Row titleRow = sheet.createRow(0);
        //给标题行的单元格设置样式和字体
        CellStyle titleCellStyle = wb.createCellStyle();//单元格样式
        titleCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//水平居中
        titleCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//垂直居中
        Font font = wb.createFont();//字体
        font.setFontName("宋体");//设置为宋体
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);//加粗
        //把字体样式绑定到标题单元格样式中
        titleCellStyle.setFont(font);
        //给标题行创建列，第一列下标为0.给列添加内容和格式
        Cell goodCodeCell = titleRow.createCell(0);
        goodCodeCell.setCellValue("计划编号");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("物料编号");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("物料名称");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("供应商编号");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("供应商名称");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("取货数量");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(6);
        batchCell.setCellValue("箱数");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(7);
        typeCell.setCellValue("长度(m)");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(8);
        nameCell.setCellValue("体积(m³)");
        nameCell.setCellStyle(titleCellStyle);
        Cell timeCell=titleRow.createCell(9);
        timeCell.setCellValue("重量(t)");
        timeCell.setCellStyle(titleCellStyle);
        Cell time2Cell=titleRow.createCell(10);
        time2Cell.setCellValue("取货日期");
        time2Cell.setCellStyle(titleCellStyle);
        Cell time3Cell=titleRow.createCell(11);
        time3Cell.setCellValue("出发地名称");
        time3Cell.setCellStyle(titleCellStyle);
        Cell cell12=titleRow.createCell(12);
        cell12.setCellValue("出发地编号");
        cell12.setCellStyle(titleCellStyle);
        Cell cell13=titleRow.createCell(13);
        cell13.setCellValue("目的地名称");
        cell13.setCellStyle(titleCellStyle);
        Cell cell14=titleRow.createCell(14);
        cell14.setCellValue("目的地编号");
        cell14.setCellStyle(titleCellStyle);
        Cell cell15=titleRow.createCell(15);
        cell15.setCellValue("线路类型");
        cell15.setCellStyle(titleCellStyle);
        Cell cell16=titleRow.createCell(16);
        cell16.setCellValue("车型");
        cell16.setCellStyle(titleCellStyle);
        Cell cell17=titleRow.createCell(17);
        cell17.setCellValue("高板长mm");
        cell17.setCellStyle(titleCellStyle);
        Cell cell18=titleRow.createCell(18);
        cell18.setCellValue("高板高mm");
        cell18.setCellStyle(titleCellStyle);
        Cell cell19=titleRow.createCell(19);
        cell19.setCellValue("低板长mm");
        cell19.setCellStyle(titleCellStyle);
        Cell cell20=titleRow.createCell(20);
        cell20.setCellValue("低板高mm");
        cell20.setCellStyle(titleCellStyle);
        Cell cell21=titleRow.createCell(21);
        cell21.setCellValue("车宽mm");
        cell21.setCellStyle(titleCellStyle);
        Cell cell22=titleRow.createCell(22);
        cell22.setCellValue("计算车高mm");
        cell22.setCellStyle(titleCellStyle);
        Cell cell23=titleRow.createCell(23);
        cell23.setCellValue("创建人");
        cell23.setCellStyle(titleCellStyle);
        Cell cell24=titleRow.createCell(24);
        cell24.setCellValue("创建时间");
        cell24.setCellStyle(titleCellStyle);
        //创建主体内容的单元格样式和字体
        CellStyle bodyCellStyle = wb.createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//左右居中
        bodyCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//上下居中
        Font bodyFont = wb.createFont();//创建字体
        bodyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);//不加粗
        bodyFont.setFontName("宋体");//设置字体
        bodyCellStyle.setFont(bodyFont);//把字体放入单元格样式
        //循环添加内容
        if (!list.isEmpty()) {
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < list.size(); i++) {
                //每循环一次，创建一行。由于第一行是标题行，所以行下标从1开始
                Row bodyRow = sheet.createRow(i + 1);
                //给每行创建列，第一列下标为0,并给每列添加内容
                //计划编号
                Cell cell0 = bodyRow.createCell(0);
                cell0.setCellValue(list.get(i).getPlannumber());
                cell0.setCellStyle(bodyCellStyle);
                cell0.setCellType(Cell.CELL_TYPE_STRING);
                //物料编号
                Cell cell1 = bodyRow.createCell(1);
                cell1.setCellValue(list.get(i).getGood().getGoodcode());
                cell1.setCellStyle(bodyCellStyle);
                cell1.setCellType(Cell.CELL_TYPE_STRING);
                //物料名称
                Cell cell2 = bodyRow.createCell(2);
                cell2.setCellValue(list.get(i).getGood().getGoodname());
                cell2.setCellStyle(bodyCellStyle);
                cell2.setCellType(Cell.CELL_TYPE_STRING);
                //供应商编号
                Cell cell3 = bodyRow.createCell(3);
                cell3.setCellValue(list.get(i).getGood().getSupplier().getSuppliercode());
                cell3.setCellStyle(bodyCellStyle);
                cell3.setCellType(Cell.CELL_TYPE_STRING);
                //供应商名称
                Cell cell4 = bodyRow.createCell(4);
                cell4.setCellValue(list.get(i).getGood().getSupplier().getSuppliername());
                cell4.setCellStyle(bodyCellStyle);
                cell4.setCellType(Cell.CELL_TYPE_STRING);
                //数量
                Cell cell5 = bodyRow.createCell(5);
                cell5.setCellValue(String.valueOf(list.get(i).getCount()));
                cell5.setCellStyle(bodyCellStyle);
                cell5.setCellType(Cell.CELL_TYPE_STRING);
                //箱数
                Cell cell6 = bodyRow.createCell(6);
                cell6.setCellValue(String.valueOf(list.get(i).getBoxcount()));
                cell6.setCellStyle(bodyCellStyle);
                cell6.setCellType(Cell.CELL_TYPE_STRING);
                //长
                Cell cell8 = bodyRow.createCell(7);
                cell8.setCellValue(String.valueOf(list.get(i).getLength()));
                cell8.setCellStyle(bodyCellStyle);
                cell8.setCellType(Cell.CELL_TYPE_STRING);
                //体积
                Cell cell7 = bodyRow.createCell(8);
                cell7.setCellValue(String.valueOf(list.get(i).getVolume()));
                cell7.setCellStyle(bodyCellStyle);
                cell7.setCellType(Cell.CELL_TYPE_STRING);
                //重量
                Cell cell9 = bodyRow.createCell(9);
                cell9.setCellValue(String.valueOf(list.get(i).getWeight()));
                cell9.setCellStyle(bodyCellStyle);
                cell9.setCellType(Cell.CELL_TYPE_STRING);
                //日期
                Cell cell10 = bodyRow.createCell(10);
                cell10.setCellValue(list.get(i).getDate());
                cell10.setCellStyle(bodyCellStyle);
                cell10.setCellType(Cell.CELL_TYPE_STRING);
                //出发地名称
                Cell cell11 = bodyRow.createCell(11);
                cell11.setCellValue(list.get(i).getStartname());
                cell11.setCellStyle(bodyCellStyle);
                cell11.setCellType(Cell.CELL_TYPE_STRING);
                //出发地编号
                Cell cell121 = bodyRow.createCell(12);
                cell121.setCellValue(list.get(i).getStartnumber());
                cell121.setCellStyle(bodyCellStyle);
                cell121.setCellType(Cell.CELL_TYPE_STRING);
                //目的地名称
                Cell cell131 = bodyRow.createCell(13);
                cell131.setCellValue(list.get(i).getEndname());
                cell131.setCellStyle(bodyCellStyle);
                cell131.setCellType(Cell.CELL_TYPE_STRING);
                //目的地编号
                Cell cell141 = bodyRow.createCell(14);
                cell141.setCellValue(list.get(i).getEndnumber());
                cell141.setCellStyle(bodyCellStyle);
                cell141.setCellType(Cell.CELL_TYPE_STRING);
                //线路类型
                Cell cell151 = bodyRow.createCell(15);
                cell151.setCellValue(list.get(i).getRoutetype());
                cell151.setCellStyle(bodyCellStyle);
                cell151.setCellType(Cell.CELL_TYPE_STRING);
                //车型
                Cell cell161 = bodyRow.createCell(16);
                cell161.setCellValue(list.get(i).getCartype());
                cell161.setCellStyle(bodyCellStyle);
                cell161.setCellType(Cell.CELL_TYPE_STRING);
                //高板长
                Cell cell171 = bodyRow.createCell(17);
                cell171.setCellValue(String.valueOf(list.get(i).getHighlength()));
                cell171.setCellStyle(bodyCellStyle);
                cell171.setCellType(Cell.CELL_TYPE_STRING);
                //高板高
                Cell cell181 = bodyRow.createCell(18);
                cell181.setCellValue(String.valueOf(list.get(i).getHighheight()));
                cell181.setCellStyle(bodyCellStyle);
                cell181.setCellType(Cell.CELL_TYPE_STRING);
                //低板长
                Cell cell191 = bodyRow.createCell(19);
                cell191.setCellValue(String.valueOf(list.get(i).getLowlength()));
                cell191.setCellStyle(bodyCellStyle);
                cell191.setCellType(Cell.CELL_TYPE_STRING);
                //低板高
                Cell cell201 = bodyRow.createCell(20);
                cell201.setCellValue(String.valueOf(list.get(i).getLowheight()));
                cell201.setCellStyle(bodyCellStyle);
                cell201.setCellType(Cell.CELL_TYPE_STRING);
                //车宽
                Cell cell211 = bodyRow.createCell(21);
                cell211.setCellValue(String.valueOf(list.get(i).getCarwidth()));
                cell211.setCellStyle(bodyCellStyle);
                cell211.setCellType(Cell.CELL_TYPE_STRING);
                //计算车高
                Cell cell221 = bodyRow.createCell(22);
                cell221.setCellValue(String.valueOf(list.get(i).getCarheight()));
                cell221.setCellStyle(bodyCellStyle);
                cell221.setCellType(Cell.CELL_TYPE_STRING);
                //创建人
                Cell cell231 = bodyRow.createCell(23);
                cell231.setCellValue(list.get(i).getUsername());
                cell231.setCellStyle(bodyCellStyle);
                cell231.setCellType(Cell.CELL_TYPE_STRING);
                //创建时间
                Cell cell241 = bodyRow.createCell(24);
                cell241.setCellValue(simpleDateFormat.format(list.get(i).getCreatetime()));
                cell241.setCellStyle(bodyCellStyle);
                cell241.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = startName+"-"+endName+"的取货计划.xlsx";
        //对文件名进行编码格式设置，防止中文乱码
        try {
            fileName = URLEncoder.encode(fileName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        //设置响应头
        response.addHeader("Content-Disposition", "attachment;filename=" + fileName);
        try {
            //得到输出流
            OutputStream os = response.getOutputStream();
            //把Excel通过输出流写出
            wb.write(os);
            //关闭流
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //---------------------------取货计划拆拼页面---------------------------
    /**
     * 根据计划编号集合查询详情
     * @param planNumbers 计划编号集合，格式：编号,编号,...
     * @return
     */
    @Override
    public Result planTakeByNumbers(String planNumbers) {
        String[] list=planNumbers.split(",");
        List<PlanTake> resultLit=new ArrayList<>();
        for(String planNumber:list){
            if(planNumber!=null){
                List<PlanTake> planTakeList=planTakeMapper.selectByPlannumber(planNumber);
                if(!planTakeList.isEmpty()){
                    resultLit.addAll(planTakeList);
                }
            }
        }
        if(resultLit.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(resultLit);
    }

    /**
     * 传入取货计划id、取货数量、箱数、车宽、车高来计算长度、体积、重量
     * @param id
     * @param count
     * @param boxCount
     * @param lowHeight
     * @param carWidth
     * @return
     */
    @Override
    public Result planTakeCalculate(int id, int count, int boxCount, int lowHeight, int carWidth) {
        //验证1：是否存在
        PlanTake planTake=planTakeMapper.selectByPrimaryKey(id);
        if(planTake==null){
            return ResultUtil.error(1,"取货计划不存在，刷新页面后重试");
        }
        //验证2：传入数量不可大于已有数量
        if(count>planTake.getCount()){
            return ResultUtil.error(1,"取货数量不可大于"+planTake.getCount());
        }
        //验证3：传入箱数不可大于已有箱数
        if(boxCount>planTake.getBoxcount()){
            return ResultUtil.error(1,"箱数不可大于"+planTake.getBoxcount());
        }
        //验证4：传入车高不可低于物料一托的高度或者是一箱的高度,不可选择低于单箱宽度或着单托宽度的车型
        //单托或单箱高度mm
        int oneHeight=0;
        //单托或单箱宽度mm
        int oneWidth=0;
        if(planTake.getGood().getOnetrayboxcount()==0){
            //非托盘件
            oneHeight=planTake.getGood().getBoxheight();
            oneWidth=planTake.getGood().getBoxwidth();
        }else {
            //托盘件单托高度=托盘高度+箱高*单托层数
            oneHeight=planTake.getGood().getTrayheight()+planTake.getGood().getBoxheight()*planTake.getGood().getOnetraylayerscount();
            //单托宽度=托盘宽度
            oneWidth=planTake.getGood().getTraywidth();
        }
        if(lowHeight<oneHeight){
            return ResultUtil.error(1,"不可选择高度低于"+oneHeight+"(mm)的车型");
        }
        if(carWidth<oneWidth){
            return ResultUtil.error(1,"不可选择宽度低于"+oneWidth+"(mm)的车型");
        }
        //计算层数、位置数、排数、长度(m)
        Map<String,Object> map=new HashMap<>();
        if(planTake.getGood().getOnetrayboxcount()==0){
            //非托盘件
            //堆叠层数=车厢高度（mm）/单箱高度（mm）
            int layers=lowHeight/planTake.getGood().getBoxheight();
            //层数
            map.put("layers",layers);
            //所占位置数量，即一个箱子占的一个格子。位置数量=箱数/堆叠层数
            BigDecimal location=new BigDecimal(boxCount).divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            map.put("location",location);
            //此处分为4种情况。：1.如果车身宽度除箱子长度能够除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //2.如果车身宽度除箱子长度不能够除尽，但是除箱子宽度能除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //3.如果车身宽度除箱子长度和宽度都除不尽，看是否能够用车身宽度除以(箱子长+箱子宽)能不能除尽，如果可以那么就用箱子长+宽之和作为排序宽度，进行摆放箱子
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%planTake.getGood().getBoxlength()==0){
                //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*箱子宽度
                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","箱长"+planTake.getGood().getBoxlength());
            }else if(carWidth%planTake.getGood().getBoxwidth()==0){
                //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*箱子长度
                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","箱宽"+planTake.getGood().getBoxwidth());
            }else if(carWidth%(planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth())==0){
                //就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                map.put("side","箱长+宽("+planTake.getGood().getBoxlength()+"+"+planTake.getGood().getBoxwidth()+")");
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是箱子的长
                    map.put("length",new BigDecimal(planTake.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                        int cha=i*planTake.getGood().getBoxlength()-(location.intValue()+1-i)*planTake.getGood().getBoxwidth();
                        if(cha>-planTake.getGood().getBoxlength()||cha<planTake.getGood().getBoxwidth()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                map.put("length",new BigDecimal(i*planTake.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                map.put("length",new BigDecimal((location.intValue()+1-i)*planTake.getGood().getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //箱子长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/planTake.getGood().getBoxlength())*planTake.getGood().getBoxlength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/planTake.getGood().getBoxwidth())*planTake.getGood().getBoxwidth();
                if((planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth())>carWidth){
                    //如果箱长+宽>车宽，直接放弃方案3，只需要比较方案1、2哪个剩余宽度小就用哪个方案。剩余宽度一样，箱长度优先
                    if(lengthLast<=widthLast){
                        //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*箱子宽度
                        BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","箱长"+planTake.getGood().getBoxlength());
                    }else {
                        //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*箱子长度/1000
                        BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","箱宽"+planTake.getGood().getBoxwidth());
                    }
                }else {
                    //托盘长+宽作为计算宽度时，剩余的车身空闲宽度
                    int sumLast=carWidth-(carWidth/(planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth()))*(planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth());
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/箱子长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            //米数=排数*箱子宽度
                            BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            map.put("length",length);
                            map.put("side","箱长"+planTake.getGood().getBoxlength());
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/箱子长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子宽度
                                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱长"+planTake.getGood().getBoxlength());
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                map.put("side","箱长+宽("+planTake.getGood().getBoxlength()+"+"+planTake.getGood().getBoxwidth()+")");
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是箱子的长
                                    map.put("length",new BigDecimal(planTake.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                        int cha=i*planTake.getGood().getBoxlength()-(location.intValue()+1-i)*planTake.getGood().getBoxwidth();
                                        if(cha>-planTake.getGood().getBoxlength()||cha<planTake.getGood().getBoxwidth()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                map.put("length",new BigDecimal(i*planTake.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                map.put("length",new BigDecimal((location.intValue()+1-i)*planTake.getGood().getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(planTake.getGood().getBoxlength()+planTake.getGood().getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            map.put("side","箱长+宽("+planTake.getGood().getBoxlength()+"+"+planTake.getGood().getBoxwidth()+")");
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是箱子的长
                                map.put("length",new BigDecimal(planTake.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                    int cha=i*planTake.getGood().getBoxlength()-(location.intValue()+1-i)*planTake.getGood().getBoxwidth();
                                    if(cha>-planTake.getGood().getBoxlength()||cha<planTake.getGood().getBoxwidth()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            map.put("length",new BigDecimal(i*planTake.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            map.put("length",new BigDecimal((location.intValue()+1-i)*planTake.getGood().getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱宽"+planTake.getGood().getBoxwidth());
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱宽"+planTake.getGood().getBoxwidth());
                            }
                        }
                    }
                }
            }
        }else {
            //托盘件
            //托盘数量=箱数/单托箱数
            BigDecimal trayCount=new BigDecimal(boxCount).divide(new BigDecimal(planTake.getGood().getOnetrayboxcount()),2,BigDecimal.ROUND_HALF_UP);
            //单托高度（mm）=物料箱高度（mm）*单托层数+托盘高度（mm）
            int trayHeight=planTake.getGood().getTrayheight()+planTake.getGood().getBoxheight()*planTake.getGood().getOnetraylayerscount();
            //堆叠层数=车厢高度（mm）/托盘高度（mm）
            int layers=lowHeight/trayHeight;
            if(layers==0){
                return ResultUtil.error(1,"车高不可低于"+trayHeight+"，否则该物料无法装车");
            }
            map.put("layers",layers);
            //所占位置数量，即一个托盘占的一个格子。位置数量=托盘数量/堆叠层数
            BigDecimal location=trayCount.divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            map.put("location",location);
            //此处分为4种情况。：1.如果车身宽度除托盘长度能够除尽，那么就用托盘长度作为排序宽度，进行摆放托盘
            //2.如果车身宽度除托盘长度不能够除尽，但是除托盘宽度能除尽，那么就用托盘长度作为排序宽度，进行摆放托盘
            //3.如果车身宽度除托盘长度和宽度都除不尽，看是否能够用车身宽度除以(托盘长+托盘宽)能不能除尽，如果可以那么就用托盘长+宽之和作为排序宽度，进行摆放托盘
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%planTake.getGood().getTraylength()==0){
                //就用托盘长来做标准摆放。那么排数=位置数量/(车身宽度/托盘长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*托盘宽度
                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","托盘长"+planTake.getGood().getTraylength());
            }else if(carWidth%planTake.getGood().getTraywidth()==0){
                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*托盘长度
                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","托盘宽"+planTake.getGood().getTraywidth());
            }else if(carWidth%(planTake.getGood().getTraywidth()+planTake.getGood().getTraylength())==0){
                //就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(planTake.getGood().getTraywidth()+planTake.getGood().getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                map.put("side","托盘长+宽("+planTake.getGood().getTraylength()+"+"+planTake.getGood().getTraywidth()+")");
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是托盘的长
                    map.put("length",new BigDecimal(planTake.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                        int cha=i*planTake.getGood().getTraylength()-(location.intValue()+1-i)*planTake.getGood().getTraywidth();
                        if(cha>-planTake.getGood().getTraylength()||cha<planTake.getGood().getTraylength()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                map.put("length",new BigDecimal(i*planTake.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                map.put("length",new BigDecimal((location.intValue()+1-i)*planTake.getGood().getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //托盘长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/planTake.getGood().getTraylength())*planTake.getGood().getTraylength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/planTake.getGood().getTraywidth())*planTake.getGood().getTraywidth();
                //托盘长+宽作为计算宽度时，剩余的车身空闲宽度。如果长+宽超过了车宽则直接放弃
                if((planTake.getGood().getTraywidth()+planTake.getGood().getTraylength())>carWidth){
                    //只需要考虑长和宽2种情况，如果长和宽都满足，那么以长优先
                    if(lengthLast<=widthLast){
                        //长做摆放
                        BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*托盘宽度
                        BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","托盘长"+planTake.getGood().getTraylength());
                    }else {
                        //用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*托盘长度
                        BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","托盘宽"+planTake.getGood().getTraywidth());
                    }
                }else{
                    //如果长+宽<车宽，那么就用3种方式判断
                    int sumLast=carWidth-(carWidth/(planTake.getGood().getTraywidth()+planTake.getGood().getTraylength()))*(planTake.getGood().getTraywidth()+planTake.getGood().getTraylength());
                    System.out.println("托盘件:3种都不行，托盘长+宽摆放剩余宽度"+sumLast);
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/托盘长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            //米数=排数*托盘宽度
                            BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            map.put("length",length);
                            map.put("side","托盘长"+planTake.getGood().getTraylength());
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/托盘长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘宽度
                                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘长"+planTake.getGood().getTraylength());
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(planTake.getGood().getTraywidth()+planTake.getGood().getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                map.put("side","托盘长+宽("+planTake.getGood().getTraylength()+"+"+planTake.getGood().getTraywidth()+")");
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是托盘的长
                                    map.put("length",new BigDecimal(planTake.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                        int cha=i*planTake.getGood().getTraylength()-(location.intValue()+1-i)*planTake.getGood().getTraywidth();
                                        if(cha>-planTake.getGood().getTraylength()||cha<planTake.getGood().getTraylength()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                map.put("length",new BigDecimal(i*planTake.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                map.put("length",new BigDecimal((location.intValue()+1-i)*planTake.getGood().getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(planTake.getGood().getTraywidth()+planTake.getGood().getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            map.put("side","托盘长+宽("+planTake.getGood().getTraylength()+"+"+planTake.getGood().getTraywidth()+")");
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是托盘的长
                                map.put("length",new BigDecimal(planTake.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                    int cha=i*planTake.getGood().getTraylength()-(location.intValue()+1-i)*planTake.getGood().getTraywidth();
                                    if(cha>-planTake.getGood().getTraylength()||cha<planTake.getGood().getTraylength()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            map.put("length",new BigDecimal(i*planTake.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            map.put("length",new BigDecimal((location.intValue()+1-i)*planTake.getGood().getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘宽"+planTake.getGood().getTraywidth());
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/planTake.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(planTake.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘宽"+planTake.getGood().getTraywidth());
                            }
                        }
                    }
                }
            }
        }
        //计算物料的总体积=物料体积+托盘体积
        //托盘体积系数
        BigDecimal trayRatio=new BigDecimal((planTake.getGood().getTrayratio()+100)).divide(new BigDecimal("100"));
        //物料总体积=箱数*箱长*箱宽*箱高/1000000000*托盘体积系数
        BigDecimal volume=new BigDecimal(planTake.getGood().getBoxlength()).multiply(new BigDecimal(planTake.getGood().getBoxwidth())).multiply(new BigDecimal(planTake.getGood().getBoxheight())).multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000000000"),2,BigDecimal.ROUND_HALF_UP).multiply(trayRatio);
        map.put("volume",volume);
        //计算物料的总重量t=单箱重量kg*箱数/1000
        BigDecimal weight=planTake.getGood().getBoxweight().multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
        map.put("weight",weight);
        return ResultUtil.success(map);
    }

    /**
     * 传入信息生成新的取货计划
     * @param carType
     * @param highLength
     * @param highHeight
     * @param lowLength
     * @param lowHeight
     * @param carWidth
     * @param planTakeInfos 取货计划集合，格式：格式：计划id,车高mm,数量,箱数,长度,体积,重量;计划id,车高mm,数量,箱数,长度,体积,重量;...
     * @return
     */
    @Override
    @Transactional
    public synchronized Result planTakeAddRepeat(String carType, int highLength, int highHeight, int lowLength, int lowHeight, int carWidth, String planTakeInfos) {
        //获取方案内容,并验证每个取货计划是否都存在
        String[] infos=planTakeInfos.split(";");
        List<Map<String,Object>> infoList=new ArrayList<>();
        String startNumber="";
        String endNumber="";
        //自定义物料id+计算车高不重复的集合，以便把不同取货计划但是物料一样的且计算车高一样记录进行合并计算
        List<String> goodList=new ArrayList<>();
        for(String info:infos){
            Map<String,Object> map=new HashMap<>();
            //获取取货计划
            PlanTake planTake=planTakeMapper.selectByPrimaryKey(Integer.parseInt(info.split(",")[0]));
            if(planTake==null){
                return ResultUtil.error(1,"有选中的取货计划不存在，刷新页面后再试");
            }else {
                //获取物料+计算车高信息
                String info1=planTake.getGood().getId()+","+info.split(",")[1];
                if(!goodList.contains(info1)){
                    goodList.add(info1);
                }
                if(startNumber.equals("")){
                    startNumber=planTake.getStartnumber();
                    endNumber=planTake.getEndnumber();
                }
                map.put("planTake",planTake);
                //车高
                map.put("carHeight",Integer.parseInt(info.split(",")[1]));
                //数量
                map.put("count",Integer.parseInt(info.split(",")[2]));
                //箱数
                map.put("boxCount",Integer.parseInt(info.split(",")[3]));
                //长度m
                map.put("length",info.split(",")[4]);
                //体积立方米
                map.put("volume",info.split(",")[5]);
                //重量t
                map.put("weight",info.split(",")[6]);
                infoList.add(map);
            }
        }
        //生成计划编号：出发地编号+目的地编号+"-"+年4位+2位月+2位日+时分秒6位+3位毫秒
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMddHHmmssSSS");
        Date now=new Date();
        String planNumber=startNumber+endNumber+"-"+simpleDateFormat.format(now);
        //获取当前账号
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        //按照物料id+计算车高进行：1.添加取货计划、2.修改原取货计划的数量
        for(String goodInfo:goodList){
            List<Map<String,Object>> list1=new ArrayList<>();
            //获取该物料id+车高对应的取货计划信息
            for(Map map:infoList){
                PlanTake planTake1=(PlanTake)map.get("planTake");
                int carh=(Integer)map.get("carHeight");
                if(String.valueOf(planTake1.getGood().getId()).equals(goodInfo.split(",")[0])&&Integer.parseInt(goodInfo.split(",")[1])==carh){
                    list1.add(map);
                }
            }
            //判断该物料是有几条取货计划
            if(list1.size()>1){
                //合并后的数量
                int totalCount=0;
                for(Map map:list1){
                    //原取货计划
                    PlanTake oldPlantake=(PlanTake) map.get("planTake");
                    //取货计划的新数量
                    int newCount=(Integer) map.get("count");
                    totalCount+=newCount;
                    //分条对原计划进行判断，如果新取货计划数量=原计划数量就删除原计划，否则就修改原计划信息
                    if(oldPlantake.getCount()==newCount){
                        planTakeMapper.deleteByPrimaryKey(oldPlantake.getId());
                    }else {
                        //对原计划的数量、箱数、长度、体积、重量修改
                        int oldNewCount=oldPlantake.getCount()-newCount;
                        oldPlantake.setCount(oldNewCount);
                        int oldNewBoxCount=0;
                        if(oldNewCount%oldPlantake.getGood().getOneboxcount()==0){
                            oldNewBoxCount=oldNewCount/oldPlantake.getGood().getOneboxcount();
                        }else {
                            oldNewBoxCount=oldNewCount/oldPlantake.getGood().getOneboxcount()+1;
                        }
                        oldPlantake.setBoxcount(oldNewBoxCount);
                        //修改原计划的长、体积、重量
                        planTakeMapper.updateByPrimaryKeySelective(calculateInfo(oldPlantake,oldPlantake.getCarheight(),oldPlantake.getCarwidth()));
                    }
                }
                //获取一条原计划
                PlanTake oldPlanTake=(PlanTake)list1.get(0).get("planTake");
                //多条计划。需要合并计算
                PlanTake planTake=new PlanTake();
                planTake.setPlannumber(planNumber);
                planTake.setGood(oldPlanTake.getGood());
                planTake.setCount(totalCount);
                int totalBoxCount=0;
                if(totalCount%oldPlanTake.getGood().getOneboxcount()==0){
                    totalBoxCount=totalCount/oldPlanTake.getGood().getOneboxcount();
                }else {
                    totalBoxCount=totalCount/oldPlanTake.getGood().getOneboxcount()+1;
                }
                planTake.setBoxcount(totalBoxCount);
                planTake.setDate(oldPlanTake.getDate());
                planTake.setStartname(oldPlanTake.getStartname());
                planTake.setStartnumber(startNumber);
                planTake.setEndname(oldPlanTake.getEndname());
                planTake.setEndnumber(endNumber);
                planTake.setRoutetype(oldPlanTake.getRoutetype());
                planTake.setCartype(carType);
                planTake.setHighlength(highLength);
                planTake.setHighheight(highHeight);
                planTake.setLowlength(lowLength);
                planTake.setLowheight(lowHeight);
                planTake.setCarheight((Integer)list1.get(0).get("carHeight"));
                planTake.setCarwidth(carWidth);
                planTake.setUsername(userName);
                planTake.setCreatetime(now);
                //计算合并后的计划的长、体积、重量
                planTakeMapper.insertSelective(calculateInfo(planTake,(Integer)list1.get(0).get("carHeight"),carWidth));
            }else if(list1.size()==1){
                //单条计划。无需计算，如果新取货数量=原取货数量直接修改，否则就要添加新取货计划并修改原计划
                PlanTake oldPlanTake=(PlanTake) list1.get(0).get("planTake");
                if((Integer)list1.get(0).get("count")==oldPlanTake.getCount()){
                    System.out.println("修改原几乎："+oldPlanTake.getId());
                    //如果新计划的取货数量=原计划的取货数量，那么修改原计划的：计划编号、车型、车型相关高长、计算车高、车宽、长度
                    oldPlanTake.setPlannumber(planNumber);
                    //车型
                    oldPlanTake.setCartype(carType);
                    oldPlanTake.setHighlength(highLength);
                    oldPlanTake.setHighheight(highHeight);
                    oldPlanTake.setLowlength(lowLength);
                    oldPlanTake.setLowheight(lowHeight);
                    //计算车高
                    oldPlanTake.setCarheight((Integer)list1.get(0).get("carHeight"));
                    //车宽
                    oldPlanTake.setCarwidth(carWidth);
                    //长度
                    oldPlanTake.setLength(new BigDecimal((String)list1.get(0).get("length")));
                    //创建人
                    oldPlanTake.setUsername(userName);
                    //创建时间
                    oldPlanTake.setCreatetime(now);
                    planTakeMapper.updateByPrimaryKeySelective(oldPlanTake);
                }else{
                    //添加新计划，然后修改原计划的数量、箱数、长、体积、重量
                    PlanTake planTake=new PlanTake();
                    planTake.setPlannumber(planNumber);
                    planTake.setGood(oldPlanTake.getGood());
                    planTake.setCount((Integer)list1.get(0).get("count"));
                    planTake.setBoxcount((Integer)list1.get(0).get("boxCount"));
                    planTake.setLength(new BigDecimal((String)list1.get(0).get("length")));
                    planTake.setVolume(new BigDecimal((String)list1.get(0).get("volume")));
                    planTake.setWeight(new BigDecimal((String)list1.get(0).get("weight")));
                    planTake.setDate(oldPlanTake.getDate());
                    planTake.setStartname(oldPlanTake.getStartname());
                    planTake.setStartnumber(startNumber);
                    planTake.setEndname(oldPlanTake.getEndname());
                    planTake.setEndnumber(endNumber);
                    planTake.setRoutetype(oldPlanTake.getRoutetype());
                    planTake.setCartype(carType);
                    planTake.setHighlength(highLength);
                    planTake.setHighheight(highHeight);
                    planTake.setLowlength(lowLength);
                    planTake.setLowheight(lowHeight);
                    planTake.setCarheight((Integer)list1.get(0).get("carHeight"));
                    planTake.setCarwidth(carWidth);
                    planTake.setUsername(userName);
                    planTake.setCreatetime(now);
                    planTakeMapper.insertSelective(planTake);
                    //修改原计划的信息
                    int newCount=oldPlanTake.getCount()-(Integer)list1.get(0).get("count");
                    //数量
                    oldPlanTake.setCount(newCount);
                    int newBoxCount=0;
                    if(newCount%oldPlanTake.getGood().getOneboxcount()==0){
                        newBoxCount=newCount/oldPlanTake.getGood().getOneboxcount();
                    }else {
                        newBoxCount=newCount/oldPlanTake.getGood().getOneboxcount()+1;
                    }
                    //箱数
                    oldPlanTake.setBoxcount(newBoxCount);
                    //修改长、体积、重量
                    planTakeMapper.updateByPrimaryKeySelective(calculateInfo(oldPlanTake,lowHeight,carWidth));
                }
            }
        }
        return ResultUtil.success();
    }

    //---------------------------取货计划详绑定吉利单据页面---------------------------
    /**
     * 根据计划编号查询内容，同物料id合并，并根据供应商编号排序再根据物料编号排序
     * @param planNumber
     * @return
     */
    @Override
    public Result planTakeByNumber(String planNumber) {
        //根据物料id合并，数量求和。根据供应商编号升序，再根据物料编号升序
        List<PlanTake> planTakeList=planTakeMapper.selectGroupByPlannumber(planNumber);
        if(planTakeList.isEmpty()){
            return ResultUtil.error(1,"取货计划不存在，重新选择取货计划绑定吉利单据");
        }
        return ResultUtil.success(planTakeList);
    }

    /**
     * 上传PD单绑定
     * @param files
     * @param planNumber
     * @return
     */
    @Override
    public Result planTakeUpload(MultipartFile[] files, String planNumber) {
        //先根据计划编号，以物料id分组得方式查询出来，把数量求和
        List<PlanTake> planTakeList=planTakeMapper.selectGroupByPlannumber(planNumber);
        if(planTakeList.isEmpty()){
            return ResultUtil.error(1,"无取货计划内容，无法绑定");
        }
        //修改总箱数
        for(PlanTake planTake:planTakeList){
            int boxCount=0;
            if(planTake.getCount()%planTake.getGood().getOneboxcount()==0){
                boxCount=planTake.getCount()/planTake.getGood().getOneboxcount();
            }else {
                boxCount=planTake.getCount()/planTake.getGood().getOneboxcount()+1;
            }
            planTake.setBoxcount(boxCount);
        }
        //获取工厂
        Factory factory=planTakeList.get(0).getGood().getSupplier().getRoute().getFactory();
        //获取所有得PDF文件中得物料信息
        List<GeelyBillCache> geelyBillCacheAll =new ArrayList<>();
        for(MultipartFile file:files){
            //获取单子中得工厂编号
            String supplierCode=PdfBoxUtilLinHai.getSupplierCode(file);
            if(supplierCode==null){
                return ResultUtil.error(1,file.getOriginalFilename()+"中获取不到供应商编号，无法读取内容");
            }
            Supplier supplier=supplierMapper.selectBySuppliercodeAndFactoryid(supplierCode,factory.getId());
            if(supplier==null){
                return ResultUtil.error(1,"在工厂("+factory.getFactoryname()+")中找不到文件("+file.getOriginalFilename()+")中的供应商("+supplierCode+")");
            }
            List<GeelyBillCache> geelyBillCacheList =new ArrayList<>();
            //用哪个工厂上传就使用那个工厂的上传读取模板类
            if(factory.getFactoryname().contains("临海")){
                geelyBillCacheList = PdfBoxUtilLinHai.getContext(file,supplier,factory);
            }else {
                return ResultUtil.error(1,"工厂:"+factory.getFactoryname()+"没有对应的文件读取模板");
            }
            //看是否能获取内容
            if(geelyBillCacheList ==null){
                return ResultUtil.error(1,"文件("+file.getOriginalFilename()+")的读取出错:"+PdfBoxUtilLinHai.getMessage());
            }
            String goodS="";
            //看上传的是否有物料不在本次取货计划中
            for(GeelyBillCache geelyBillCache:geelyBillCacheList){
                //上传的物料是否在本次计划中，默认未不在
                boolean isExit=false;
                for(PlanTake planTake:planTakeList){
                    if(planTake.getGood().getId()==geelyBillCache.getGood().getId()){
                        isExit=true;
                    }
                }
                if(isExit==false){
                    goodS+="、"+geelyBillCache.getGood().getGoodcode();
                }
            }
            if(goodS.equals("")){
                geelyBillCacheAll.addAll(geelyBillCacheList);
            }else {
                //返回提示
                return ResultUtil.error(1,"文件("+file.getOriginalFilename()+")中物料"+goodS.substring(1)+"不在当前取货计划中");
            }
        }
        Date now=new Date();
        //获取所有PD单内容后，看是否有重复上传的。有的话就不保存，没有就要保存到PD单在途表种geelybillCache
        for(GeelyBillCache geelyBillCache:geelyBillCacheAll){
            GeelyBillCache geelyBillCache1=geelyBillCacheMapper.selectByGoodidAndBillnumber(geelyBillCache.getGood().getId(),geelyBillCache.getBillnumber());
            if(geelyBillCache1==null){
                geelyBillCache.setUploadtime(now);
                //保存上传的数据
                geelyBillCacheMapper.insertSelective(geelyBillCache);
            }
        }
        //把上传的数据根据计划排序
        List<List<GeelyBillCache>> lastList=new ArrayList<>();
        for(PlanTake planTake:planTakeList){
            List<GeelyBillCache> geelyBillCaches=new ArrayList<>();
            for(GeelyBillCache geelyBillCache:geelyBillCacheAll){
                if(geelyBillCache.getGood().getId()==planTake.getGood().getId()){
                    geelyBillCaches.add(geelyBillCache);
                }
            }
            lastList.add(geelyBillCaches);
        }
        //返回以物料id分组后，把数量求和的取货计划
        return ResultUtil.success(lastList);
    }

    //根据传入的取货计划、车高、车宽计算出长、体积、重量并赋值给取货计划属性，返回取货对象
    public PlanTake calculateInfo(PlanTake planTake,int carHeight,int carWidth){
        Good good=planTake.getGood();
        int count=planTake.getCount();
        int boxCount=planTake.getBoxcount();
        //1.计算长度
        if(good.getOnetrayboxcount()==0){
            //非托盘件
            //堆叠层数=车厢高度（mm）/单箱高度（mm）
            int layers=carHeight/good.getBoxheight();
            //所占位置数量，即一个箱子占的一个格子。位置数量=箱数/堆叠层数
            BigDecimal location=new BigDecimal(boxCount).divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            //此处分为4种情况。：1.如果车身宽度除箱子长度能够除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //2.如果车身宽度除箱子长度不能够除尽，但是除箱子宽度能除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //3.如果车身宽度除箱子长度和宽度都除不尽，看是否能够用车身宽度除以(箱子长+箱子宽)能不能除尽，如果可以那么就用箱子长+宽之和作为排序宽度，进行摆放箱子
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%good.getBoxlength()==0){
                //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                //米数=排数*箱子宽度
                BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                planTake.setLength(length);
            }else if(carWidth%good.getBoxwidth()==0){
                //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                //米数=排数*箱子长度
                BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                planTake.setLength(length);
            }else if(carWidth%(good.getBoxlength()+good.getBoxwidth())==0){
                //就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getBoxlength()+good.getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是箱子的长
                    planTake.setLength(new BigDecimal(good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                        int cha=i*good.getBoxlength()-(location.intValue()+1-i)*good.getBoxwidth();
                        if(cha>-good.getBoxlength()||cha<good.getBoxwidth()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                planTake.setLength(new BigDecimal(i*good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                planTake.setLength(new BigDecimal((location.intValue()+1-i)*good.getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //箱子长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/good.getBoxlength())*good.getBoxlength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/good.getBoxwidth())*good.getBoxwidth();
                if((good.getBoxlength()+good.getBoxwidth())>carWidth){
                    //如果箱长+宽>车宽，直接放弃方案3，只需要比较方案1、2哪个剩余宽度小就用哪个方案。剩余宽度一样，箱长度优先
                    if(lengthLast<=widthLast){
                        //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                        //米数=排数*箱子宽度
                        BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        planTake.setLength(length);
                    }else {
                        //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                        //米数=排数*箱子长度/1000
                        BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        planTake.setLength(length);
                    }
                }else {
                    //托盘长+宽作为计算宽度时，剩余的车身空闲宽度
                    int sumLast=carWidth-(carWidth/(good.getBoxlength()+good.getBoxwidth()))*(good.getBoxlength()+good.getBoxwidth());
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/箱子长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                            //米数=排数*箱子宽度
                            BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            planTake.setLength(length);
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/箱子长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                                //米数=排数*箱子宽度
                                BigDecimal length=row.multiply(new BigDecimal(good.getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                planTake.setLength(length);
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getBoxlength()+good.getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是箱子的长
                                    planTake.setLength(new BigDecimal(good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                        int cha=i*good.getBoxlength()-(location.intValue()+1-i)*good.getBoxwidth();
                                        if(cha>-good.getBoxlength()||cha<good.getBoxwidth()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                planTake.setLength(new BigDecimal(i*good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                planTake.setLength(new BigDecimal((location.intValue()+1-i)*good.getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getBoxlength()+good.getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是箱子的长
                                planTake.setLength(new BigDecimal(good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                    int cha=i*good.getBoxlength()-(location.intValue()+1-i)*good.getBoxwidth();
                                    if(cha>-good.getBoxlength()||cha<good.getBoxwidth()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            planTake.setLength(new BigDecimal(i*good.getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            planTake.setLength(new BigDecimal((location.intValue()+1-i)*good.getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                planTake.setLength(length);
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                planTake.setLength(length);
                            }
                        }
                    }
                }
            }
        }else {
            //托盘件
            //托盘数量=箱数/单托箱数
            BigDecimal trayCount=new BigDecimal(boxCount).divide(new BigDecimal(good.getOnetrayboxcount()),2,BigDecimal.ROUND_HALF_UP);
            //单托高度（mm）=物料箱高度（mm）*单托层数+托盘高度（mm）
            int trayHeight=good.getTrayheight()+good.getBoxheight()*good.getOnetraylayerscount();
            //堆叠层数=车厢高度（mm）/托盘高度（mm）
            int layers=carHeight/trayHeight;
            //所占位置数量，即一个托盘占的一个格子。位置数量=托盘数量/堆叠层数
            BigDecimal location=trayCount.divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            //此处分为4种情况。：1.如果车身宽度除托盘长度能够除尽，那么就用托盘长度作为排序宽度，进行摆放托盘
            //2.如果车身宽度除托盘长度不能够除尽，但是除托盘宽度能除尽，那么就用托盘长度作为排序宽度，进行摆放托盘
            //3.如果车身宽度除托盘长度和宽度都除不尽，看是否能够用车身宽度除以(托盘长+托盘宽)能不能除尽，如果可以那么就用托盘长+宽之和作为排序宽度，进行摆放托盘
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%good.getTraylength()==0){
                //就用托盘长来做标准摆放。那么排数=位置数量/(车身宽度/托盘长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                //米数=排数*托盘宽度
                BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                planTake.setLength(length);
            }else if(carWidth%good.getTraywidth()==0){
                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                //米数=排数*托盘长度
                BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                planTake.setLength(length);
            }else if(carWidth%(good.getTraywidth()+good.getTraylength())==0){
                //就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getTraywidth()+good.getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是托盘的长
                    planTake.setLength(new BigDecimal(good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                        int cha=i*good.getTraylength()-(location.intValue()+1-i)*good.getTraywidth();
                        if(cha>-good.getTraylength()||cha<good.getTraylength()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                planTake.setLength(new BigDecimal(i*good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                planTake.setLength(new BigDecimal((location.intValue()+1-i)*good.getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //托盘长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/good.getTraylength())*good.getTraylength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/good.getTraywidth())*good.getTraywidth();
                //托盘长+宽作为计算宽度时，剩余的车身空闲宽度。如果长+宽超过了车宽则直接放弃
                if((good.getTraywidth()+good.getTraylength())>carWidth){
                    //只需要考虑长和宽2种情况，如果长和宽都满足，那么以长优先
                    if(lengthLast<=widthLast){
                        //长做摆放
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                        //米数=排数*托盘宽度
                        BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        planTake.setLength(length);
                    }else {
                        //用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                        //米数=排数*托盘长度
                        BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        planTake.setLength(length);
                    }
                }else{
                    //如果长+宽<车宽，那么就用3种方式判断
                    int sumLast=carWidth-(carWidth/(good.getTraywidth()+good.getTraylength()))*(good.getTraywidth()+good.getTraylength());
                    System.out.println("托盘件:3种都不行，托盘长+宽摆放剩余宽度"+sumLast);
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/托盘长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                            //米数=排数*托盘宽度
                            BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            planTake.setLength(length);
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/托盘长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                                //米数=排数*托盘宽度
                                BigDecimal length=row.multiply(new BigDecimal(good.getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                planTake.setLength(length);
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getTraywidth()+good.getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是托盘的长
                                    planTake.setLength(new BigDecimal(good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                        int cha=i*good.getTraylength()-(location.intValue()+1-i)*good.getTraywidth();
                                        if(cha>-good.getTraylength()||cha<good.getTraylength()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                planTake.setLength(new BigDecimal(i*good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                planTake.setLength(new BigDecimal((location.intValue()+1-i)*good.getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(good.getTraywidth()+good.getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是托盘的长
                                planTake.setLength(new BigDecimal(good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                    int cha=i*good.getTraylength()-(location.intValue()+1-i)*good.getTraywidth();
                                    if(cha>-good.getTraylength()||cha<good.getTraylength()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            planTake.setLength(new BigDecimal(i*good.getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            planTake.setLength(new BigDecimal((location.intValue()+1-i)*good.getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                planTake.setLength(length);
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/good.getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(good.getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                planTake.setLength(length);
                            }
                        }
                    }
                }
            }
        }
        //计算物料的总体积=物料体积+托盘体积
        //托盘体积系数
        BigDecimal trayRatio=new BigDecimal((good.getTrayratio()+100)).divide(new BigDecimal("100"));
        //物料总体积=箱数*箱长*箱宽*箱高/1000000000*托盘体积系数
        BigDecimal volume=new BigDecimal(good.getBoxlength()).multiply(new BigDecimal(good.getBoxwidth())).multiply(new BigDecimal(good.getBoxheight())).multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000000000"),2,BigDecimal.ROUND_HALF_UP).multiply(trayRatio);
        planTake.setVolume(volume);
        //计算物料的总重量t=单箱重量kg*箱数/1000
        BigDecimal weight=good.getBoxweight().multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
        planTake.setWeight(weight);
        return planTake;
    }
}
