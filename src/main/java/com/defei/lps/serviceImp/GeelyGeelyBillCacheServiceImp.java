package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.GeelyBillCacheService;
import com.defei.lps.uploadUtil.PdfBoxUtilLinHai;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GeelyGeelyBillCacheServiceImp implements GeelyBillCacheService {
    @Autowired
    private GeelyBillCacheMapper geelyBillCacheMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private GeelyBillRecordMapper geelyBillRecordMapper;
    @Autowired
    private PlancacheGeelybillcacheMapper plancacheGeelybillcacheMapper;
    @Autowired
    private PlanRecordMapper planRecordMapper;

    /**
     * PD单批量上传
     * @param files
     * @param supplierCode
     * @param factoryId
     * @return
     */
    @Override
    @Transactional
    public synchronized Result upload(MultipartFile[] files,String supplierCode,int factoryId) {
        Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
        if(factory==null){
            return ResultUtil.error(1,"选择的工厂不存在");
        }
        //获取供应商
        Supplier supplier=supplierMapper.selectBySuppliercodeAndFactoryid(supplierCode,factoryId);
        if(supplier==null){
            return ResultUtil.error(1,factory.getFactoryname()+"工厂中供应商不存在");
        }
        List<Map<String,Object>> list=new ArrayList<>();
        Date now=new Date();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("MM-dd HH:mm:ss");
        //通过上传的PDF文件PD单，获取记录集合
        for(MultipartFile file:files){
            Map<String,Object> map=new HashMap<>();
            map.put("fileName",file.getOriginalFilename());
            map.put("time",simpleDateFormat.format(now));
            List<GeelyBillCache> geelyBillCacheList =new ArrayList<>();
            //用哪个工厂上传就使用那个工厂的上传读取模板类
            if(factory.getFactoryname().contains("临海")){
                geelyBillCacheList = PdfBoxUtilLinHai.getContext(file,supplier,factory);
            }else {
                return ResultUtil.error(1,"工厂:"+factory.getFactoryname()+"没有对应的文件读取模板");
            }
            if(geelyBillCacheList ==null){
                map.put("code",1);
                String message="";
                //用哪个工厂上传就使用那个工厂的上传读取模板类
                if(factory.getFactoryname().contains("临海")){
                    message= PdfBoxUtilLinHai.getMessage();
                }else {
                    return ResultUtil.error(1,"工厂:"+factory.getFactoryname()+"没有对应的文件读取模板");
                }
                map.put("message",message);
            }else {
                for(GeelyBillCache geelyBillCache : geelyBillCacheList){
                    geelyBillCache.setUploadtime(now);
                }
                geelyBillCacheMapper.insertBatch(geelyBillCacheList);
                map.put("code",0);
                map.put("message","上传"+ geelyBillCacheList.size()+"行记录");
            }
            list.add(map);
        }
        return ResultUtil.success(list);
    }

    /**
     * 条件分页查询PD单在途记录
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param billNumber PD单单号
     * @param urgent 是否加急
     * @param routeId 线路id
     * @param uploadDate PD单上传时间
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber,String urgent, int routeId, int factoryId,String uploadDate, int currentPage) {
        //校验参数
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!supplierCode.matches("^[0-9A-Z]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大写字母");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!billNumber.matches("^[0-9A-Z]{0,20}$")){
            return ResultUtil.error(1,"PD单号必须为1-20位的数字、大写字母");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<GeelyBillCache> list= geelyBillCacheMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,urgent,routeId,factoryId,uploadDate,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount= geelyBillCacheMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,urgent,routeId,factoryId,uploadDate);
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
     * 根据id删除
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        GeelyBillCache geelyBillCache = geelyBillCacheMapper.selectByPrimaryKey(id);
        if(geelyBillCache !=null){
            geelyBillCacheMapper.deleteByPrimaryKey(id);
        }
        return ResultUtil.success();
    }

    /**
     * 根据计划id查询出对应的PD单
     * @param planCacheId
     * @return
     */
    @Override
    public Result billCacheByPlancacheid(int planCacheId) {
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(planCacheId);
        if(planCache==null){
            return ResultUtil.error(1,"计划不存在，刷新页面后重试");
        }
        //根据物料id查询未绑定计划的PD单记录
        List<GeelyBillCache> geelyBillCaches = geelyBillCacheMapper.selectUnbindByGoodid(planCache.getGood().getId());
        if(geelyBillCaches.isEmpty()){
            return ResultUtil.error(1,"计划没有对应的PD单记录");
        }
        return ResultUtil.success(geelyBillCaches);
    }

    /**
     * 根据计划id、PD单记录id进行绑定操作
     * @param planCacheId
     * @param billCacheIds PD单记录id集合，格式：id,id,id....
     * @return
     */
    @Override
    public Result billCacheUpdate(int planCacheId, String billCacheIds) {
        PlanCache planCache=planCacheMapper.selectByPrimaryKey(planCacheId);
        if(planCache==null){
            return ResultUtil.error(1,"计划不存在，刷新页面后重试");
        }
        //查询所有的PD单记录
        List<GeelyBillCache> geelyBillCacheList =new ArrayList<>();
        String[] idList= billCacheIds.split(",");
        for(String id:idList){
            if(id.matches("^[1-9]{1}[0-9]{0,10}$")){
                GeelyBillCache geelyBillCache = geelyBillCacheMapper.selectByPrimaryKey(Integer.parseInt(id));
                if(geelyBillCache !=null){
                    geelyBillCacheList.add(geelyBillCache);
                }else {
                    return ResultUtil.error(1,"所选的PD单记录中有不存在的，请刷新页面后重试");
                }
            }
        }
        //所有PD单的数量之和
        int totalCount= geelyBillCacheList.stream().collect(Collectors.summingInt(GeelyBillCache::getCount));
        //计划剩余可取货数量
        int lastCount=planCache.getCount()-planCache.getTakecount();
        if(totalCount>lastCount){
            return ResultUtil.error(1,"所选的PD单记录数量总和"+totalCount+"不可大于计划剩余可取货数量"+lastCount);
        }
        //绑定：1.修改PD单记录中计划日期为计划记录的日期  2.修改计划中的取货数量
        //修改计划的取货数量
        planCache.setTakecount(planCache.getTakecount()+totalCount);
        if(lastCount==totalCount){
            //如果本次绑定数量=剩余可取货数量，那么计划变为在途状态
            planCache.setState("在途");
        }
        planCacheMapper.updateByPrimaryKeySelective(planCache);
        //修改PD单记录的计划日期
        for(GeelyBillCache geelyBillCache : geelyBillCacheList){
            geelyBillCacheMapper.updateByPrimaryKeySelective(geelyBillCache);
        }
        return ResultUtil.success();
    }

    /**
     * 根据扫描的PD单获取所有在途记录
     * @param billNumber
     * @return
     */
    @Override
    public Result billCacheByBillnumber(String billNumber) {
        if(!billNumber.matches("^[0-9A-Z]{1,20}$")){
            return ResultUtil.error(1,"PD单号只能为1-20位的数字、大小字母");
        }
        List<GeelyBillCache> geelyBillCaches = geelyBillCacheMapper.selectByBillnumber(billNumber);
        if(geelyBillCaches.isEmpty()){
            List<GeelyBillRecord> geelyBillRecords = geelyBillRecordMapper.selectByBillnumber(billNumber);
            if(geelyBillRecords.isEmpty()){
                return ResultUtil.error(1,"PD单在系统中找不到");
            }else {
                return ResultUtil.error(1,"PD单已经回执完成");
            }
        }
        return ResultUtil.success(geelyBillCaches);
    }

    /**
     * 扫描PD单编号，获取PD单内容明细
     * @param geelyBillNumber
     * @return
     */
    @Override
    public Result geelyBillCacheDetail(String geelyBillNumber) {
        List<GeelyBillCache> geelyBillCacheList=geelyBillCacheMapper.selectByBillnumber(geelyBillNumber);
        if(geelyBillCacheList.isEmpty()){
            return ResultUtil.error(1,"吉利单据不存在，可以到“已回执吉利单据”中查看是否已经回执了");
        }
        return ResultUtil.success(geelyBillCacheList);
    }

    /**
     * 在途吉利单转为完结吉利单,吉利单据回执操作
     * @param geelyBillCacheId
     * @param count
     * @param remarks
     * @return
     */
    @Override
    @Transactional
    public Result geelyBillRecordAdd(int geelyBillCacheId, int count, String remarks) {
        GeelyBillCache geelyBillCache=geelyBillCacheMapper.selectByPrimaryKey(geelyBillCacheId);
        if(geelyBillCache==null){
            return ResultUtil.error(1,"未回执吉利单据记录不存在，刷新页面重试");
        }
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now =new Date();
        //1.添加吉利单据完结记录
        GeelyBillRecord geelyBillRecord=new GeelyBillRecord();
        geelyBillRecord.setBillnumber(geelyBillCache.getBillnumber());
        geelyBillRecord.setGood(geelyBillCache.getGood());
        geelyBillRecord.setCount(geelyBillCache.getCount());
        geelyBillRecord.setReceivecount(count);
        geelyBillRecord.setBatch(geelyBillCache.getBatch());
        geelyBillRecord.setUrgent(geelyBillCache.getUrgent());
        geelyBillRecord.setUploadtime(simpleDateFormat.format(geelyBillCache.getUploadtime()));
        geelyBillRecord.setReceivetime(now);
        geelyBillRecord.setNeedbind("否");
        //看实收数量和单据数量的关系：1.实收>单据，返回提示再补一个吉利单据并绑定到运输单中 2.相等，正常  3.实收<单据，填写原因
        if(new BigDecimal(geelyBillCache.getCount()).compareTo(new BigDecimal(count))==1){
            //实收数<单据数
            remarks="实收数量少于单据数量"+(geelyBillCache.getCount()-count)+"个:"+remarks;
        }else if(new BigDecimal(geelyBillCache.getCount()).compareTo(new BigDecimal(count))==-1){
            //实收数>单据数
            remarks="实收数量多于单据数量"+(count-geelyBillCache.getCount())+"个:"+remarks;
            geelyBillRecord.setNeedbind("是");
        }
        geelyBillRecord.setBindbillnumber("");
        geelyBillRecord.setRemarks(remarks);
        geelyBillRecordMapper.insertSelective(geelyBillRecord);
        //2.删除在途吉利单据
        geelyBillCacheMapper.deleteByPrimaryKey(geelyBillCacheId);
        //3.修改缺件计划的回执数量
        //根据吉利单号查询绑定的取货计划
        List<PlancacheGeelybillcache> plancacheGeelybillcaches=plancacheGeelybillcacheMapper.selectByGeelybillcacheid(geelyBillCacheId);
        if(!plancacheGeelybillcaches.isEmpty()){
            //看对应的每个计划是否都是最后一个吉利单据回执，如果是就把缺件计划变为完结，否则就只是修改缺件计划的收货数量
            for(PlancacheGeelybillcache plancacheGeelybillcache:plancacheGeelybillcaches){
                //根据计划id、当前吉利单据id查询非当前吉利单据id的记录
                List<PlancacheGeelybillcache> plancacheGeelybillcacheList=plancacheGeelybillcacheMapper.selectExcludeGeelybillcacheidByPlancacheid(plancacheGeelybillcache.getPlancacheid(),geelyBillCacheId);
                if(plancacheGeelybillcacheList.isEmpty()){
                    //当前扫描的单据就是计划对应得最后一个单据，计划需要回执
                    PlanCache planCache=planCacheMapper.selectByPrimaryKey(plancacheGeelybillcache.getPlancacheid());
                    if(planCache!=null){
                        //添加计划完结记录
                        PlanRecorde planRecorde=new PlanRecorde();
                        planRecorde.setPlancacheid(planCache.getId());
                        planRecorde.setGood(planCache.getGood());
                        planRecorde.setCount(planCache.getCount());
                        planRecorde.setMaxcount(planCache.getMaxcount());
                        planRecorde.setMincount(planCache.getMincount());
                        planRecorde.setBoxcount(planCache.getBoxcount());
                        planRecorde.setDate(planCache.getDate());
                        planRecorde.setReceivedate(planCache.getReceivedate());
                        planRecorde.setType(planCache.getType());
                        planRecorde.setUrgent(planCache.getUrgent());
                        planRecorde.setRemarks(planCache.getRemarks());
                        planRecorde.setCreatetime(simpleDateFormat.format(planCache.getCreatetime()));
                        planRecorde.setOvertime(now);
                        planRecordMapper.insertSelective(planRecorde);
                        //删除在途缺件计划
                        planCacheMapper.deleteByPrimaryKey(planCache.getId());
                    }
                }else {
                    //当前扫描的单据就不是计划对应得最后一个单据，计划只用修改收货数量
                    PlanCache planCache=planCacheMapper.selectByPrimaryKey(plancacheGeelybillcache.getPlancacheid());
                    if(planCache!=null){
                        planCache.setReceivecount(planCache.getReceivecount()+count);
                        planCacheMapper.updateByPrimaryKeySelective(planCache);
                    }
                }
            }
        }
        if(new BigDecimal(geelyBillCache.getCount()).compareTo(new BigDecimal(count))==-1){
            return ResultUtil.success("回执成功！由于实收数大于单据数"+(count-geelyBillCache.getCount())+"个，请补一张吉利单据并绑定到当前回执的单据上");
        }
        return ResultUtil.success("回执成功！");
    }
}
