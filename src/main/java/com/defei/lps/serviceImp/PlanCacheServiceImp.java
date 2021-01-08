package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.PlanCacheService;
import com.defei.lps.util.DateUtil;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanCacheServiceImp implements PlanCacheService {
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private GoodMapper goodMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private ShortageMapper shortageMapper;
    @Autowired
    private PlanHandleRecordMapper planHandleRecordMapper;
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;
    @Autowired
    private PlanRecordMapper planRecordMapper;
    @Autowired
    private ParamsMapper paramsMapper;
    @Autowired
    private PlanTakeMapper planTakeMapper;


    /**
     * 修改计划.此功能主要是当供应商给的PD单内容和计划不一致时，计划根据PD单内容进行调整
     * 只能修改数量
     * @param id
     * @param count
     * @return
     */
    @Override
    @Transactional
    public synchronized Result update(int id, int count,String remarks) {
        //检验参数
        if(count<=0){
            return ResultUtil.error(1,"数量必须为正整数");
        }
        //当前用户名称
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        //修改的计划
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(id);
        if(planCache==null){
            return ResultUtil.error(1,"计划不存在，刷新页面后重试");
        }
        if(planCache.getState().equals("未取货")){
            //只有数量、备注2个任何一个发生变化才能修改
            if(count==planCache.getCount()&&planCache.getRemarks().equals(remarks)){
                return ResultUtil.error(1,"该计划未做任何修改");
            }else {
                //只能修改数量、备注
                String content="";
                if(!planCache.getRemarks().equals(remarks)){
                    content+=",备注由“"+planCache.getRemarks()+"”改为“"+remarks+"”";
                    planCache.setRemarks(remarks);
                }
                if(count!=planCache.getCount()){
                    content+=",数量由“"+planCache.getCount()+"”改为“"+count+"”";
                    planCache.setCount(count);
                    int boxCount=0;
                    if(count%planCache.getGood().getOneboxcount()==0){
                        boxCount=count/planCache.getGood().getOneboxcount();
                    }else {
                        boxCount=count/planCache.getGood().getOneboxcount()+1;
                    }
                    planCache.setBoxcount(boxCount);
                    //修改
                    planCacheMapper.updateByPrimaryKeySelective(planCache);
                    //删除所有未确认计划
                    planCacheMapper.deleteByGoodidAndState(planCache.getGood().getId(),"未确认");
                    //根据物料查询，日期升序。查出来的都是已经确认过的计划
                    List<PlanCache> planCacheList=planCacheMapper.selectByGoodid(planCache.getGood().getId());
                    //参与计算的缺件报表集合
                    List<Shortage> shortages=new ArrayList<>();
                    int planCacheTotalCount=0;
                    if(planCacheList.isEmpty()){
                        //没有已经确认的计划，看是否有最近一次已经完成的计划
                        List<PlanRecorde> planRecordeList=planRecordMapper.selectByGoodid(planCache.getGood().getId());
                        if(planRecordeList.isEmpty()){
                            //如果完结缺件计划为空，说明该物料需要从缺件报表的第一个日期开始计算
                            shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(planCache.getGood().getId(),"","");
                        }else {
                            //如果完结缺件计划有，就要从最大日期开始计算缺件报表
                            shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(planCache.getGood().getId(),planRecordeList.get(0).getDate(),"");
                            if(!shortages.isEmpty()){
                                //去掉完结计划的那一天的缺件报表记录
                                shortages.remove(0);
                            }
                        }
                    }else {
                        //在途总数量
                        planCacheTotalCount=planCacheList.stream().collect(Collectors.summingInt(PlanCache::getCount));
                        //有已经确认的计划，就从最近一次确认的计划日期其开始计算
                        shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(planCache.getGood().getId(),planCacheList.get(planCacheList.size()-1).getDate(),"");
                        if(!shortages.isEmpty()){
                            //去掉最近一次确认过的计划的那一天的缺件报表记录
                            shortages.remove(0);
                        }
                    }
                    //如果缺件报表集合不为空，开始生成计划
                    if(!shortages.isEmpty()){
                        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
                        Calendar calendar = Calendar.getInstance();
                        //开始计算缺件计划
                        for(Shortage shortage:shortages){
                            if((planCacheTotalCount+shortage.getStock())<=planCache.getGood().getTriggerstock()){
                                //如果计划结存<=物料触发数量，生成计划
                                PlanCache planCache1=new PlanCache();
                                planCache1.setGood(planCache.getGood());
                                int planCount=0;
                                if((planCache.getGood().getMaxstock()-shortage.getStock())%planCache.getGood().getOneboxcount()==0){
                                    planCount=planCache.getGood().getMaxstock()-shortage.getStock();
                                }else {
                                    planCount=((planCache.getGood().getMaxstock()-shortage.getStock())/planCache.getGood().getOneboxcount())*planCache.getGood().getOneboxcount();
                                }
                                planCache1.setCount(planCount);
                                planCache1.setTakecount(0);
                                planCache1.setReceivecount(0);
                                planCache1.setBoxcount(planCount/planCache.getGood().getOneboxcount());
                                planCache1.setDate(shortage.getDate());
                                //预计到达日期.默认计划都是上午取货，那么如果运输在途天数是带小数的，就向下取整
                                int transitDay=Double.valueOf(planCache.getGood().getSupplier().getTransitday()).intValue();
                                if(transitDay>0){
                                    try {
                                        calendar.setTime(simpleDateFormat.parse(shortage.getDate()));
                                        //把日期增加运输在途天数
                                        calendar.add(Calendar.DATE, transitDay);
                                        planCache1.setReceivedate(simpleDateFormat.format(calendar.getTime()));
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                        planCache1.setReceivedate("");
                                    }
                                }else {
                                    planCache1.setReceivedate(shortage.getDate());
                                }
                                planCache1.setState("未确认");
                                planCache1.setType("系统");
                                planCache1.setRemarks("");
                                planCacheMapper.insertSelective(planCache1);
                                //添加了计划，那么从这一天开始，再往后计算计划时，就要把这个计划的取货数量累加到计划结存种
                                planCacheTotalCount=planCacheTotalCount+planCount;
                            }
                        }
                    }
                }else {
                    //修改
                    planCacheMapper.updateByPrimaryKeySelective(planCache);
                }
                //添加修改记录
                PlanHandleRecord planHandleRecord =new PlanHandleRecord();
                planHandleRecord.setGood(planCache.getGood());
                planHandleRecord.setDate(planCache.getDate());
                planHandleRecord.setContent(content.substring(1));
                SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                planHandleRecord.setCreatetime(simpleDateFormat.format(planCache.getCreatetime()));
                planHandleRecord.setUsername(userName);
                planHandleRecordMapper.insertSelective(planHandleRecord);
            }
        }else {
            return ResultUtil.error(1,"只能修改未取货状态的计划");
        }
        return ResultUtil.success();
    }

    /**
     * 只能删除未取货的计划，删除后立刻进行重新计算计划
     * @param id
     * @return
     */
    /*public Result delete(int id) {
        PlanCache oldPlanCache=planCacheMapper.selectByPrimaryKey(id);
        if(!oldPlanCache.getState().equals("未取货")){
            return ResultUtil.error(1,"只能删除未取货的计划");
        }
        Good good=oldPlanCache.getGood();
        //删除
        planCacheMapper.deleteByPrimaryKey(id);
        //删除所有未确认计划
        planCacheMapper.deleteByGoodidAndState(good.getId(),"未确认");
        //根据物料查询，日期升序。查出来的都是已经确认过的计划
        List<PlanCache> planCacheList=planCacheMapper.selectByGoodid(good.getId());
        //参与计算的缺件报表集合
        List<Shortage> shortages=new ArrayList<>();
        int planCacheTotalCount=0;
        if(planCacheList.isEmpty()){
            //没有已经确认的计划，看是否有最近一次已经完成的计划
            List<PlanRecorde> planRecordeList=planRecordMapper.selectByGoodid(good.getId());
            if(planRecordeList.isEmpty()){
                //如果完结缺件计划为空，说明该物料需要从缺件报表的第一个日期开始计算
                shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),"","");
            }else {
                //如果完结缺件计划有，就要从最大日期开始计算缺件报表
                shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),planRecordeList.get(0).getDate(),"");
                if(!shortages.isEmpty()){
                    //去掉完结计划的那一天的缺件报表记录
                    shortages.remove(0);
                }
            }
        }else {
            //在途总数量
            planCacheTotalCount=planCacheList.stream().collect(Collectors.summingInt(PlanCache::getCount));
            //有已经确认的计划，就从最近一次确认的计划日期其开始计算
            shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),planCacheList.get(planCacheList.size()-1).getDate(),"");
            if(!shortages.isEmpty()){
                //去掉最近一次确认过的计划的那一天的缺件报表记录
                shortages.remove(0);
            }
        }
        //如果缺件报表集合不为空，开始生成计划
        if(!shortages.isEmpty()){
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendar = Calendar.getInstance();
            //开始计算缺件计划
            for(Shortage shortage:shortages){
                if((planCacheTotalCount+shortage.getStock())<=good.getTriggerstock()){
                    //如果计划结存<=物料触发数量，生成计划
                    PlanCache planCache=new PlanCache();
                    planCache.setGood(good);
                    int planCount=0;
                    if((good.getMaxstock()-shortage.getStock())%good.getOneboxcount()==0){
                        planCount=good.getMaxstock()-shortage.getStock();
                    }else {
                        planCount=((good.getMaxstock()-shortage.getStock())/good.getOneboxcount())*good.getOneboxcount();
                    }
                    planCache.setCount(planCount);
                    planCache.setTakecount(0);
                    planCache.setReceivecount(0);
                    planCache.setBoxcount(planCount/good.getOneboxcount());
                    planCache.setDate(shortage.getDate());
                    //预计到达日期.默认计划都是上午取货，那么如果运输在途天数是带小数的，就向下取整
                    int transitDay=Double.valueOf(good.getSupplier().getTransitday()).intValue();
                    if(transitDay>0){
                        try {
                            calendar.setTime(simpleDateFormat.parse(shortage.getDate()));
                            //把日期增加运输在途天数
                            calendar.add(Calendar.DATE, transitDay);
                            planCache.setReceivedate(simpleDateFormat.format(calendar.getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            planCache.setReceivedate("");
                        }
                    }else {
                        planCache.setReceivedate(shortage.getDate());
                    }
                    planCache.setState("未确认");
                    planCache.setType("系统");
                    planCache.setRemarks("");
                    planCacheMapper.insertSelective(planCache);
                    //添加了计划，那么从这一天开始，再往后计算计划时，就要把这个计划的取货数量累加到计划结存种
                    planCacheTotalCount=planCacheTotalCount+planCount;
                }
            }
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //当前用户名称
        String userName= (String)SecurityUtils.getSubject().getPrincipal();
        //添加修改记录
        PlanHandleRecord planHandleRecord =new PlanHandleRecord();
        planHandleRecord.setGood(oldPlanCache.getGood());
        planHandleRecord.setDate(oldPlanCache.getDate());
        planHandleRecord.setContent("计划被删除。原计划内容(数量:"+oldPlanCache.getCount()+",状态:未取货,类型:"+oldPlanCache.getType()+",备注:"+oldPlanCache.getRemarks()+")");
        planHandleRecord.setCreatetime(simpleDateFormat.format(oldPlanCache.getCreatetime()));
        planHandleRecord.setUsername(userName);
        planHandleRecordMapper.insertSelective(planHandleRecord);
        return ResultUtil.success();
    }*/
    @Override
    @Transactional
    public Result delete(int id) {
        PlanCache oldPlanCache=planCacheMapper.selectByPrimaryKey(id);
        if(!oldPlanCache.getState().equals("未取货")){
            return ResultUtil.error(1,"只能删除未取货的计划");
        }
        //只能删除发货日期大于今天的计划
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        try {
            if(simpleDateFormat.parse(oldPlanCache.getDate()).getTime()<=simpleDateFormat.parse(simpleDateFormat.format(new Date())).getTime()){
                return ResultUtil.error(1,"只可删除发货日期大于今天的计划");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return ResultUtil.error(1,"缺件计划的发货日期格式错误");
        }
        //先看该计划是否已经生成了取货计划，如果有关联的取货计划，那么把所有的取货计划删除，取货计划中其余的物料还原到缺件计划中
        List<PlanTake> planTakeList=planTakeMapper.selectGroupPlannumberByPlancacheid(id);
        if(!planTakeList.isEmpty()){
            //不为空，说明有取货计划。把每个取货计划删除，并把其他物料的数量还原到计划中
            for(PlanTake planTake:planTakeList){
                //1.还原其他物料的数量到在途缺件计划中。根据取货计划编号查询
                List<PlanTake> planTakeList1=planTakeMapper.selectByPlannumber(planTake.getPlannumber());
                if(!planTakeList1.isEmpty()){
                    //把每个记录的数量还原到计划中
                    for(PlanTake planTake1:planTakeList1){
                        if(planTake1.getPlancacheid()!=id){
                            PlanCache planCache=planCacheMapper.selectByPrimaryKey(planTake1.getPlancacheid());
                            if(planCache!=null){
                                //修改确认数量。新确认数量=原确认数量-取货计划的数量
                                planCache.setSurecount(planCache.getSurecount()-planTake1.getCount());
                                planCacheMapper.updateByPrimaryKeySelective(planCache);
                            }
                        }
                    }
                }
                //2.删除取货计划
                planTakeMapper.deleteByPlannumber(planTake.getPlannumber());
            }
        }
        //删除在途计划
        planCacheMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param routeId, 线路id
     * @param factoryId 工厂id
     * @param date 计划日期
     * @param state 计划状态
     * @param urgent 是否加急
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode,String goodName,String supplierCode, String supplierName, int routeId, int factoryId, String date,String state,String urgent,String type,int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<PlanCache> list=planCacheMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,date,state,type,routeId,factoryId,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=planCacheMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,date,state,type,routeId,factoryId);
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
    public Result planCacheUpdate(int id) {
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(id);
        if(planCache==null){
            return ResultUtil.error(1,"计划不存在");
        }
        return ResultUtil.success(planCache);
    }

    /**
     * 根据供应商编号查询所有的取货数量不等于计划数量的未取货计划
     * @param supplierCode
     * @return
     */
    @Override
    public Result untakePlanCacheBySupplierCode(String supplierCode) {
        if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }
        //根据供应商编号查询未取货状态的记录，且是取货数量不等于计划数量的记录
        List<PlanCache> planCacheList=planCacheMapper.selectUntakeBySuppliercodeAndState(supplierCode,"未取货");
        if(planCacheList.isEmpty()){
            return ResultUtil.error(1,"该供应商没有未绑定PD单的未取货状态的计划");
        }
        return ResultUtil.success(planCacheList);
    }

    //-------------------------------取货计划生成页面--------------------------

    /**
     * 生成取货计划页面，根据出发地id和目的地id查询所有未确认缺件计划，
     * 根据物料进行显示所有的未确认缺件计划。
     * 把所选物料从今天开始重新计算一遍缺件计划，就相当于上传了一次缺件报表，只不过报表数据未变化，这样就
     * 能克服一个问题：由于取货计划生成页面只显示当天及之后的缺件、缺件计划情况，
     * 那么如果1号上传缺件报表生成计划，当天并未生成取货计划，那么在2号未上传缺件报表之前的时间段内
     * 查看缺件计划，会导致1号的一些紧急计划被漏掉，无法查看。
     * @param startId
     * @param endId
     * @param endType
     * @return
     */
    @Override
    public synchronized Result planCacheByRoute(int startId, int endId, String endType) {
        //看变化比参数是否设置了
        Params params = paramsMapper.selectByName("shortageStockChangeRatio");
        if(params ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化占比:shortageStockChangeRatio");
        }
        String shortageStockChangeRatio= params.getParamvalue();
        //看缺件结存变化数量是否设置了
        Params params1 = paramsMapper.selectByName("shortageStockChangeCount");
        if(params1 ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化数量:shortageStockChangeCount");
        }
        String shortageStockChangeCount= params1.getParamvalue();
        //出发地是区域
        Area area=areaMapper.selectByPrimaryKey(startId);
        if(area==null){
            return ResultUtil.error(1,"区域不存在，刷新后再试");
        }
        List<Route> routeList=new ArrayList<>();
        if(endType.equals("工厂")){
            Factory factory=factoryMapper.selectByPrimaryKey(endId);
            if(factory==null){
                return ResultUtil.error(1,"工厂不存在，刷新后再试");
            }
            Route route=routeMapper.selectByFactoryidAndAreaid(endId,startId);
            if(route==null){
                return ResultUtil.error(1,"区域和工厂未创建线路");
            }
            routeList.add(route);
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
            routeList.addAll(routeList1);
        }
        if(routeList.size()<=0){
            return ResultUtil.error(1,"所选出发地和目的地之间没有分配线路");
        }
        //根据线路id集合查询该线路集合内的所有物料
        List<Good> goodList=goodMapper.selectByRouteids(routeList);
        if(goodList.isEmpty()){
            return ResultUtil.error(1,"所选线路上没有供应商、物料数据");
        }
        //开始日期默认为当天,获取从当天开始一直到缺件报表结束日期为止这期间的日期区间
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String today=simpleDateFormat.format(now);
        //缺件报表记录最大的日期
        Shortage shortageMax=shortageMapper.selectMaxDate();
        if(shortageMax==null){
            return ResultUtil.error(1,"所选线路上的物料没有未来几天的缺件报表记录，请上传缺件报表");
        }
        String maxDate=shortageMax.getDate();
        List<String> dateList=DateUtil.getBetweenDate(today,maxDate);
        if(dateList==null){
            return ResultUtil.error(1,"所选线路上的物料没有未来几天的缺件报表记录，请上传缺件报表");
        }
        //计算每个物料的缺件计划，按照缺件报表上传时的计算逻辑
        List<PlanCache> newPlan=new ArrayList<>();
        for(Good good:goodList){
            //删除所有的该物料的未确认的计划
            planCacheMapper.deleteByGoodidAndState(good.getId(),"未确认");
            //验证：如果该物料上传的缺件报表记录中从今天开始未来几天的所有缺件需求和结存值都是0，则该物料不计算缺件计划
            List<Shortage> shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),today,"");
            //如果当前物料，在日期区间内每天都没有缺件报表记录，那么直接跳过
            if(shortages.isEmpty()){
                continue;
            }
            //是否能创建缺件计划，默认位否
            boolean canCreate=false;
            //判断每一天的需求数量是否为0，如果每天的需求数量都为0，那么无需计算缺件计划
            for(Shortage shortage1:shortages){
                if(shortage1.getNeedcount()!=0){
                    //需求数量不为0，跳出循环，需要创建计划
                    canCreate=true;
                }
            }
            if(canCreate==false){
                shortages=null;
                //无需创建缺件计划，直接循环下一个物料
                continue;
            }
            //获取当前循环物料的配送周期天数
            int transitDay=new BigDecimal(good.getSupplier().getTransitday()).intValue();
            if(good.getSupplier().getTransitday().contains(".")){
                //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
                transitDay++;
            }
            //更新到货结存。把在途计划的取货数量全部加到缺件报表记录中
            for(int i=0;i<shortages.size();i++){
                //查询是否有当天到货的在途记录
                List<PlanCache> planCacheList1=planCacheMapper.selectByGoodidAndReceivedateAndExcludeState(good.getId(),"未确认",shortages.get(i).getDate());
                if(!planCacheList1.isEmpty()){
                    int receiveTotalCount=planCacheList1.stream().collect(Collectors.summingInt(PlanCache::getCount));
                    //更新接下来几天的结存
                    //如果当天到货，且当天的结存小于物料拉动库存，那么就说明时紧急拉动，紧急拉动更新时是从到货当天开始。非紧急是从到货第二天更新库存
                    if(shortages.get(i).getStock()<good.getTriggerstock()){
                        for(int k=i;k<shortages.size();k++){
                            shortages.get(k).setStock(shortages.get(k).getStock()+receiveTotalCount);
                        }
                    }else {
                        //非紧急拉动，从第二天开始更新结存
                        if(i!=(shortages.size()-1)){
                            for(int k=i+1;k<shortages.size();k++){
                                shortages.get(k).setStock(shortages.get(k).getStock()+receiveTotalCount);
                            }
                        }
                    }
                }
            }
            //第1：先判断是否有第一个正常缺件计划。第一个正常缺件计划标准：以上传的第二天为发货日期，往后推运输周期天数得到到货日期，看到货日期后一天的结存是否小于等于物料拉动库存
            //自定义：该物料第一个正常计划的应该到货日期在缺件集合中的下标.默认为运输周期天数+1
            int index=transitDay+1;
            //如果第一个正常计划的到货日期第二天的下标不超过缺件集合最后一个记录的下标，那么就可以生成正常计划
            if((transitDay+2)<=(shortages.size()-1)){
                //从第一个正常缺件计划到货日期后一天开始循环，生成正常缺件计划
                for(int i=(transitDay+2);i<shortages.size();i++){
                    if(shortages.get(i).getStock()<=good.getTriggerstock()){
                        //生成正常缺件计划
                        //最小取货数量。到货日期后运输周期天数内每天需求之和
                        int minCount=0;
                        for(int g=0;g<transitDay;g++){
                            Shortage shortage1=shortages.get(i+g);
                            if(shortage1!=null){
                                minCount+=shortage1.getNeedcount();
                            }
                        }
                        //修正，箱数向上取整
                        if(minCount%good.getOneboxcount()!=0){
                            minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                        }
                        //最大取货数量
                        int maxCount=0;
                        //获取前一天的到货后结存
                        int yestodayPlanStock=shortages.get(i-1).getStock();
                        if(yestodayPlanStock<0){
                            yestodayPlanStock=0;
                        }
                        if((minCount+yestodayPlanStock+shortages.get(i-1).getNeedcount())<good.getMaxstock()){
                            maxCount=good.getMaxstock()-shortages.get(i-1).getNeedcount()-yestodayPlanStock;
                            //箱数向下取整
                            if(maxCount%good.getOneboxcount()!=0){
                                maxCount=(maxCount/good.getOneboxcount()-1)*good.getOneboxcount();
                            }
                        }else {
                            maxCount=minCount;
                        }
                        //添加本次生成的正常缺件计划
                        PlanCache newPlanCache=new PlanCache();
                        newPlanCache.setGood(good);
                        newPlanCache.setCount(maxCount);
                        newPlanCache.setMaxcount(maxCount);
                        newPlanCache.setMincount(minCount);
                        newPlanCache.setSurecount(0);
                        newPlanCache.setTakecount(0);
                        newPlanCache.setReceivecount(0);
                        //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                        newPlanCache.setBoxcount(maxCount/good.getOneboxcount());
                        //发货日期:从当前日期回推运输周期+1天
                        newPlanCache.setDate(shortages.get(i-1-transitDay).getDate());
                        //到货日期为当前的前一天
                        newPlanCache.setReceivedate(shortages.get(i-1).getDate());
                        newPlanCache.setState("未确认");
                        newPlanCache.setType("系统");
                        newPlanCache.setUrgent("否");
                        newPlanCache.setRemarks("");
                        newPlanCache.setCreatetime(now);
                        newPlan.add(newPlanCache);
                    }
                }
            }else {
                //无需生成第一个正常缺件计划，那么说明第一个正常缺件集合到货日期大于了缺件报表中最大日期
                if(index>(shortages.size()-1)){
                    index=shortages.size()-1;
                }
            }
            //生成紧急计划。如果从今天到第一个正常计划的到货日期中任何一天的结存小于物料最小库存，那么就产生紧急计划，送货数量就是每天结存小于物料最小库存的数量
            for(int i=0;i<=index;i++){
                //如果当天结存<=物料拉动库存，就生成计划
                if(shortages.get(i).getStock()<=good.getTriggerstock()){
                    //生成紧急计划
                    //发货日期
                    String sendDate="";
                    //到货日期
                    String receiveDate="";
                    //获取今天到第一个正常计划到货日期的缺件报表集合
                    List<Shortage> fistList=new ArrayList<>();
                    for(int g=0;g<=index;g++){
                        fistList.add(shortages.get(g));
                    }
                    //找到更新后的集合中结存最小的记录,结存升序排序
                    List<Shortage> sortList=fistList.stream().sorted(Comparator.comparing(Shortage::getStock)).collect(Collectors.toList());
                    //最小取货数量=拉动库存-最小结存记录的结存
                    int minCount=good.getTriggerstock()-sortList.get(0).getStock();
                    //最小取货数量，箱数向上取整。如果是没有零头箱，那么总箱数之上再加上一箱。
                    if(minCount%good.getOneboxcount()!=0){
                        minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                    }else {
                        minCount=minCount+good.getOneboxcount();
                    }
                    //推算到货日期就为当前循环的前一天
                    if((i-1)<=0){
                        //到货日期为上传当天或当天之前，那么紧急计划，发货日期和到货日期都为上传当天
                        sendDate=today;
                        receiveDate=today;
                    }else {
                        //到货日期在集合中，那么获取到货日期
                        receiveDate=shortages.get(i-1).getDate();
                        //回推发货日期
                        if((i-1-transitDay)<=0){
                            sendDate=today;
                        }else {
                            sendDate=shortages.get(i-1-transitDay).getDate();
                        }
                    }
                    //看生成的紧急计划是否有在途计划与其发货日期、到货日期都一样，如果有，就把紧急计划合并到在途之中。并把在途计划改为紧急
                    PlanCache planCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),sendDate,receiveDate);
                    if(planCache!=null){
                        //有发货日期、到货日期都一样的计划，就看存在的计划取货数量是否大于当前计划数量，是就不做任何操作。不是则合并：修改原计划取货数量为
                        //当前取货数量+存在计划取货数量，存在计划的最大最小值为当前计划的取货数量+存在计划取货数量，紧急在途为是，添加备注
                        planCache.setCount(planCache.getCount()+minCount);
                        planCache.setMincount(planCache.getCount()+minCount);
                        planCache.setMaxcount(planCache.getCount()+minCount);
                        planCache.setUrgent("是");
                        planCache.setRemarks(today+"新增"+minCount+"的紧急需求，请及时配送");
                        planCacheMapper.updateByPrimaryKeySelective(planCache);
                    }else {
                        //没有发货日期和到货日期都一样的在途记录，那么就添加本次生成的紧急缺件计划
                        PlanCache newPlanCache=new PlanCache();
                        newPlanCache.setGood(good);
                        newPlanCache.setCount(minCount);
                        //紧急拉动计划的最大取货数量=最小取货数量。因为这个计划是补第一个正常计划到货期间不足的数量，不能送太多
                        newPlanCache.setMaxcount(minCount);
                        newPlanCache.setMincount(minCount);
                        newPlanCache.setSurecount(0);
                        newPlanCache.setTakecount(0);
                        newPlanCache.setReceivecount(0);
                        //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                        newPlanCache.setBoxcount(minCount/good.getOneboxcount());
                        newPlanCache.setDate(sendDate);
                        newPlanCache.setReceivedate(receiveDate);
                        newPlanCache.setState("未确认");
                        newPlanCache.setType("系统");
                        newPlanCache.setUrgent("是");
                        newPlanCache.setRemarks("");
                        newPlanCache.setCreatetime(now);
                        newPlan.add(newPlanCache);
                    }
                    //产生了一条紧急拉动，那么就跳出循环
                    break;
                }
            }
        }//物料循环生成未确认计划完毕
        //如果有新增计划，那么批量添加新计划
        if(!newPlan.isEmpty()){
            planCacheMapper.insertBatch(newPlan);
        }
        //有日期区间，就把日期区间每天的每个物料的缺件信息、缺件计划信息、上一次已确认缺件计划信息返回页面
        List<Map<String,Object>> list=new ArrayList<>();
        for(Good good:goodList){
            //如果当前物料，在日期区间内每天都没有缺件报表记录，那么直接跳过
            List<Shortage> shortageList=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),today,maxDate);
            if(shortageList.isEmpty()){
                continue;
            }
            Map<String,Object> map=new HashMap<>();
            //返回1：物料信息
            map.put("good",good);
            //返回2：获取物料对应的最近一次的在途缺件计划或者是最近一次完结缺件计划
            List<PlanCache> lastPlanCaches=planCacheMapper.selectLessDateByGoodidAndState(good.getId(),"在途",today);
            if(!lastPlanCaches.isEmpty()){
                map.put("lastPlan",lastPlanCaches.get(lastPlanCaches.size()-1));
            }else {
                //缺件计划完结记录中根据物料id查询，并以计划日期降序
                List<PlanRecorde> planRecords=planRecordMapper.selectByGoodid(good.getId());
                if(planRecords.isEmpty()){
                    map.put("lastPlan",null);
                }else {
                    //第一个就是最近一次的缺件计划完结记录
                    map.put("lastPlan",planRecords.get(0));
                }
            }
            //返回3：日期区间内每天的在途、未取货、未确认缺件计划
            List<Map<String,Object>> planList=new ArrayList<>();
            for(String date:dateList){
                Map<String,Object> map1=new HashMap<>();
                //获取该发货日期为当天的缺件计划信息
                PlanCache planCache1=planCacheMapper.selectByGoodidAndDate(good.getId(),date);
                if(planCache1!=null){
                    map1.put("plan",planCache1);
                    //判断是否显示复选框：无论状态，只要计划的已确认数量<取货数量，就显示复选框
                    if(planCache1.getSurecount()<planCache1.getCount()){
                        map1.put("checkbox",true);
                    }else {
                        map1.put("checkbox",false);
                    }
                }else {
                    map1.put("plan",null);
                    map1.put("checkbox",false);
                }
                //返回4：日期区间内每天的缺件报表信息
                Shortage shortage1=shortageMapper.selectByGoodidAndDate(good.getId(),date);
                if(shortage1==null){
                    map1.put("shortage",null);
                    //是否显示红色标注
                    map1.put("shortageRed",false);
                }else {
                    map1.put("shortage",shortage1);
                    //如果物料的最大库存为0，则不判断
                    if(shortage1.getGood().getMaxstock()==0){
                        map1.put("shortageRed",false);
                    }else {
                        //判断结存数量是否显示红色，判断标准：1.如果当前结存和上次结存之间的差值占物料最大库存的XX%及以上，这个XX%是可设置的参数
                        //2.当前结存和上次结存之间的差值>XX个，这个XX个是可以设置的参数
                        int cha=shortage1.getStock()-shortage1.getLaststock();
                        if(cha<0){
                            cha=shortage1.getLaststock()-shortage1.getStock();
                        }
                        //计算差值占物料最大库存的比例
                        String max=String.valueOf(shortage1.getGood().getMaxstock());
                        String chaStr=String.valueOf(cha);
                        BigDecimal ratio=new BigDecimal(chaStr).divide(new BigDecimal(max) ,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                        if(ratio.compareTo(new BigDecimal(shortageStockChangeRatio))!=-1){
                            //差值与最大库存占比高于或等于设置的比例，那么再看差值数量是否高于或等于设置的数量
                            if(new BigDecimal(cha).compareTo(new BigDecimal(shortageStockChangeCount))!=-1){
                                //差值数量>=设置的数量，那么就设置位红色
                                //是否显示红色标注
                                map1.put("shortageRed",true);
                            }else {
                                map1.put("shortageRed",false);
                            }
                        }else {
                            //是否显示红色标注
                            map1.put("shortageRed",false);
                        }
                    }
                }
                planList.add(map1);
            }
            map.put("planList",planList);
            list.add(map);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("list",list);
        map.put("dateList",dateList);
        return ResultUtil.success(map);
    }

    /**
     * 选框选中后，根据计划Id查询计划详情
     * @param planCacheId
     * @return
     */
    @Override
    public Result planCacheDetail(int planCacheId) {
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(planCacheId);
        if(planCache==null){
            return ResultUtil.error(1,"选择的缺件计划不存在");
        }
        return ResultUtil.success(planCache);
    }

    /**
     * 页面选择某个缺件计划的复选框后，弹出填入数量的模态框，之后点击确认时调用此函数。
     * 1.此函数，做一次不更改数据库数据，只更改页面数据的操作，并把更改后的页面数据返回页面，让页面显示出来
     * 2.计算选中数量的物料的长、体积、重量信息，返回给页面
     * @param planCacheId 缺件计划的id
     * @param takeDate 选择的发货日期
     * @param chooseCount 填入的数量
     * @param lowHeight 车厢低板高度mm
     * @param carWidth 车厢宽度mm
     * @return
     */
    @Override
    public Result planCacheChoose(int planCacheId, String takeDate,int chooseCount,int lowHeight,int carWidth) {
        if(takeDate.equals("")){
            return ResultUtil.error(1,"请选择取货日期");
        }else if(chooseCount<=0){
            return ResultUtil.error(1,"取货数量必须大于0");
        }else if(lowHeight<=0){
            return ResultUtil.error(1,"车箱高度(mm)必须大于0");
        }else if(carWidth<=0){
            return ResultUtil.error(1,"车箱车宽(mm)必须大于0");
        }
        //验证1：看计划是否存在
        PlanCache oldPlanCache=planCacheMapper.selectByPrimaryKey(planCacheId);
        if(oldPlanCache==null){
            return ResultUtil.error(1,"缺件计划不存在");
        }
        //验证：特别紧急计划必须填要求的数量
        if(oldPlanCache.getUrgent().equals("是")&&oldPlanCache.getMincount()==oldPlanCache.getMaxcount()){
            if(chooseCount!=oldPlanCache.getMincount()){
                return ResultUtil.error(1,"特别紧急的计划必须填最小取货数量来生成取货计划");
            }
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String today=simpleDateFormat.format(now);
        //验证2：看是否有缺件报表
        //日期区间为当天到缺件报表记录中日期最大的日期
        Shortage maxShortage=shortageMapper.selectMaxDate();
        if(maxShortage==null){
            return ResultUtil.error(1,"当前选择的计划没有今天及之后的缺件报表记录，请上传缺件报表");
        }
        List<String> dateList=DateUtil.getBetweenDate(simpleDateFormat.format(now),maxShortage.getDate());
        if(dateList==null){
            return ResultUtil.error(1,"当前选择的计划没有今天及之后的缺件报表记录，请上传缺件报表");
        }
        //验证3：传入的取货日期必须大于当前时间，小于最大缺件报表日期
        try {
            if(simpleDateFormat.parse(takeDate).getTime()<simpleDateFormat.parse(today).getTime()){
                return ResultUtil.error(1,"取货日期时间必须大于等于当前日期");
            }
            if(simpleDateFormat.parse(takeDate).getTime()>simpleDateFormat.parse(maxShortage.getDate()).getTime()){
                return ResultUtil.error(1,"取货日期必须小于等于"+maxShortage.getDate());
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return ResultUtil.error(1,"选择的取货时间格式错误，联系管理员");
        }
        //验证4：传入取货日期必须小于等于当前选择计划日期
        try {
            if(simpleDateFormat.parse(takeDate).getTime()>simpleDateFormat.parse(oldPlanCache.getDate()).getTime()){
                //如果传入取货日期>当前选择的计划日期，返回提示
                return ResultUtil.error(1,"只能选择大于或等于取货日期"+takeDate+"的计划");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return ResultUtil.error(1,"选择的取货时间格式错误，联系管理员");
        }
        //验证5：看所选计划是否时第一个未确认的计划
        List<PlanCache> planCacheList=planCacheMapper.selectLessDateByGoodidAndState(oldPlanCache.getGood().getId(),"未确认",oldPlanCache.getDate());
        if(!planCacheList.isEmpty()){
            return ResultUtil.error(1,"所选计划之前还有未确认的计划");
        }
        //验证6：参数是否设置
        //看变化比参数是否设置了
        Params params = paramsMapper.selectByName("shortageStockChangeRatio");
        if(params ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化占比:shortageStockChangeRatio");
        }
        String shortageStockChangeRatio= params.getParamvalue();
        //看缺件结存变化数量是否设置了
        Params params1 = paramsMapper.selectByName("shortageStockChangeCount");
        if(params1 ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化数量:shortageStockChangeCount");
        }
        String shortageStockChangeCount= params1.getParamvalue();

        //第一步：修改选择计划的发货日期、到货日期、确认数量，但是不更新到数据库，只是返回到页面
        //看是否修改了发货时间
        if(!takeDate.equals(oldPlanCache.getDate())){
            //修改该了发货时间，才判断是否能修该发货时间
            //获取当前物料的配送周期天数
            int transitDay=new BigDecimal(oldPlanCache.getGood().getSupplier().getTransitday()).intValue();
            if(oldPlanCache.getGood().getSupplier().getTransitday().contains(".")){
                //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
                transitDay++;
            }
            Calendar calendar=Calendar.getInstance();
            try {
                //如果原计划的发货日期与到货日期之间天数>=运输周期，那么才需要判断重新推算到货日期
                calendar.setTime(simpleDateFormat.parse(oldPlanCache.getDate()));
                calendar.add(Calendar.DATE, transitDay);
                if(simpleDateFormat.parse(oldPlanCache.getReceivedate()).getTime()>=calendar.getTime().getTime()){
                    //如果原计划的到货日期大>=原计划发货日期+运输周期,说明原计划是正常运输周期的计划，如果也不是加急计划，那么需要重新推算新发货日期的到货日期
                    if(oldPlanCache.getUrgent().equals("否")){
                        //需要推算新的到货日期
                        String receiveDate="";
                        //设置日期为变更后的日期
                        calendar.setTime(simpleDateFormat.parse(takeDate));
                        //把日期增加运输在途天数
                        calendar.add(Calendar.DATE, transitDay);
                        //得到预计到达日期
                        receiveDate=simpleDateFormat.format(calendar.getTime());
                        oldPlanCache.setReceivedate(receiveDate);
                        //如果计算出来的预计到达日期<=原预计到达日期，那么时可以修改日期的
                        if(simpleDateFormat.parse(receiveDate).getTime()>simpleDateFormat.parse(oldPlanCache.getReceivedate()).getTime()){
                            //如果时把到货日期往后推了，即发货日期往后推了，看到后时，结存是否为负数，是负数就不可修改
                            Shortage shortage1=shortageMapper.selectByGoodidAndDate(oldPlanCache.getGood().getId(),receiveDate);
                            if(shortage1!=null){
                                if(shortage1.getStock()<=0){
                                    return ResultUtil.error(1,"修改发货日期后，会导致停线，不可修改发货日期");
                                }
                            }else {
                                return ResultUtil.error(1,"修改发货日期后，无法判断到货时是否停线，不可修改发货日期");
                            }
                        }
                    }
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return ResultUtil.error(1,"选择的取货时间格式错误，联系管理员");
            }
            //验证完毕，可修改当前计划的取货日期
            oldPlanCache.setDate(takeDate);
        }
        //修改已确认数量
        if(chooseCount>(oldPlanCache.getCount()-oldPlanCache.getSurecount())){
            return ResultUtil.error(1,"填入的数量不可超过剩余可选数量"+(oldPlanCache.getCount()-oldPlanCache.getSurecount()));
        }
        oldPlanCache.setSurecount(oldPlanCache.getSurecount()+chooseCount);

        //第二步：返回从当天开始到缺件报表最大日期间的每天的物料缺件信息、物料的计划信息、当前的物料信息、最近一次发货在途或完结记录
        Map<String,Object> map=new HashMap<>();
        //返回1：物料信息
        map.put("good",oldPlanCache.getGood());
        //返回2：获取物料对应的最近一次的在途缺件计划或者是最近一次完结缺件计划
        List<PlanCache> lastPlanCaches=planCacheMapper.selectLessDateByGoodidAndState(oldPlanCache.getGood().getId(),"在途",today);
        if(!lastPlanCaches.isEmpty()){
            map.put("lastPlan",lastPlanCaches.get(lastPlanCaches.size()-1));
        }else {
            //缺件计划完结记录中根据物料id查询，并以计划日期降序
            List<PlanRecorde> planRecords=planRecordMapper.selectByGoodid(oldPlanCache.getGood().getId());
            if(planRecords.isEmpty()){
                map.put("lastPlan",null);
            }else {
                //第一个就是最近一次的缺件计划完结记录
                map.put("lastPlan",planRecords.get(0));
            }
        }
        //返回3：日期区间内每天的在途、未取货、未确认缺件计划和每日的缺件报表记录
        //每日缺件、计缺件划信息集合
        List<Map<String,Object>> planList=new ArrayList<>();
        for(String date:dateList){
            //返回：每天的缺计划信息。根据日期查询每天的缺件计划，如果原本当天没有缺件计划，但是却是选中的发货日期，那么就把选中的计划放入这一天
            PlanCache planCache1=planCacheMapper.selectByGoodidAndDate(oldPlanCache.getGood().getId(),date);
            Map<String,Object> map1=new HashMap<>();
            if(planCache1!=null){
                //如果当天有缺件计划，且缺件计划就是当前选择的计划，并且该缺件计划原发货日期不等于选择的发货日期，那么就不需要把原日期放入这一天
                if(planCache1.getId().intValue()==oldPlanCache.getId().intValue()){
                    if(!takeDate.equals(planCache1.getDate())){
                        System.out.println("同一个计划，但是选择的发货日期和原计划发货日期不一致");
                        //原发货日期不等于选择的发货日期，就返回空
                        map1.put("plan",null);
                        map1.put("checkbox",false);
                        map1.put("checked",false);
                    }else {
                        //原计划发货日期等于选择的发货日期，返回修改后的原计划.且复选框是被选择状态
                        map1.put("plan",oldPlanCache);
                        map1.put("checkbox",true);
                        map1.put("checked",true);
                    }
                }else {
                    //如果查询的缺件计划不是选择的缺件计划，就正常放入这一天中
                    map1.put("plan",planCache1);
                    //判断是否显示复选框：无论状态，只要计划的已确认数量<取货数量，就显示复选框
                    if(planCache1.getSurecount()<planCache1.getCount()){
                        map1.put("checkbox",true);
                        //显示复选框，再看是否需要默认选中
                        map1.put("checked",false);
                    }else {
                        map1.put("checkbox",false);
                        //不显示复选框，默认不选中
                        map1.put("checked",false);
                    }
                }
            }else {
                if(date.equals(takeDate)){
                    //如果原本当天没有缺件计划，但是却是选中的发货日期，那么就把选中的计划放入这一天
                    map1.put("plan",oldPlanCache);
                    map1.put("checkbox",true);
                    //显示复选框并选中
                    map1.put("checked",true);
                }else {
                    //原先没有计划，该日期也不是选择的发货日期，那么直接放入空的计划信息
                    map1.put("plan",null);
                    map1.put("checkbox",false);
                    map1.put("checked",false);
                }
            }
            //返回：日期区间内每天的缺件报表信息
            Shortage shortage1=shortageMapper.selectByGoodidAndDate(oldPlanCache.getGood().getId(),date);
            if(shortage1==null){
                map1.put("shortage",null);
                //是否显示红色标注
                map1.put("shortageRed",false);
            }else {
                map1.put("shortage",shortage1);
                //如果物料的最大库存为0，则不判断
                if(shortage1.getGood().getMaxstock()==0){
                    map1.put("shortageRed",false);
                }else {
                    //判断结存数量是否显示红色，判断标准：1.如果当前结存和上次结存之间的差值占物料最大库存的XX%及以上，这个XX%是可设置的参数
                    //2.当前结存和上次结存之间的差值>XX个，这个XX个是可以设置的参数
                    int cha=shortage1.getStock()-shortage1.getLaststock();
                    if(cha<0){
                        cha=shortage1.getLaststock()-shortage1.getStock();
                    }
                    //计算差值占物料最大库存的比例
                    String max=String.valueOf(shortage1.getGood().getMaxstock());
                    String chaStr=String.valueOf(cha);
                    BigDecimal ratio=new BigDecimal(chaStr).divide(new BigDecimal(max) ,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                    if(ratio.compareTo(new BigDecimal(shortageStockChangeRatio))!=-1){
                        //差值与最大库存占比高于或等于设置的比例，那么再看差值数量是否高于或等于设置的数量
                        if(new BigDecimal(cha).compareTo(new BigDecimal(shortageStockChangeCount))!=-1){
                            //差值数量>=设置的数量，那么就设置位红色
                            //是否显示红色标注
                            map1.put("shortageRed",true);
                        }else {
                            map1.put("shortageRed",false);
                        }
                    }else {
                        //是否显示红色标注
                        map1.put("shortageRed",false);
                    }
                }
            }
            planList.add(map1);
        }
        map.put("planList",planList);

        //第三步：返回该物料被选中的数量计算出来的长、体积、重量信息
        //计算所选的物料的箱数
        int boxCount=0;
        if(chooseCount%oldPlanCache.getGood().getOneboxcount()!=0){
            boxCount=chooseCount/oldPlanCache.getGood().getOneboxcount()+1;
        }else {
            boxCount=chooseCount/oldPlanCache.getGood().getOneboxcount();
        }
        //看该物料是托盘件还是非托盘件，分别计算层数、物料占的位置数、放几排、占车长米数、是用箱子或托盘的长宽哪一边作为靠车宽摆放边
        if(oldPlanCache.getGood().getOnetrayboxcount()==0){
            //非托盘件
            //堆叠层数=车厢高度（mm）/单箱高度（mm）
            int layers=lowHeight/oldPlanCache.getGood().getBoxheight();
            if(layers==0){
                return ResultUtil.error(1,"车高不可低于"+oldPlanCache.getGood().getBoxheight()+"，否则该物料无法装车");
            }
            //层数
            map.put("layers",layers);
            //所占位置数量，即一个箱子占的一个格子。位置数量=箱数/堆叠层数
            BigDecimal location=new BigDecimal(boxCount).divide(new BigDecimal(layers),2,BigDecimal.ROUND_HALF_UP);
            map.put("location",location);
            //此处分为4种情况。：1.如果车身宽度除箱子长度能够除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //2.如果车身宽度除箱子长度不能够除尽，但是除箱子宽度能除尽，那么就用箱子长度作为排序宽度，进行摆放箱子
            //3.如果车身宽度除箱子长度和宽度都除不尽，看是否能够用车身宽度除以(箱子长+箱子宽)能不能除尽，如果可以那么就用箱子长+宽之和作为排序宽度，进行摆放箱子
            //4.如果前三种情况都除不尽，那么就看谁摆放后剩余的车身宽度最小，就用哪种方案摆放
            if(carWidth%oldPlanCache.getGood().getBoxlength()==0){
                //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*箱子宽度
                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","箱长"+oldPlanCache.getGood().getBoxlength());
            }else if(carWidth%oldPlanCache.getGood().getBoxwidth()==0){
                //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*箱子长度
                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","箱宽"+oldPlanCache.getGood().getBoxwidth());
            }else if(carWidth%(oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth())==0){
                //就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                map.put("side","箱长+宽("+oldPlanCache.getGood().getBoxlength()+"+"+oldPlanCache.getGood().getBoxwidth()+")");
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是箱子的长
                    map.put("length",new BigDecimal(oldPlanCache.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                        int cha=i*oldPlanCache.getGood().getBoxlength()-(location.intValue()+1-i)*oldPlanCache.getGood().getBoxwidth();
                        if(cha>-oldPlanCache.getGood().getBoxlength()||cha<oldPlanCache.getGood().getBoxwidth()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                map.put("length",new BigDecimal(i*oldPlanCache.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                map.put("length",new BigDecimal((location.intValue()+1-i)*oldPlanCache.getGood().getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //箱子长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/oldPlanCache.getGood().getBoxlength())*oldPlanCache.getGood().getBoxlength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/oldPlanCache.getGood().getBoxwidth())*oldPlanCache.getGood().getBoxwidth();
                if((oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth())>carWidth){
                    //如果箱长+宽>车宽，直接放弃方案3，只需要比较方案1、2哪个剩余宽度小就用哪个方案。剩余宽度一样，箱长度优先
                    if(lengthLast<=widthLast){
                        //就用箱子长来做标准摆放。那么排数=位置数量/(车身宽度/箱子长度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*箱子宽度
                        BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","箱长"+oldPlanCache.getGood().getBoxlength());
                    }else {
                        //就用箱子宽来做标准摆放。那么排数=位置数量/(车身宽度/箱子宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*箱子长度/1000
                        BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","箱宽"+oldPlanCache.getGood().getBoxwidth());
                    }
                }else {
                    //托盘长+宽作为计算宽度时，剩余的车身空闲宽度
                    int sumLast=carWidth-(carWidth/(oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth()))*(oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth());
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/箱子长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            //米数=排数*箱子宽度
                            BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            map.put("length",length);
                            map.put("side","箱长"+oldPlanCache.getGood().getBoxlength());
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/箱子长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxlength()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子宽度
                                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxwidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱长"+oldPlanCache.getGood().getBoxlength());
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                map.put("side","箱长+宽("+oldPlanCache.getGood().getBoxlength()+"+"+oldPlanCache.getGood().getBoxwidth()+")");
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是箱子的长
                                    map.put("length",new BigDecimal(oldPlanCache.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                        int cha=i*oldPlanCache.getGood().getBoxlength()-(location.intValue()+1-i)*oldPlanCache.getGood().getBoxwidth();
                                        if(cha>-oldPlanCache.getGood().getBoxlength()||cha<oldPlanCache.getGood().getBoxwidth()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                map.put("length",new BigDecimal(i*oldPlanCache.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                map.put("length",new BigDecimal((location.intValue()+1-i)*oldPlanCache.getGood().getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用箱子长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(箱子长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(oldPlanCache.getGood().getBoxlength()+oldPlanCache.getGood().getBoxwidth())),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            map.put("side","箱长+宽("+oldPlanCache.getGood().getBoxlength()+"+"+oldPlanCache.getGood().getBoxwidth()+")");
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是箱子的长
                                map.put("length",new BigDecimal(oldPlanCache.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*箱子长-（位置数量(向上取整)-i）*箱子宽的绝对值<箱子长，那么这个时候就是最优拼载方案
                                    int cha=i*oldPlanCache.getGood().getBoxlength()-(location.intValue()+1-i)*oldPlanCache.getGood().getBoxwidth();
                                    if(cha>-oldPlanCache.getGood().getBoxlength()||cha<oldPlanCache.getGood().getBoxwidth()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            map.put("length",new BigDecimal(i*oldPlanCache.getGood().getBoxlength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            map.put("length",new BigDecimal((location.intValue()+1-i)*oldPlanCache.getGood().getBoxwidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱宽"+oldPlanCache.getGood().getBoxwidth());
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getBoxwidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*箱子长度
                                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getBoxlength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","箱宽"+oldPlanCache.getGood().getBoxwidth());
                            }
                        }
                    }
                }
            }
        }else {
            //托盘件
            //托盘数量=箱数/单托箱数
            BigDecimal trayCount=new BigDecimal(boxCount).divide(new BigDecimal(oldPlanCache.getGood().getOnetrayboxcount()),2,BigDecimal.ROUND_HALF_UP);
            //单托高度（mm）=物料箱高度（mm）*单托层数+托盘高度（mm）
            int trayHeight=oldPlanCache.getGood().getTrayheight()+oldPlanCache.getGood().getBoxheight()*oldPlanCache.getGood().getOnetraylayerscount();
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
            if(carWidth%oldPlanCache.getGood().getTraylength()==0){
                //就用托盘长来做标准摆放。那么排数=位置数量/(车身宽度/托盘长度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*托盘宽度
                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","托盘长"+oldPlanCache.getGood().getTraylength());
            }else if(carWidth%oldPlanCache.getGood().getTraywidth()==0){
                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                //米数=排数*托盘长度
                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                map.put("length",length);
                map.put("side","托盘宽"+oldPlanCache.getGood().getTraywidth());
            }else if(carWidth%(oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength())==0){
                //就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                BigDecimal row=location.divide(new BigDecimal(carWidth/(oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                map.put("row",row);
                map.put("side","托盘长+宽("+oldPlanCache.getGood().getTraylength()+"+"+oldPlanCache.getGood().getTraywidth()+")");
                //米数要以拼装的最长的一边为准。
                if(row.intValue()<=1){
                    //如果只有一排，那么米数就是托盘的长
                    map.put("length",new BigDecimal(oldPlanCache.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                }else {
                    //如果有多排，看哪种方式最合适
                    for(int i=1;i<location.intValue();i++){
                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                        int cha=i*oldPlanCache.getGood().getTraylength()-(location.intValue()+1-i)*oldPlanCache.getGood().getTraywidth();
                        if(cha>-oldPlanCache.getGood().getTraylength()||cha<oldPlanCache.getGood().getTraylength()){
                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                            if(cha>=0){
                                map.put("length",new BigDecimal(i*oldPlanCache.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                map.put("length",new BigDecimal((location.intValue()+1-i)*oldPlanCache.getGood().getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }
                        }
                    }
                }
            }else {
                //托盘长度作为计算宽度时，剩余的车身空闲宽度
                int lengthLast=carWidth-(carWidth/oldPlanCache.getGood().getTraylength())*oldPlanCache.getGood().getTraylength();
                //托盘宽度作为计算宽度时，剩余的车身空闲宽度
                int widthLast=carWidth-(carWidth/oldPlanCache.getGood().getTraywidth())*oldPlanCache.getGood().getTraywidth();
                //托盘长+宽作为计算宽度时，剩余的车身空闲宽度。如果长+宽超过了车宽则直接放弃
                if((oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength())>carWidth){
                    //只需要考虑长和宽2种情况，如果长和宽都满足，那么以长优先
                    if(lengthLast<=widthLast){
                        //长做摆放
                        BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*托盘宽度
                        BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","托盘长"+oldPlanCache.getGood().getTraylength());
                    }else {
                        //用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                        BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                        map.put("row",row);
                        //米数=排数*托盘长度
                        BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                        map.put("length",length);
                        map.put("side","托盘宽"+oldPlanCache.getGood().getTraywidth());
                    }
                }else{
                    //如果长+宽<车宽，那么就用3种方式判断
                    int sumLast=carWidth-(carWidth/(oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength()))*(oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength());
                    System.out.println("托盘件:3种都不行，托盘长+宽摆放剩余宽度"+sumLast);
                    if(lengthLast<=widthLast){
                        if(widthLast<=sumLast){
                            //长<=宽,且宽<=和，那么就用长。那么排数=位置数量/(车身宽度/托盘长度)
                            BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            //米数=排数*托盘宽度
                            BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                            map.put("length",length);
                            map.put("side","托盘长"+oldPlanCache.getGood().getTraylength());
                        }else {
                            //长<=宽,且宽>和
                            if(lengthLast<=sumLast){
                                //长<=宽,且宽>和,且长<=和，用长。那么排数=位置数量/(车身宽度/托盘长度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraylength()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘宽度
                                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraywidth())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘长"+oldPlanCache.getGood().getTraylength());
                            }else {
                                //长<=宽,且宽>和,且长>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                                BigDecimal row=location.divide(new BigDecimal(carWidth/(oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                map.put("side","托盘长+宽("+oldPlanCache.getGood().getTraylength()+"+"+oldPlanCache.getGood().getTraywidth()+")");
                                //米数要以拼装的最长的一边为准。
                                if(row.intValue()<=1){
                                    //如果只有一排，那么米数就是托盘的长
                                    map.put("length",new BigDecimal(oldPlanCache.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                }else {
                                    //如果有多排，看哪种方式最合适
                                    for(int i=1;i<location.intValue();i++){
                                        //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                        int cha=i*oldPlanCache.getGood().getTraylength()-(location.intValue()+1-i)*oldPlanCache.getGood().getTraywidth();
                                        if(cha>-oldPlanCache.getGood().getTraylength()||cha<oldPlanCache.getGood().getTraylength()){
                                            //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                            if(cha>=0){
                                                map.put("length",new BigDecimal(i*oldPlanCache.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }else {
                                                map.put("length",new BigDecimal((location.intValue()+1-i)*oldPlanCache.getGood().getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }else {
                        if(sumLast<widthLast){
                            //长>宽，且宽>和，用和，就用托盘长+宽来做标准摆放。那么排数=位置数量/(车身宽度/(托盘长+宽))
                            BigDecimal row=location.divide(new BigDecimal(carWidth/(oldPlanCache.getGood().getTraywidth()+oldPlanCache.getGood().getTraylength())),2,BigDecimal.ROUND_HALF_UP);
                            map.put("row",row);
                            map.put("side","托盘长+宽("+oldPlanCache.getGood().getTraylength()+"+"+oldPlanCache.getGood().getTraywidth()+")");
                            //米数要以拼装的最长的一边为准。
                            if(row.intValue()<=1){
                                //如果只有一排，那么米数就是托盘的长
                                map.put("length",new BigDecimal(oldPlanCache.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                            }else {
                                //如果有多排，看哪种方式最合适
                                for(int i=1;i<location.intValue();i++){
                                    //如果以托盘长为计算标准，从1开始逐步计算，看i*托盘长-（位置数量(向上取整)-i）*托盘宽的绝对值<托盘长，那么这个时候就是最优拼载方案
                                    int cha=i*oldPlanCache.getGood().getTraylength()-(location.intValue()+1-i)*oldPlanCache.getGood().getTraywidth();
                                    if(cha>-oldPlanCache.getGood().getTraylength()||cha<oldPlanCache.getGood().getTraylength()){
                                        //此时就是最优拼载方案，看哪边的总长大，哪边就是该物料的米数
                                        if(cha>=0){
                                            map.put("length",new BigDecimal(i*oldPlanCache.getGood().getTraylength()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }else {
                                            map.put("length",new BigDecimal((location.intValue()+1-i)*oldPlanCache.getGood().getTraywidth()).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP));
                                        }
                                    }
                                }
                            }
                        }else {
                            //长>宽，且宽<=和
                            if(lengthLast<=sumLast){
                                //长>宽，且宽<=和,且长<=和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘宽"+oldPlanCache.getGood().getTraywidth());
                            }else {
                                //长>宽，且宽<=和,且长>和，用宽
                                //就用托盘宽来做标准摆放。那么排数=位置数量/(车身宽度/托盘宽度)
                                BigDecimal row=location.divide(new BigDecimal(carWidth/oldPlanCache.getGood().getTraywidth()),2,BigDecimal.ROUND_HALF_UP);
                                map.put("row",row);
                                //米数=排数*托盘长度
                                BigDecimal length=row.multiply(new BigDecimal(oldPlanCache.getGood().getTraylength())).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
                                map.put("length",length);
                                map.put("side","托盘宽"+oldPlanCache.getGood().getTraywidth());
                            }
                        }
                    }
                }
            }
        }
        //计算物料的总体积=物料体积+托盘体积
        //托盘体积系数
        BigDecimal trayRatio=new BigDecimal((oldPlanCache.getGood().getTrayratio()+100)).divide(new BigDecimal("100"));
        //物料总体积=箱数*箱长*箱宽*箱高/1000000000*托盘体积系数
        BigDecimal volume=new BigDecimal(oldPlanCache.getGood().getBoxlength()).multiply(new BigDecimal(oldPlanCache.getGood().getBoxwidth())).multiply(new BigDecimal(oldPlanCache.getGood().getBoxheight())).multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000000000"),2,BigDecimal.ROUND_HALF_UP).multiply(trayRatio);
        map.put("volume",volume);
        //计算物料的总重量t=单箱重量kg*箱数/1000
        BigDecimal weight=oldPlanCache.getGood().getBoxweight().multiply(new BigDecimal(boxCount)).divide(new BigDecimal("1000"),2,BigDecimal.ROUND_HALF_UP);
        map.put("weight",weight);
        /*//获取需要计算计划的缺件报表集合
        List<Shortage> shortageList=shortageMapper.selectByGoodidAndDatestartAndDateend(oldPlanCache.getGood().getId(),dateStr,"");
        if(!shortageList.isEmpty()){
            shortageList.remove(0);
        }
        //2.计算缺件计划
        if(!shortageList.isEmpty()){
            //计算计划
            //查询物料所有的计划取货数量总和（这里是已经确认计划的取货数量+当前修改的计划取货数量）
            int planCacheTotalCount=planCacheMapper.selectByGoodid(oldPlanCache.getGood().getId()).stream().collect(Collectors.summingInt(PlanCache::getCount));
            //开始计算缺件计划
            for(Shortage shortage1:shortageList){
                if((planCacheTotalCount+shortage1.getStock())<=oldPlanCache.getGood().getTriggerstock()){
                    //如果计划结存<=物料触发数量，生成计划
                    PlanCache planCache1=new PlanCache();
                    planCache1.setGood(oldPlanCache.getGood());
                    int planCount=0;
                    if((oldPlanCache.getGood().getMaxstock()-shortage1.getStock())%oldPlanCache.getGood().getOneboxcount()==0){
                        planCount=oldPlanCache.getGood().getMaxstock()-shortage1.getStock();
                    }else {
                        planCount=((oldPlanCache.getGood().getMaxstock()-shortage1.getStock())/oldPlanCache.getGood().getOneboxcount())*oldPlanCache.getGood().getOneboxcount();
                    }
                    planCache1.setCount(planCount);
                    planCache1.setTakecount(0);
                    planCache1.setReceivecount(0);
                    planCache1.setBoxcount(planCount/oldPlanCache.getGood().getOneboxcount());
                    planCache1.setDate(shortage1.getDate());
                    //预计到达日期.默认计划都是上午取货，那么如果运输在途天数是带小数的，就向下取整.
                    int transitDay1=Double.valueOf(oldPlanCache.getGood().getSupplier().getTransitday()).intValue();
                    if(transitDay1>0){
                        try {
                            calendar.setTime(simpleDateFormat.parse(shortage1.getDate()));
                            //把日期增加运输在途天数
                            calendar.add(Calendar.DATE, transitDay1);
                            planCache1.setReceivedate(simpleDateFormat.format(calendar.getTime()));
                        } catch (ParseException e) {
                            e.printStackTrace();
                            planCache1.setReceivedate("");
                        }
                    }else {
                        planCache1.setReceivedate(shortage1.getDate());
                    }
                    planCache1.setState("未确认");
                    planCache1.setType("系统");
                    planCache1.setRemarks("");
                    planCacheMapper.insertSelective(planCache1);
                    //添加了计划，那么从这一天开始，再往后计算计划时，就要把这个计划的取货数量累加到计划结存种
                    planCacheTotalCount=planCacheTotalCount+planCount;
                }
            }
        }
        List<Map<String,Object>> planList=new ArrayList<>();
        //3.按照日期的缺件计划集合。生成计划后，把生成的计划查询出来，放入对应的日期区间内，返回前端
        for(String date1:dateList){
            //返回1：物料每天的缺件计划
            PlanCache planCache1=planCacheMapper.selectByGoodidAndDate(oldPlanCache.getGood().getId(),date1);
            Map<String,Object> map1=new HashMap<>();
            if(planCache1!=null){
                map1.put("plan",planCache1);
            }else {
                map1.put("plan",null);
            }
            //返回2：日期区间内每天的缺件报表信息
            Shortage shortage1=shortageMapper.selectByGoodidAndDate(oldPlanCache.getGood().getId(),date1);
            if(shortage1==null){
                map1.put("shortage",null);
                //是否显示红色标注
                map1.put("shortageRed",false);
            }else {
                map1.put("shortage",shortage1);
                //判断结存数量是否显示红色，判断标准：1.如果当前结存和上次结存之间的差值占物料最大库存的XX%及以上，这个XX%是可设置的参数
                //2.当前结存和上次结存之间的差值>XX个，这个XX个是可以设置的参数
                int cha=shortage1.getStock()-shortage1.getLaststock();
                if(cha<0){
                    cha=shortage1.getLaststock()-shortage1.getStock();
                }
                //计算差值占物料最大库存的比例
                String max=String.valueOf(shortage1.getGood().getMaxstock());
                String chaStr=String.valueOf(cha);
                BigDecimal ratio=new BigDecimal(chaStr).divide(new BigDecimal(max) ,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                if(ratio.compareTo(new BigDecimal(shortageStockChangeRatio))!=-1){
                    //差值与最大库存占比高于或等于设置的比例，那么再看差值数量是否高于或等于设置的数量
                    if(new BigDecimal(cha).compareTo(new BigDecimal(shortageStockChangeCount))!=-1){
                        //差值数量>=设置的数量，那么就设置位红色
                        //是否显示红色标注
                        map1.put("shortageRed",true);
                    }else {
                        map1.put("shortageRed",false);
                    }
                }else {
                    //是否显示红色标注
                    map1.put("shortageRed",false);
                }
            }
            planList.add(map1);
        }*/
        return ResultUtil.success(map);
    }

    /**
     * 取消选择某个缺件计划后，根据缺件计划id重新加载该物料的缺件记录、缺件计划信息
     * @param planCacheId
     * @return
     */
    @Override
    public Result planCacheChooseCancel(int planCacheId) {
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(planCacheId);
        if(planCache==null){
            return ResultUtil.error(1,"缺件计划不存在");
        }
        Good good=planCache.getGood();
        //看变化比参数是否设置了
        Params params = paramsMapper.selectByName("shortageStockChangeRatio");
        if(params ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化占比:shortageStockChangeRatio");
        }
        String shortageStockChangeRatio= params.getParamvalue();
        //看缺件结存变化数量是否设置了
        Params params1 = paramsMapper.selectByName("shortageStockChangeCount");
        if(params1 ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化数量:shortageStockChangeCount");
        }
        String shortageStockChangeCount= params1.getParamvalue();
        //开始日期默认为当天,获取从当天开始一直到缺件报表结束日期为止这期间的日期区间
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String today=simpleDateFormat.format(now);
        //缺件报表记录最大的日期
        Shortage shortageMax=shortageMapper.selectMaxDate();
        if(shortageMax==null){
            return ResultUtil.error(1,"物料没有未来几天的缺件报表记录，请上传缺件报表");
        }
        String maxDate=shortageMax.getDate();
        List<String> dateList=DateUtil.getBetweenDate(today,maxDate);
        if(dateList==null){
            return ResultUtil.error(1,"物料没有未来几天的缺件报表记录，请上传缺件报表");
        }
        //返回：日期区间内每天的在途、未取货、未确认缺件计划
        List<Map<String,Object>> planList=new ArrayList<>();
        for(String date:dateList){
            Map<String,Object> map1=new HashMap<>();
            //1.获取该发货日期为当天的缺件计划信息
            PlanCache planCache1=planCacheMapper.selectByGoodidAndDate(good.getId(),date);
            if(planCache1!=null){
                map1.put("plan",planCache1);
                //判断是否显示复选框：无论状态，只要计划的已确认数量<取货数量，就显示复选框
                if(planCache1.getSurecount()<planCache1.getCount()){
                    map1.put("checkbox",true);
                }else {
                    map1.put("checkbox",false);
                }
            }else {
                map1.put("plan",null);
                map1.put("checkbox",false);
            }
            //2：日期区间内每天的缺件报表信息
            Shortage shortage1=shortageMapper.selectByGoodidAndDate(good.getId(),date);
            if(shortage1==null){
                map1.put("shortage",null);
                //是否显示红色标注
                map1.put("shortageRed",false);
            }else {
                map1.put("shortage",shortage1);
                //如果物料的最大库存为0，则不判断
                if(shortage1.getGood().getMaxstock()==0){
                    map1.put("shortageRed",false);
                }else {
                    //判断结存数量是否显示红色，判断标准：1.如果当前结存和上次结存之间的差值占物料最大库存的XX%及以上，这个XX%是可设置的参数
                    //2.当前结存和上次结存之间的差值>XX个，这个XX个是可以设置的参数
                    int cha=shortage1.getStock()-shortage1.getLaststock();
                    if(cha<0){
                        cha=shortage1.getLaststock()-shortage1.getStock();
                    }
                    //计算差值占物料最大库存的比例
                    String max=String.valueOf(shortage1.getGood().getMaxstock());
                    String chaStr=String.valueOf(cha);
                    BigDecimal ratio=new BigDecimal(chaStr).divide(new BigDecimal(max) ,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                    if(ratio.compareTo(new BigDecimal(shortageStockChangeRatio))!=-1){
                        //差值与最大库存占比高于或等于设置的比例，那么再看差值数量是否高于或等于设置的数量
                        if(new BigDecimal(cha).compareTo(new BigDecimal(shortageStockChangeCount))!=-1){
                            //差值数量>=设置的数量，那么就设置位红色
                            //是否显示红色标注
                            map1.put("shortageRed",true);
                        }else {
                            map1.put("shortageRed",false);
                        }
                    }else {
                        //是否显示红色标注
                        map1.put("shortageRed",false);
                    }
                }
            }
            planList.add(map1);
        }
        Map<String,Object> map=new HashMap<>();
        map.put("goodId",good.getId());
        map.put("planList",planList);
        return ResultUtil.success(map);
    }

    /**
     * 修改缺件报表结存数。根据传入的日期、物料id、修改后数量，修改该日期中该物料的缺件报表记录的结存数量
     * 1.修改缺件报表记录
     * 2.根据修改后的缺件报表记录重新计算缺件计划
     * 3.把重新计算后的缺件计划、缺件信息显示到页面
     * @param goodId 物料id
     * @param stock 修改后的缺件报表记录的结存数量
     * @param date 日期
     * @return
     */
    @Override
    public synchronized Result updateShortageByGoodAndDate(int goodId, int stock, String date) {
        //验证1
        Shortage shortage=shortageMapper.selectByGoodidAndDate(goodId,date);
        if(shortage==null){
            return ResultUtil.error(1,"缺件报表记录不存在，刷新页面后重试");
        }
        //验证2.如果数量一样就不修改
        if(stock==shortage.getStock()){
            return ResultUtil.error(1,"修改后数量和修改前一样，无需修改");
        }
        //验证3.看变化比参数是否设置了
        Params params = paramsMapper.selectByName("shortageStockChangeRatio");
        if(params ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化占比:shortageStockChangeRatio");
        }
        String shortageStockChangeRatio= params.getParamvalue();
        //看缺件结存变化数量是否设置了
        Params params1 = paramsMapper.selectByName("shortageStockChangeCount");
        if(params1 ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化数量:shortageStockChangeCount");
        }
        String shortageStockChangeCount= params1.getParamvalue();
        //验证4.日期区间是否存在
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Date now=new Date();
        String today=simpleDateFormat.format(now);
        Shortage maxShortage=shortageMapper.selectMaxDate();
        if(maxShortage==null){
            return ResultUtil.error(1,"缺件报表记录中没有当前物料的未来几天的记录，请上传缺件报表");
        }
        List<String> dateList=DateUtil.getBetweenDate(today,maxShortage.getDate());
        if(dateList==null){
            return ResultUtil.error(1,"缺件报表记录中没有当前物料的未来几天的记录，请上传缺件报表");
        }
        //数量不一样，修改缺件记录
        shortage.setStock(stock);
        shortageMapper.updateByPrimaryKeySelective(shortage);
        //修改该缺件记录之后的所有记录的结存数量
        List<Shortage> shortageList=shortageMapper.selectByGoodidAndDatestartAndDateend(goodId,date,"");
        if(!shortageList.isEmpty()){
            shortageList.remove(0);
        }
        if(!shortageList.isEmpty()){
            int preStock=stock;
            for(Shortage shortage1:shortageList){
                //当前记录的新的结存=前一个记录的新结存-当前记录的需求
                int currentStock=preStock-shortage1.getNeedcount();
                shortage1.setStock(currentStock);
                shortageMapper.updateByPrimaryKeySelective(shortage1);
                //更新前一个记录的新结存,前一个新结存=当前记录的新结存
                preStock=currentStock;
            }
        }
        //修改缺件报表记录后，从传今天开始，重新计算该物料的未确认计划
        Good good=shortage.getGood();
        //物料从今天开始到最大日期的缺件报表集合
        List<Shortage> shortages=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),today,"");
        //1.删除所有未确认计划
        planCacheMapper.deleteByGoodidAndState(goodId,"未确认");
        //2.获取当前循环物料的配送周期天数
        int transitDay=new BigDecimal(good.getSupplier().getTransitday()).intValue();
        if(good.getSupplier().getTransitday().contains(".")){
            //如果配送天数值含有.号，就说明是有小数的，那么统一给配送周期向上取整
            transitDay++;
        }
        //3.看是否有发货日期大于今天的在途计划，有就需要给今天生成的计划添加备注
        String remarks="";
        List<PlanCache> planCacheList=planCacheMapper.selectGreaterReceiveateByGoodidAndExcludeState(good.getId(),"未确认",today);
        if(!planCacheList.isEmpty()){
            remarks="有"+planCacheList.size()+"个已确认计划的发货日期大于"+today+",请和厂家沟通取消";
        }
        //第4：先判断是否有第一个正常缺件计划。第一个正常缺件计划标准：以上传的第二天为发货日期，往后推运输周期天数得到到货日期，看到货日期后一天的结存是否小于等于物料拉动库存
        //自定义：该物料第一个正常计划的应该到货日期在缺件集合中的下标.默认为运输周期天数+1
        int index=transitDay+1;
        //如果第一个正常计划的到货日期第二天的下标不超过缺件集合最后一个记录的下标，那么就可以技术正常计划
        if((transitDay+2)<=(shortages.size()-1)){
            //自定义：前一个正常缺件计划到货后结存.由于前面没有正常缺件计划，所以就默认为第一个正常缺件计划到货当天的上传的缺件报表记录的结存，以此来判断第一个正常缺件计划是否应该生成
            int planStock=shortages.get(transitDay+1).getStock();
            //自定义：前一次正常缺件计划的到货数量,以计划的最大取货数量为准。由于前面没有，所以默认为0
            int planCount=0;
            //从第一个正常缺件计划到货日期后一天开始循环，生成正常缺件计划
            for(int i=(transitDay+2);i<shortages.size();i++){
                //当前循环的收货后结存=前一个计划到货数量-当前循环的需求数量
                int currentStock=planStock-shortages.get(i).getNeedcount();
                if(currentStock<=good.getTriggerstock()){
                    //生成正常缺件计划
                    //最小取货数量。到货日期后一天数的补足拉动库存数+扣除到货日期后一天剩余的运输周期天数的缺件报表记录的需求数
                    int minCount=0;
                    for(int g=0;g<transitDay;g++){
                        Shortage shortage1=shortages.get(i+g);
                        if(shortage!=null){
                            minCount+=shortage1.getNeedcount();
                        }
                    }
                    //修正，箱数向上取整
                    if(minCount%good.getOneboxcount()!=0){
                        minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                    }
                    //最大取货数量
                    int maxCount=0;
                    //获取前一天的到货后结存
                    int yestodayPlanStock=planStock;
                    if(yestodayPlanStock<0){
                        yestodayPlanStock=0;
                    }
                    if((minCount+yestodayPlanStock+shortages.get(i-1).getNeedcount())<good.getMaxstock()){
                        maxCount=good.getMaxstock()-shortages.get(i-1).getNeedcount()-yestodayPlanStock;
                        //箱数向下取整
                        if(maxCount%good.getOneboxcount()!=0){
                            maxCount=(maxCount/good.getOneboxcount()-1)*good.getOneboxcount();
                        }
                    }else {
                        maxCount=minCount;
                    }
                    //更新前一次计划到货数量
                    planCount=maxCount;
                    //更新前一次正常缺件计划的到货后结存。
                    planStock=currentStock+maxCount;
                    //添加本次生成的正常缺件计划
                    PlanCache newPlanCache=new PlanCache();
                    newPlanCache.setGood(good);
                    newPlanCache.setCount(maxCount);
                    newPlanCache.setMaxcount(maxCount);
                    newPlanCache.setMincount(minCount);
                    newPlanCache.setSurecount(0);
                    newPlanCache.setTakecount(0);
                    newPlanCache.setReceivecount(0);
                    //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                    newPlanCache.setBoxcount(maxCount/good.getOneboxcount());
                    //发货日期:从当前日期回推运输周期+1天
                    newPlanCache.setDate(shortages.get(i-1-transitDay).getDate());
                    //到货日期为当前的前一天
                    newPlanCache.setReceivedate(shortages.get(i-1).getDate());
                    newPlanCache.setState("未确认");
                    newPlanCache.setType("系统");
                    newPlanCache.setUrgent("否");
                    newPlanCache.setRemarks(remarks);
                    newPlanCache.setCreatetime(now);
                    planCacheMapper.insertSelective(newPlanCache);
                }
            }
        }else {
            //无需生成第一个正常缺件计划，那么说明第一个正常缺件集合到货日期大于了缺件报表中最大日期
            if(index>(shortages.size()-1)){
                index=shortages.size()-1;
            }
        }
        //5.循环今天到第一个正常计划的到货日期下标，看是否有紧急计划
        List<Shortage> shortageList1=new ArrayList<>();
        for(int i=0;i<=index;i++){
            shortageList1.add(shortages.get(i));
        }
        //自定义：前一天的收货后的结存。默认为第一天的结存+第一天的需求
        int yestodayStock=0;
        for(int i=0;i<=index;i++){
            //当天结存
            int currentStock=yestodayStock-shortages.get(i).getNeedcount();
            //如果当天结存<=物料拉动库存，就生成计划
            if(currentStock<=good.getTriggerstock()){
                //生成紧急计划
                //发货日期
                String sendDate="";
                //到货日期
                String receiveDate="";
                //把期间所有到货的数量更新到缺件报表集合中，找到更新后结存最小的记录。
                for(int t=0;t<index;t++){
                    PlanCache planCache1=planCacheMapper.selectByGoodidAndReceivedate(good.getId(),shortages.get(t).getDate());
                    if(planCache1!=null){
                        //更新后面的结存
                        for(int e=t+1;e<shortageList.size();e++){
                            shortageList.get(e).setStock(shortageList.get(e).getStock()+planCache1.getCount());
                        }
                    }
                }
                //找到更新后的集合中结存最小的记录,结存升序排序
                List<Shortage> sortList=shortageList.stream().sorted(Comparator.comparing(Shortage::getStock)).collect(Collectors.toList());
                //最小取货数量=拉动库存-最小结存记录的结存
                int minCount=good.getTriggerstock()-sortList.get(0).getStock();
                //最小取货数量，箱数向上取整。如果是没有零头箱，那么总箱数之上再加上一箱。
                if(minCount%good.getOneboxcount()!=0){
                    minCount=(minCount/good.getOneboxcount()+1)*good.getOneboxcount();
                }else {
                    minCount=minCount+good.getOneboxcount();
                }
                //推算到货日期就为当前循环的前一天
                if((i-1)<=0){
                    //到货日期为上传当天或当天之前，那么紧急计划，发货日期和到货日期都为上传当天
                    sendDate=today;
                    receiveDate=today;
                }else {
                    //到货日期在集合中，那么获取到货日期
                    receiveDate=shortages.get(i-1).getDate();
                    //回推发货日期
                    if((i-1-transitDay)<=0){
                        sendDate=today;
                    }else {
                        sendDate=shortages.get(i-1-transitDay).getDate();
                    }
                }
                //看生成的紧急计划是否有在途计划于其发货日期、到货日期都一样，如果有，就把紧急计划合并到在途之中。并把在途计划改为紧急
                PlanCache planCache=planCacheMapper.selectByGoodidAndDateAndReceivedate(good.getId(),sendDate,receiveDate);
                if(planCache!=null){
                    planCache.setUrgent("是");
                    planCache.setRemarks(today+"新增"+minCount+"的紧急需求，请及时配送");
                    //修改最小取货数量
                    planCache.setMincount(planCache.getMincount()+minCount);
                    //如果原计划的最大取货数量<修改后最小取货数量，则修改最大取货数量
                    if(planCache.getMaxcount()<(planCache.getMincount()+minCount)){
                        planCache.setMaxcount(planCache.getMincount()+minCount);
                    }
                    //修改取货数量
                    planCache.setCount(planCache.getCount()+minCount);
                    planCacheMapper.updateByPrimaryKeySelective(planCache);
                }else {
                    //没有发货日期和到货日期都一样的在途记录，那么就添加本次生成的紧急缺件计划
                    PlanCache newPlanCache=new PlanCache();
                    newPlanCache.setGood(good);
                    newPlanCache.setCount(minCount);
                    //紧急拉动计划的最大取货数量=最小取货数量。因为这个计划是补第一个正常计划到货期间不足的数量，不能送太多
                    newPlanCache.setMaxcount(minCount);
                    newPlanCache.setMincount(minCount);
                    newPlanCache.setSurecount(0);
                    newPlanCache.setTakecount(0);
                    newPlanCache.setReceivecount(0);
                    //由于前面的总取货数量已经做过收容数取整，所以直接得到箱数
                    newPlanCache.setBoxcount(minCount/good.getOneboxcount());
                    newPlanCache.setDate(sendDate);
                    newPlanCache.setReceivedate(receiveDate);
                    newPlanCache.setState("未确认");
                    newPlanCache.setType("系统");
                    newPlanCache.setUrgent("是");
                    newPlanCache.setRemarks(remarks);
                    newPlanCache.setCreatetime(now);
                    planCacheMapper.insertSelective(newPlanCache);
                }
                //产生了一条紧急拉动，那么就跳出循环
                break;
            }else {
                //查询当天是否有到货，如果有到货，就把到货数量加到当天结存中，然后更新前一天收货后结存
                PlanCache planCache1=planCacheMapper.selectByGoodidAndReceivedate(good.getId(),shortages.get(i).getDate());
                if(planCache1!=null){
                    yestodayStock=currentStock+planCache1.getCount();
                }else{
                    yestodayStock=currentStock;
                }
            }
        }
        //返回从当天开始到缺件报表最大日期间的每天的物料缺件信息、物料的计划信息、当前的物料信息、最近一次发货在途或完结记录
        //返回：日期区间内每天的在途、未取货、未确认缺件计划和每日的缺件报表记录
        List<Map<String,Object>> planList=new ArrayList<>();
        for(String date1:dateList){
            Map<String,Object> map1=new HashMap<>();
            //获取该发货日期为当天的缺件计划信息
            PlanCache planCache1=planCacheMapper.selectByGoodidAndDate(good.getId(),date1);
            if(planCache1!=null){
                map1.put("plan",planCache1);
                //判断是否显示复选框：无论状态，只要计划的已确认数量<取货数量，就显示复选框
                if(planCache1.getSurecount()<planCache1.getCount()){
                    map1.put("checkbox",true);
                }else {
                    map1.put("checkbox",false);
                }
            }else {
                map1.put("plan",null);
                map1.put("checkbox",false);
            }
            //返回4：日期区间内每天的缺件报表信息
            Shortage shortage1=shortageMapper.selectByGoodidAndDate(good.getId(),date1);
            if(shortage1==null){
                map1.put("shortage",null);
                //是否显示红色标注
                map1.put("shortageRed",false);
            }else {
                map1.put("shortage",shortage1);
                //如果物料的最大库存为0，则不判断
                if(shortage1.getGood().getMaxstock()==0){
                    map1.put("shortageRed",false);
                }else {
                    //判断结存数量是否显示红色，判断标准：1.如果当前结存和上次结存之间的差值占物料最大库存的XX%及以上，这个XX%是可设置的参数
                    //2.当前结存和上次结存之间的差值>XX个，这个XX个是可以设置的参数
                    int cha=shortage1.getStock()-shortage1.getLaststock();
                    if(cha<0){
                        cha=shortage1.getLaststock()-shortage1.getStock();
                    }
                    //计算差值占物料最大库存的比例
                    String max=String.valueOf(shortage1.getGood().getMaxstock());
                    String chaStr=String.valueOf(cha);
                    BigDecimal ratio=new BigDecimal(chaStr).divide(new BigDecimal(max) ,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                    if(ratio.compareTo(new BigDecimal(shortageStockChangeRatio))!=-1){
                        //差值与最大库存占比高于或等于设置的比例，那么再看差值数量是否高于或等于设置的数量
                        if(new BigDecimal(cha).compareTo(new BigDecimal(shortageStockChangeCount))!=-1){
                            //差值数量>=设置的数量，那么就设置位红色
                            //是否显示红色标注
                            map1.put("shortageRed",true);
                        }else {
                            map1.put("shortageRed",false);
                        }
                    }else {
                        //是否显示红色标注
                        map1.put("shortageRed",false);
                    }
                }
            }
            planList.add(map1);
        }
        return ResultUtil.success(planList);
    }

    /**
     * 添加手工计划
     * 只能添加在最后一个已确认计划之后
     * @param goodId
     * @param count 数量
     * @param date 日期
     * @return
     */
    @Override
    public synchronized Result add(int goodId, int count, String date,String remarks) {
        //验证1.检测参数
        if(!remarks.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,.，。*-]{1,50}$")){
            return ResultUtil.error(1,"备注只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_,.，。*-)");
        }
        //验证2：传入的取货日期必须大于当前时间
        Date now=new Date();
        SimpleDateFormat simpleDateFormat1=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            if(simpleDateFormat1.parse(date).getTime()<=now.getTime()){
                return ResultUtil.error(1,"取货日期时间必须大于当前日期时间");
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return ResultUtil.error(1,"选择的取货时间格式错误，联系管理员");
        }
        String time=date.split(" ")[1];
        String dateStr=date.split(" ")[0];
        //验证3：物料信息是否存在
        Good good=goodMapper.selectByPrimaryKey(goodId);
        if (good==null){
            return ResultUtil.error(1,"物料不存在");
        }
        //验证4.看变化比参数是否设置了
        Params params = paramsMapper.selectByName("shortageStockChangeRatio");
        if(params ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化占比:shortageStockChangeRatio");
        }
        String shortageStockChangeRatio= params.getParamvalue();
        //验证5.看缺件结存变化数量是否设置了
        Params params1 = paramsMapper.selectByName("shortageStockChangeCount");
        if(params1 ==null){
            return ResultUtil.error(1,"运行参数中没有设置缺件结存变化数量:shortageStockChangeCount");
        }
        //验证6.看物料是否有当天及未来几天对应的缺件报表记录
        List<Shortage> shortageList1=shortageMapper.selectByGoodidAndDatestartAndDateend(goodId,dateStr,"");
        if(shortageList1.isEmpty()){
            return ResultUtil.error(1,"该物料没有"+dateStr+"及以后几天的缺件报表，无法添加缺件计划");
        }
        String shortageStockChangeCount= params1.getParamvalue();
        //验证7.看手工单日期处于什么位置
        PlanCache planCache=new PlanCache();
        planCache.setGood(good);
        planCache.setCount(count);
        int boxCount=0;
        if(count%good.getOneboxcount()==0){
            boxCount=count/good.getOneboxcount();
        }else {
            boxCount=count/good.getOneboxcount()+1;
        }
        planCache.setBoxcount(boxCount);
        planCache.setDate(dateStr);
        //预计到达日期.默认计划都是上午取货，那么如果运输在途天数是带小数的，就向下取整.看传入的日期+时间种时间是否为下午
        //如果时间为12点以后，那么在途运输天数带小数的就向上取整
        int transitDay=0;
        if(Integer.parseInt(time.split(":")[0])>=12){
            transitDay=Double.valueOf(good.getSupplier().getTransitday()).intValue()+1;
        }else {
            transitDay=Double.valueOf(good.getSupplier().getTransitday()).intValue();
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        if(transitDay>0){
            try {
                calendar.setTime(simpleDateFormat.parse(date));
                //把日期增加运输在途天数
                calendar.add(Calendar.DATE, transitDay);
                planCache.setReceivedate(simpleDateFormat.format(calendar.getTime()));
            } catch (ParseException e) {
                e.printStackTrace();
                planCache.setReceivedate("");
            }
        }else {
            planCache.setReceivedate(date);
        }
        planCache.setState("未确认");
        planCache.setType("手工");
        planCache.setRemarks(remarks);
        //根据物料id查询运行中计划记录
        List<PlanCache> planCaches=planCacheMapper.selectByGoodid(good.getId());
        //添加计划、生成添加日期之后的计划
        if(planCaches.isEmpty()){
            //1.当该物料没有任何运行中计划时，直接添加，并重新计算之后的未确认计划
            planCacheMapper.insertSelective(planCache);
            //计算之后的未确认计划
            List<Shortage> shortageList=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),dateStr,"");
            if(!shortageList.isEmpty()){
                shortageList.remove(0);
                //缺件报表不为空，计算缺件计划
                if(!shortageList.isEmpty()){
                    //计算后面的计划
                    int planCacheTotalCount=count;
                    for(Shortage shortage:shortageList){
                        if((planCacheTotalCount+shortage.getStock())<=good.getTriggerstock()){
                            //如果计划结存<=物料触发数量，生成计划
                            PlanCache planCache1=new PlanCache();
                            planCache1.setGood(good);
                            int planCount=0;
                            if((good.getMaxstock()-shortage.getStock())%good.getOneboxcount()==0){
                                planCount=good.getMaxstock()-shortage.getStock();
                            }else {
                                planCount=((good.getMaxstock()-shortage.getStock())/good.getOneboxcount())*good.getOneboxcount();
                            }
                            planCache1.setCount(planCount);
                            planCache1.setTakecount(0);
                            planCache1.setReceivecount(0);
                            planCache1.setBoxcount(planCount/good.getOneboxcount());
                            planCache1.setDate(shortage.getDate());
                            //预计到达日期.默认计划都是上午取货，那么如果运输在途天数是带小数的，就向下取整
                            int transitDay1=Double.valueOf(good.getSupplier().getTransitday()).intValue();
                            if(transitDay1>0){
                                try {
                                    calendar.setTime(simpleDateFormat.parse(shortage.getDate()));
                                    //把日期增加运输在途天数
                                    calendar.add(Calendar.DATE, transitDay1);
                                    planCache1.setReceivedate(simpleDateFormat.format(calendar.getTime()));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                    planCache1.setReceivedate("");
                                }
                            }else {
                                planCache1.setReceivedate(shortage.getDate());
                            }
                            planCache1.setState("未确认");
                            planCache1.setType("系统");
                            planCache1.setRemarks("");
                            planCacheMapper.insertSelective(planCache1);
                            //添加了计划，那么从这一天开始，再往后计算计划时，就要把这个计划的取货数量累加到计划结存种
                            planCacheTotalCount=planCacheTotalCount+planCount;
                        }
                    }
                }
            }
        }else {
            //2.在有计划的前提下，判断是否都是已经确认过的计划，如果都是确认过的计划，那么可以添加，且只能添加到最后一个计划之后。
            // 如果不都是已经确认过的计划，包含了未确认的计划，那么返回提示直接修改计划就可以不用添加计划
            boolean hasUnsure=false;
            for(PlanCache planCache1:planCaches){
                if(planCache1.getState().equals("未确认")){
                    hasUnsure=true;
                    break;
                }
            }
            if(hasUnsure){
                //有已确认的计划，有未确认计划，直接返回提示
                return ResultUtil.error(1,"无需添加新计划，请直接修改已经存在的未确认的计划");
            }else {
                //有已确认的计划，没有未确认的计划，添加新计划。且新计划日期必须大于最后一个已经确认的计划日期
                String lastDate=planCaches.get(planCaches.size()-1).getDate();
                try {
                    if(simpleDateFormat.parse(dateStr).getTime()<=simpleDateFormat.parse(lastDate).getTime()){
                        return ResultUtil.error(1,"新增计划的日期必须大于"+lastDate);
                    }else {
                        //添加新计划
                        planCacheMapper.insertSelective(planCache);
                        //计算之后的未确认计划
                        List<Shortage> shortageList=shortageMapper.selectByGoodidAndDatestartAndDateend(good.getId(),dateStr,"");
                        if(!shortageList.isEmpty()){
                            //去掉添加计划的当天的缺件报表记录
                            shortageList.remove(0);
                            //如果仍然有缺件报表记录，就计算后面的缺件计划
                            if(shortageList.isEmpty()){
                                //前面已确认计划取货数量之和
                                int total=planCaches.stream().collect(Collectors.summingInt(PlanCache::getCount));
                                //计算后面的计划。计划结存=前面已确认的计划取货数量之和+刚添加的新的未确认计划数量
                                int planCacheTotalCount=total+count;
                                for(Shortage shortage:shortageList){
                                    if((planCacheTotalCount+shortage.getStock())<=good.getTriggerstock()){
                                        //如果计划结存<=物料触发数量，生成计划
                                        PlanCache planCache1=new PlanCache();
                                        planCache1.setGood(good);
                                        int planCount=0;
                                        if((good.getMaxstock()-shortage.getStock())%good.getOneboxcount()==0){
                                            planCount=good.getMaxstock()-shortage.getStock();
                                        }else {
                                            planCount=((good.getMaxstock()-shortage.getStock())/good.getOneboxcount())*good.getOneboxcount();
                                        }
                                        planCache1.setCount(planCount);
                                        planCache1.setTakecount(0);
                                        planCache1.setReceivecount(0);
                                        planCache1.setBoxcount(planCount/good.getOneboxcount());
                                        planCache1.setDate(shortage.getDate());
                                        //预计到达日期.默认计划都是上午取货，那么如果运输在途天数是带小数的，就向下取整
                                        int transitDay1=Double.valueOf(good.getSupplier().getTransitday()).intValue();
                                        if(transitDay1>0){
                                            try {
                                                calendar.setTime(simpleDateFormat.parse(shortage.getDate()));
                                                //把日期增加运输在途天数
                                                calendar.add(Calendar.DATE, transitDay1);
                                                planCache1.setReceivedate(simpleDateFormat.format(calendar.getTime()));
                                            } catch (ParseException e) {
                                                e.printStackTrace();
                                                planCache1.setReceivedate("");
                                            }
                                        }else {
                                            planCache1.setReceivedate(shortage.getDate());
                                        }
                                        planCache1.setState("未确认");
                                        planCache1.setType("系统");
                                        planCache1.setRemarks("");
                                        planCacheMapper.insertSelective(planCache1);
                                        //添加了计划，那么从这一天开始，再往后计算计划时，就要把这个计划的取货数量累加到计划结存种
                                        planCacheTotalCount=planCacheTotalCount+planCount;
                                    }
                                }
                            }
                        }
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                    return ResultUtil.error(1,"选择的取货日期格式错误，请联系管理员");
                }
            }
        }
        //查询最大日期的缺件报表记录
        Shortage maxShortage=shortageMapper.selectMaxDate();
        //计划生成完毕，把计划按照日期集合返回到页面
        List<String> dateList=DateUtil.getBetweenDate(simpleDateFormat.format(new Date()),maxShortage.getDate());
        //返回前端
        List<Map<String,Object>> planList=new ArrayList<>();
        //前端数据1：按照日期的缺件计划集合。生成计划后，把生成的计划查询出来，放入对应的日期区间内，返回前端
        for(String date1:dateList){
            Map<String,Object> map=new HashMap<>();
            PlanCache planCache1=planCacheMapper.selectByGoodidAndDate(goodId,date1);
            if(planCache1==null){
                map.put("plan",null);
            }else {
                map.put("plan",planCache1);
            }
            //缺件记录
            Shortage shortage1=shortageMapper.selectByGoodidAndDate(goodId,date1);
            if(shortage1==null){
                map.put("shortage",null);
                //是否显示红色标注
                map.put("shortageRed",false);
            }else {
                map.put("shortage",shortage1);
                //判断结存数量是否显示红色，判断标准：1.如果当前结存和上次结存之间的差值占物料最大库存的XX%及以上，这个XX%是可设置的参数
                //2.当前结存和上次结存之间的差值>XX个，这个XX个是可以设置的参数
                int cha=shortage1.getStock()-shortage1.getLaststock();
                if(cha<0){
                    cha=shortage1.getLaststock()-shortage1.getStock();
                }
                //计算差值占物料最大库存的比例
                String max=String.valueOf(shortage1.getGood().getMaxstock());
                String chaStr=String.valueOf(cha);
                BigDecimal ratio=new BigDecimal(chaStr).divide(new BigDecimal(max) ,2, BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal("100"));
                if(ratio.compareTo(new BigDecimal(shortageStockChangeRatio))!=-1){
                    //差值与最大库存占比高于或等于设置的比例，那么再看差值数量是否高于或等于设置的数量
                    if(new BigDecimal(cha).compareTo(new BigDecimal(shortageStockChangeCount))!=-1){
                        //差值数量>=设置的数量，那么就设置位红色
                        //是否显示红色标注
                        map.put("shortageRed",true);
                    }else {
                        map.put("shortageRed",false);
                    }
                }else {
                    //是否显示红色标注
                    map.put("shortageRed",false);
                }
            }
            planList.add(map);
        }
        return ResultUtil.success(planList);
    }
}
