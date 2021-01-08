package com.defei.lps.serviceImp;

import com.defei.lps.dao.FactoryMapper;
import com.defei.lps.dao.GoodMapper;
import com.defei.lps.dao.SupplierMapper;
import com.defei.lps.entity.Factory;
import com.defei.lps.entity.Good;
import com.defei.lps.entity.Supplier;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.GoodService;
import com.defei.lps.uploadUtil.GoodExcelUpload;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GoodServiceImp implements GoodService {
    @Autowired
    private GoodMapper goodMapper;
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private FactoryMapper factoryMapper;

    /**
     * 添加物料
     * @param goodCode
     * @param goodName
     * @param factoryId
     * @param supplierId
     * @param oneBoxCount
     * @param oneCarCount
     * @param maxStock
     * @param triggerStock
     * @param quotaRatio
     * @param boxType
     * @param boxLength
     * @param boxWidth
     * @param boxHeight
     * @param boxWeight
     * @param returnRatio
     * @param oneTrayBoxCount
     * @param packRemarks
     * @return
     */
    @Override
    public Result add(String goodCode, String goodName, int factoryId, int supplierId, int oneBoxCount, int binCount,  int oneCarCount, int maxStock, int triggerStock, int quotaRatio, String boxType, int boxLength, int boxWidth, int boxHeight, int packBoxLength, int packBoxWidth, int packBoxHeight, String packBoxWeight,String boxWeight,int returnRatio,int oneTrayBoxCount,int oneTrayLayersCount,int trayRatio,int trayLength,int trayWidth,int trayHeight,String packRemarks,String receiver) {
        //校验参数合法性
        if(!goodCode.matches("^[0-9A-Za-z#-]{1,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()。.（）-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*()。.（）-)");
        }else if(oneBoxCount<=0){
            return ResultUtil.error(1,"收容数必须是大于0的整数");
        }else if(binCount<=0){
            return ResultUtil.error(1,"上线收容数必须是大于0的整数");
        }else if(oneCarCount<=0){
            return ResultUtil.error(1,"单耗必须是大于0的整数");
        }else if(maxStock<=0){
            return ResultUtil.error(1,"最大库存数必须是大于0的整数");
        }else if(triggerStock<=0||triggerStock>=maxStock){
            return ResultUtil.error(1,"拉动库存数必须是大于0的整数且小于最大库存数");
        }else if(quotaRatio<0||quotaRatio>100){
            return ResultUtil.error(1,"配额率(%)必须是0-100的整数");
        }else if(!boxType.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,10}$")){
            return ResultUtil.error(1,"箱型只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(boxLength<=0){
            return ResultUtil.error(1,"箱长(mm)必须是大于0的整数");
        }else if(boxWidth<=0){
            return ResultUtil.error(1,"箱宽(mm)必须是大于0的整数");
        }else if(boxHeight<=0){
            return ResultUtil.error(1,"箱高(mm)必须是大于0的整数");
        }else if(packBoxLength<=0){
            return ResultUtil.error(1,"上线箱长(mm)必须是大于0的整数");
        }else if(packBoxWidth<=0){
            return ResultUtil.error(1,"上线箱宽(mm)必须是大于0的整数");
        }else if(packBoxHeight<=0){
            return ResultUtil.error(1,"上线箱高(mm)必须是大于0的整数");
        }else if(!packBoxWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
            return ResultUtil.error(1,"上线满箱重量(kg)必须是大于0的最多保留2位小数的数字");
        }else if(!boxWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
            return ResultUtil.error(1,"满箱重量(kg)必须是大于0的最多保留2位小数的数字");
        }else{
            if(new BigDecimal(boxWeight).compareTo(BigDecimal.ZERO)!=1){
                return ResultUtil.error(1,"满箱重量(kg)必须是大于0");
            }else if(returnRatio<0||returnRatio>100){
                return ResultUtil.error(1,"返空率(%)必须是0-100的整数");
            }else if(!packRemarks.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,:：，。.*-]{0,50}$")){
                return ResultUtil.error(1,"包装描述只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_,:：，。.*-)");
            }else if(!receiver.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,:：，。.*-]{0,20}$")){
                return ResultUtil.error(1,"接收方只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_,:：，。.*-)");
            }else if(oneTrayBoxCount<0){
                return ResultUtil.error(1,"单托箱数必须是大于等于0的整数");
            }else if(oneTrayBoxCount==0){
                //单托箱数==0说明是非托盘件，那么托盘体积占比必须为0
                if(trayRatio!=0){
                    return ResultUtil.error(1,"非托盘件的托盘体积占比必须是0");
                }else if(oneTrayLayersCount!=0){
                    return ResultUtil.error(1,"非托盘件的托盘层数必须是0");
                }else if(trayLength!=0){
                    return ResultUtil.error(1,"非托盘件的托盘长度必须是0");
                }else if(trayWidth!=0){
                    return ResultUtil.error(1,"非托盘件的托盘宽度必须是0");
                }else if(trayHeight!=0){
                    return ResultUtil.error(1,"非托盘件的托盘高度必须是0");
                }
            }else if(oneTrayBoxCount>0){
                //单托箱数>0，说明是托盘件，那么托盘体积必须大于0
                if(trayRatio<=0||trayRatio>=100){
                    return ResultUtil.error(1,"托盘件的托盘体积占比必须是1-99的整数");
                }else if(oneTrayLayersCount<=0||oneTrayLayersCount>=100){
                    return ResultUtil.error(1,"托盘件的托盘层数必须是1-99的整数");
                }else if(trayLength<=0){
                    return ResultUtil.error(1,"托盘件的托盘长度必须是大于0的整数");
                }else if(trayWidth<=0){
                    return ResultUtil.error(1,"托盘件的托盘宽度必须是大于0的整数");
                }else if(trayHeight<=0){
                    return ResultUtil.error(1,"托盘件的托盘高度必须是大于0的整数");
                }
            }
        }
        //工厂是否存在
        Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
        if(factory==null){
            return ResultUtil.error(1,"选择的工厂不存在，刷新页面后重试");
        }
        //供应商是否存在
        Supplier supplier=supplierMapper.selectByPrimaryKey(supplierId);
        if(supplier==null){
            return ResultUtil.error(1,"供应商不存在，刷新页面后重试");
        }
        //检测：物料编号+供应商编号+工厂id是否存在
        Good good1=goodMapper.selectByGoodcodeAndSupplierid(goodCode,supplierId);
        if(good1!=null){
            return ResultUtil.error(1,"物料信息已存在");
        }
        //添加
        Good good=new Good();
        good.setGoodcode(goodCode);
        good.setGoodname(goodName);
        good.setSupplier(supplier);
        good.setOneboxcount(oneBoxCount);
        good.setBincount(binCount);
        good.setOnecarcount(oneCarCount);
        good.setMaxstock(maxStock);
        good.setTriggerstock(triggerStock);
        good.setQuotaratio(quotaRatio);
        good.setBoxtype(boxType);
        good.setBoxlength(boxLength);
        good.setBoxwidth(boxWidth);
        good.setBoxheight(boxHeight);
        good.setPackboxlength(packBoxLength);
        good.setPackboxwidth(packBoxWidth);
        good.setPackboxheight(packBoxHeight);
        good.setPackboxweight(new BigDecimal(packBoxWeight));
        good.setBoxweight(new BigDecimal(boxWeight));
        good.setReturnratio(returnRatio);
        good.setOnetrayboxcount(oneTrayBoxCount);
        good.setOnetraylayerscount(oneTrayLayersCount);
        good.setTrayratio(trayRatio);
        good.setTraylength(trayLength);
        good.setTraywidth(trayWidth);
        good.setTrayheight(trayHeight);
        good.setPackremarks(packRemarks);
        good.setReceiver(receiver);
        goodMapper.insertSelective(good);
        return ResultUtil.success();
    }

    /**
     * 批量上传添加。重复的修改，不重复的添加
     * @param excelFile
     * @return
     */
    @Override
    public Result upload(MultipartFile excelFile) {
        GoodExcelUpload ge=new GoodExcelUpload();
        List<Good> list=ge.getList(excelFile);
        if(list==null){
            return ResultUtil.error(1,ge.getMessage());
        }
        //验证是否重复
        List<Good> lastList=new ArrayList<>();
        int updateCount=0;
        for(Good good:list){
            Good good1=goodMapper.selectByGoodcodeAndSupplierid(good.getGoodcode(),good.getSupplier().getId());
            if(good1==null){
                lastList.add(good);
            }else {
                //修改物料信息
                good.setId(good1.getId());
                goodMapper.updateByPrimaryKeySelective(good);
                updateCount++;
            }
        }
        if(!lastList.isEmpty()){
            //批量插入
            goodMapper.insertBatch(lastList);
        }
        return ResultUtil.success("上传"+list.size()+"条记录，新增"+lastList.size()+"条，修改"+updateCount+"条");
    }

    /**
     * 根据id修改物料信息
     * @param id
     * @param goodCode
     * @param goodName
     * @param factoryId
     * @param supplierId
     * @param oneBoxCount
     * @param oneCarCount
     * @param maxStock
     * @param triggerStock
     * @param quotaRatio
     * @param boxType
     * @param boxLength
     * @param boxWidth
     * @param boxHeight
     * @param boxWeight
     * @param returnRatio
     * @param oneTrayBoxCount
     * @param packRemarks
     * @return
     */
    @Override
    public Result update(int id, String goodCode, String goodName, int factoryId, int supplierId, int oneBoxCount, int binCount, int oneCarCount, int maxStock, int triggerStock, int quotaRatio, String boxType, int boxLength, int boxWidth, int boxHeight,int packBoxLength, int packBoxWidth, int packBoxHeight, String packBoxWeight, String boxWeight,int returnRatio,int oneTrayBoxCount,int oneTrayLayersCount,int trayRatio,int trayLength,int trayWidth,int trayHeight,String packRemarks,String receiver) {
        //校验参数合法性
        if(!goodCode.matches("^[0-9A-Za-z#-]{1,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()。.（）-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*()。.（）-)");
        }else if(oneBoxCount<=0){
            return ResultUtil.error(1,"收容数必须是大于0的整数");
        }else if(binCount<=0){
            return ResultUtil.error(1,"上线收容数必须是大于0的整数");
        }else if(oneCarCount<=0){
            return ResultUtil.error(1,"单耗必须是大于0的整数");
        }else if(maxStock<=0){
            return ResultUtil.error(1,"最大库存数必须是大于0的整数");
        }else if(triggerStock<=0||triggerStock>=maxStock){
            return ResultUtil.error(1,"拉动库存数必须是大于0的整数且小于最大库存数");
        }else if(quotaRatio<0||quotaRatio>100){
            return ResultUtil.error(1,"配额率(%)必须是0-100的整数");
        }else if(!boxType.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,10}$")){
            return ResultUtil.error(1,"箱型只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(boxLength<=0){
            return ResultUtil.error(1,"箱长(mm)必须是大于0的整数");
        }else if(boxWidth<=0){
            return ResultUtil.error(1,"箱宽(mm)必须是大于0的整数");
        }else if(boxHeight<=0){
            return ResultUtil.error(1,"箱高(mm)必须是大于0的整数");
        }else if(packBoxLength<=0){
            return ResultUtil.error(1,"上线箱长(mm)必须是大于0的整数");
        }else if(packBoxWidth<=0){
            return ResultUtil.error(1,"上线箱宽(mm)必须是大于0的整数");
        }else if(packBoxHeight<=0){
            return ResultUtil.error(1,"上线箱高(mm)必须是大于0的整数");
        }else if(!packBoxWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
            return ResultUtil.error(1,"上线满箱重量(kg)必须是大于0的最多保留2位小数的数字");
        }else if(!boxWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
            return ResultUtil.error(1,"满箱重量(kg)必须是大于0的最多保留2位小数的数字");
        }else{
            if(new BigDecimal(boxWeight).compareTo(BigDecimal.ZERO)!=1){
                return ResultUtil.error(1,"满箱重量(kg)必须是大于0");
            }else if(returnRatio<0||returnRatio>100){
                return ResultUtil.error(1,"返空率(%)必须是0-100的整数");
            }else if(!packRemarks.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,:：，。.*-]{0,50}$")){
                return ResultUtil.error(1,"包装描述只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_,:：，。.*-)");
            }else if(!receiver.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,:：，。.*-]{0,20}$")){
                return ResultUtil.error(1,"接收方只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_,:：，。.*-)");
            }else if(oneTrayBoxCount<0){
                return ResultUtil.error(1,"单托箱数必须是大于等于0的整数");
            }else if(oneTrayBoxCount==0){
                //单托箱数==0说明是非托盘件，那么托盘体积占比必须为0
                if(trayRatio!=0){
                    return ResultUtil.error(1,"非托盘件的托盘体积占比必须是0");
                }else if(oneTrayLayersCount!=0){
                    return ResultUtil.error(1,"非托盘件的托盘层数必须是0");
                }else if(trayLength!=0){
                    return ResultUtil.error(1,"非托盘件的托盘长度必须是0");
                }else if(trayWidth!=0){
                    return ResultUtil.error(1,"非托盘件的托盘宽度必须是0");
                }else if(trayHeight!=0){
                    return ResultUtil.error(1,"非托盘件的托盘高度必须是0");
                }
            }else if(oneTrayBoxCount>0){
                //单托箱数>0，说明是托盘件，那么托盘体积必须大于0
                if(trayRatio<=0||trayRatio>=100){
                    return ResultUtil.error(1,"托盘件的托盘体积占比必须是1-99的整数");
                }else if(oneTrayLayersCount<=0||oneTrayLayersCount>=100){
                    return ResultUtil.error(1,"托盘件的托盘层数必须是1-99的整数");
                }else if(trayLength<=0){
                    return ResultUtil.error(1,"托盘件的托盘长度必须是大于0的整数");
                }else if(trayWidth<=0){
                    return ResultUtil.error(1,"托盘件的托盘宽度必须是大于0的整数");
                }else if(trayHeight<=0){
                    return ResultUtil.error(1,"托盘件的托盘高度必须是大于0的整数");
                }
            }
        }
        //物料是否存在
        Good good=goodMapper.selectByPrimaryKey(id);
        if(good==null){
            return ResultUtil.error(1,"物料信息不存在，刷新页面后重试");
        }
        //工厂是否存在
        Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
        if(factory==null){
            return ResultUtil.error(1,"选择的工厂不存在，刷新页面后重试");
        }
        //供应商是否存在
        Supplier supplier=supplierMapper.selectByPrimaryKey(supplierId);
        if(supplier==null){
            return ResultUtil.error(1,"供应商不存在，刷新页面后重试");
        }
        //修改后的物料信息是否存在
        if(!goodCode.equals(good.getGoodcode())||supplierId!=good.getSupplier().getId()||factoryId!=good.getSupplier().getRoute().getFactory().getId()){
            Good good1=goodMapper.selectByGoodcodeAndSupplierid(goodCode,supplierId);
            if(good1!=null){
                return ResultUtil.error(1,"修改后的物料信息已经存在");
            }
        }
        //修改
        good.setGoodcode(goodCode);
        good.setGoodname(goodName);
        good.setSupplier(supplier);
        good.setOneboxcount(oneBoxCount);
        good.setBincount(binCount);
        good.setOnecarcount(oneCarCount);
        good.setMaxstock(maxStock);
        good.setTriggerstock(triggerStock);
        good.setQuotaratio(quotaRatio);
        good.setBoxtype(boxType);
        good.setBoxlength(boxLength);
        good.setBoxwidth(boxWidth);
        good.setBoxheight(boxHeight);
        good.setPackboxlength(packBoxLength);
        good.setPackboxwidth(packBoxWidth);
        good.setPackboxheight(packBoxHeight);
        good.setPackboxweight(new BigDecimal(packBoxWeight));
        good.setBoxweight(new BigDecimal(boxWeight));
        good.setReturnratio(returnRatio);
        good.setOnetrayboxcount(oneTrayBoxCount);
        good.setOnetraylayerscount(oneTrayLayersCount);
        good.setTrayratio(trayRatio);
        good.setTraylength(trayLength);
        good.setTraywidth(trayWidth);
        good.setTrayheight(trayHeight);
        good.setPackremarks(packRemarks);
        good.setReceiver(receiver);
        goodMapper.updateByPrimaryKeySelective(good);
        return ResultUtil.success();
    }

    /**
     * 删除
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        goodMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param boxType
     * @param factoryId
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String boxType, int factoryId, int currentPage) {
        if(!goodCode.matches("^[0-9A-Za-z#-]{0,30}$")){
            return ResultUtil.error(1,"物料编号只能是1-30位的数字、大小写字母、特殊字符(#-)");
        }else if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }if(!supplierCode.matches("^[0-9A-Za-z-]{0,6}$")){
            return ResultUtil.error(1,"供应商编号只能是1-6位的数字、大小写字母、特殊字符(-)");
        }else if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,50}$")){
            return ResultUtil.error(1,"供应商名称只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }else if(!boxType.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,10}$")){
            return ResultUtil.error(1,"包装箱类型只能是1-10位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        //工厂是否存在
        if(factoryId!=0){
            Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
            if(factory==null){
                return ResultUtil.error(1,"选择的工厂不存在，刷新页面后重试");
            }
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Good> list=goodMapper.selectLimitByCondition(goodCode,goodName ,supplierName,supplierCode,boxType,factoryId,0,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=goodMapper.selectCountByCondition(goodCode,goodName ,supplierName,supplierCode,boxType,factoryId,0);
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
    public Result goodById(int id) {
        Good good=goodMapper.selectByPrimaryKey(id);
        if(good==null){
            return ResultUtil.error(1,"物料信息不存在");
        }
        return ResultUtil.success(good);
    }

    /**
     * 物料信息下载
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param boxType
     * @param factoryId
     * @param response
     */
    @Override
    public void download(String goodCode, String goodName, String supplierCode, String supplierName, String boxType, int factoryId, HttpServletResponse response) {
        //获取结果
        List<Good> list=goodMapper.selectByCondition(goodCode,goodName,supplierName,supplierCode,boxType,factoryId,0);
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
        goodCodeCell.setCellValue("物料编号");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("物料名称");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("供应商编号");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("供应商名称");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("所属工厂");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("收容数");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(6);
        batchCell.setCellValue("上线收容数");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(7);
        typeCell.setCellValue("单耗");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(8);
        nameCell.setCellValue("最大库存");
        nameCell.setCellStyle(titleCellStyle);
        Cell timeCell=titleRow.createCell(9);
        timeCell.setCellValue("拉动库存");
        timeCell.setCellStyle(titleCellStyle);
        Cell time2Cell=titleRow.createCell(10);
        time2Cell.setCellValue("配额率(%)");
        time2Cell.setCellStyle(titleCellStyle);
        Cell time3Cell=titleRow.createCell(11);
        time3Cell.setCellValue("箱型");
        time3Cell.setCellStyle(titleCellStyle);
        Cell cell12=titleRow.createCell(12);
        cell12.setCellValue("箱长(mm)");
        cell12.setCellStyle(titleCellStyle);
        Cell cell13=titleRow.createCell(13);
        cell13.setCellValue("箱宽(mm)");
        cell13.setCellStyle(titleCellStyle);
        Cell cell14=titleRow.createCell(14);
        cell14.setCellValue("箱高(mm)");
        cell14.setCellStyle(titleCellStyle);
        Cell cell15=titleRow.createCell(15);
        cell15.setCellValue("上线箱长(mm)");
        cell15.setCellStyle(titleCellStyle);
        Cell cell16=titleRow.createCell(16);
        cell16.setCellValue("上线箱宽(mm)");
        cell16.setCellStyle(titleCellStyle);
        Cell cell17=titleRow.createCell(17);
        cell17.setCellValue("上线箱高(mm)");
        cell17.setCellStyle(titleCellStyle);
        Cell cell18=titleRow.createCell(18);
        cell18.setCellValue("上线满箱重量(kg)");
        cell18.setCellStyle(titleCellStyle);
        Cell cell19=titleRow.createCell(19);
        cell19.setCellValue("满箱重量(kg)");
        cell19.setCellStyle(titleCellStyle);
        Cell cell20=titleRow.createCell(20);
        cell20.setCellValue("返空率(%)");
        cell20.setCellStyle(titleCellStyle);
        Cell cell21=titleRow.createCell(21);
        cell21.setCellValue("单托箱数");
        cell21.setCellStyle(titleCellStyle);
        Cell cell22=titleRow.createCell(22);
        cell22.setCellValue("单托层数");
        cell22.setCellStyle(titleCellStyle);
        Cell cell23=titleRow.createCell(23);
        cell23.setCellValue("托盘体积占比(%)");
        cell23.setCellStyle(titleCellStyle);
        Cell cell24=titleRow.createCell(24);
        cell24.setCellValue("托盘长(mm)");
        cell24.setCellStyle(titleCellStyle);
        Cell cell25=titleRow.createCell(25);
        cell25.setCellValue("托盘宽(mm)");
        cell25.setCellStyle(titleCellStyle);
        Cell cell26=titleRow.createCell(26);
        cell26.setCellValue("托盘高(mm)");
        cell26.setCellStyle(titleCellStyle);
        Cell cell27=titleRow.createCell(27);
        cell27.setCellValue("包装描述");
        cell27.setCellStyle(titleCellStyle);
        Cell cell28=titleRow.createCell(28);
        cell28.setCellValue("接收方");
        cell28.setCellStyle(titleCellStyle);
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
            for (int i = 0; i < list.size(); i++) {
                //每循环一次，创建一行。由于第一行是标题行，所以行下标从1开始
                Row bodyRow = sheet.createRow(i + 1);
                //给每行创建列，第一列下标为0,并给每列添加内容
                //物料编号
                Cell cell0 = bodyRow.createCell(0);
                cell0.setCellValue(list.get(i).getGoodcode());
                cell0.setCellStyle(bodyCellStyle);
                cell0.setCellType(Cell.CELL_TYPE_STRING);
                //物料名称
                Cell cell1 = bodyRow.createCell(1);
                cell1.setCellValue(list.get(i).getGoodname());
                cell1.setCellStyle(bodyCellStyle);
                cell1.setCellType(Cell.CELL_TYPE_STRING);
                //供应商编号
                Cell cell2 = bodyRow.createCell(2);
                cell2.setCellValue(list.get(i).getSupplier().getSuppliercode());
                cell2.setCellStyle(bodyCellStyle);
                cell2.setCellType(Cell.CELL_TYPE_STRING);
                //供应商名称
                Cell cell3 = bodyRow.createCell(3);
                cell3.setCellValue(list.get(i).getSupplier().getSuppliername());
                cell3.setCellStyle(bodyCellStyle);
                cell3.setCellType(Cell.CELL_TYPE_STRING);
                //所属工厂
                Cell cell4 = bodyRow.createCell(4);
                cell4.setCellValue(list.get(i).getSupplier().getRoute().getFactory().getFactoryname());
                cell4.setCellStyle(bodyCellStyle);
                cell4.setCellType(Cell.CELL_TYPE_STRING);
                //收容数
                Cell cell5 = bodyRow.createCell(5);
                cell5.setCellValue(String.valueOf(list.get(i).getOneboxcount()));
                cell5.setCellStyle(bodyCellStyle);
                cell5.setCellType(Cell.CELL_TYPE_STRING);
                //上线收容数
                Cell cell51 = bodyRow.createCell(6);
                cell51.setCellValue(String.valueOf(list.get(i).getBincount()));
                cell51.setCellStyle(bodyCellStyle);
                cell51.setCellType(Cell.CELL_TYPE_STRING);
                //单耗
                Cell cell6 = bodyRow.createCell(7);
                cell6.setCellValue(String.valueOf(list.get(i).getOnecarcount()));
                cell6.setCellStyle(bodyCellStyle);
                cell6.setCellType(Cell.CELL_TYPE_STRING);
                //最大库存
                Cell cell8 = bodyRow.createCell(8);
                cell8.setCellValue(String.valueOf(list.get(i).getMaxstock()));
                cell8.setCellStyle(bodyCellStyle);
                cell8.setCellType(Cell.CELL_TYPE_STRING);
                //拉动库存
                Cell cell7 = bodyRow.createCell(9);
                cell7.setCellValue(String.valueOf(list.get(i).getTriggerstock()));
                cell7.setCellStyle(bodyCellStyle);
                cell7.setCellType(Cell.CELL_TYPE_STRING);
                //配额
                Cell cell9 = bodyRow.createCell(10);
                cell9.setCellValue(String.valueOf(list.get(i).getQuotaratio()));
                cell9.setCellStyle(bodyCellStyle);
                cell9.setCellType(Cell.CELL_TYPE_STRING);
                //箱型
                Cell cell10 = bodyRow.createCell(11);
                cell10.setCellValue(list.get(i).getBoxtype());
                cell10.setCellStyle(bodyCellStyle);
                cell10.setCellType(Cell.CELL_TYPE_STRING);
                //长
                Cell cell11 = bodyRow.createCell(12);
                cell11.setCellValue(String.valueOf(list.get(i).getBoxlength()));
                cell11.setCellStyle(bodyCellStyle);
                cell11.setCellType(Cell.CELL_TYPE_STRING);
                //宽
                Cell cell121 = bodyRow.createCell(13);
                cell121.setCellValue(String.valueOf(list.get(i).getBoxwidth()));
                cell121.setCellStyle(bodyCellStyle);
                cell121.setCellType(Cell.CELL_TYPE_STRING);
                //高
                Cell cell131 = bodyRow.createCell(14);
                cell131.setCellValue(String.valueOf(list.get(i).getBoxheight()));
                cell131.setCellStyle(bodyCellStyle);
                cell131.setCellType(Cell.CELL_TYPE_STRING);
                //上线长
                Cell cell115 = bodyRow.createCell(15);
                cell115.setCellValue(String.valueOf(list.get(i).getPackboxlength()));
                cell115.setCellStyle(bodyCellStyle);
                cell115.setCellType(Cell.CELL_TYPE_STRING);
                //上线宽
                Cell cell161 = bodyRow.createCell(16);
                cell161.setCellValue(String.valueOf(list.get(i).getPackboxwidth()));
                cell161.setCellStyle(bodyCellStyle);
                cell161.setCellType(Cell.CELL_TYPE_STRING);
                //上线高
                Cell cell171 = bodyRow.createCell(17);
                cell171.setCellValue(String.valueOf(list.get(i).getPackboxheight()));
                cell171.setCellStyle(bodyCellStyle);
                cell171.setCellType(Cell.CELL_TYPE_STRING);
                //上线重量
                Cell cell181 = bodyRow.createCell(18);
                cell181.setCellValue(String.valueOf(list.get(i).getPackboxweight()));
                cell181.setCellStyle(bodyCellStyle);
                cell181.setCellType(Cell.CELL_TYPE_STRING);
                //重量
                Cell cell141 = bodyRow.createCell(19);
                cell141.setCellValue(String.valueOf(list.get(i).getBoxweight()));
                cell141.setCellStyle(bodyCellStyle);
                cell141.setCellType(Cell.CELL_TYPE_STRING);
                //返空比例
                Cell cell151 = bodyRow.createCell(20);
                cell151.setCellValue(String.valueOf(list.get(i).getReturnratio()));
                cell151.setCellStyle(bodyCellStyle);
                cell151.setCellType(Cell.CELL_TYPE_STRING);
                //单拖箱数
                Cell cell211 = bodyRow.createCell(21);
                cell211.setCellValue(String.valueOf(list.get(i).getOnetrayboxcount()));
                cell211.setCellStyle(bodyCellStyle);
                cell211.setCellType(Cell.CELL_TYPE_STRING);
                //单拖层数
                Cell cell122 = bodyRow.createCell(22);
                cell122.setCellValue(String.valueOf(list.get(i).getOnetraylayerscount()));
                cell122.setCellStyle(bodyCellStyle);
                cell122.setCellType(Cell.CELL_TYPE_STRING);
                //托盘体积占比
                Cell cell123 = bodyRow.createCell(23);
                cell123.setCellValue(String.valueOf(list.get(i).getTrayratio()));
                cell123.setCellStyle(bodyCellStyle);
                cell123.setCellType(Cell.CELL_TYPE_STRING);
                //托盘长
                Cell cell124 = bodyRow.createCell(24);
                cell124.setCellValue(String.valueOf(list.get(i).getTraylength()));
                cell124.setCellStyle(bodyCellStyle);
                cell124.setCellType(Cell.CELL_TYPE_STRING);
                //托盘体宽
                Cell cell125 = bodyRow.createCell(25);
                cell125.setCellValue(String.valueOf(list.get(i).getTraywidth()));
                cell125.setCellStyle(bodyCellStyle);
                cell125.setCellType(Cell.CELL_TYPE_STRING);
                //托盘高
                Cell cell126 = bodyRow.createCell(26);
                cell126.setCellValue(String.valueOf(list.get(i).getTrayheight()));
                cell126.setCellStyle(bodyCellStyle);
                cell126.setCellType(Cell.CELL_TYPE_STRING);
                //包装描述
                Cell cell127 = bodyRow.createCell(27);
                cell127.setCellValue(list.get(i).getPackremarks());
                cell127.setCellStyle(bodyCellStyle);
                cell127.setCellType(Cell.CELL_TYPE_STRING);
                //接收方
                Cell cell128 = bodyRow.createCell(28);
                cell128.setCellValue(list.get(i).getReceiver());
                cell128.setCellStyle(bodyCellStyle);
                cell128.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = list.get(0).getSupplier().getRoute().getFactory().getFactoryname()+"物料信息.xlsx";
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

    /**
     * 物料批量上传的模板下载
     * @param response
     */
    @Override
    public void modelDownload(HttpServletResponse response) {
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
        goodCodeCell.setCellValue("物料编号");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("物料名称");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("供应商编号");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("供应商名称");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("所属工厂");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("收容数");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(6);
        batchCell.setCellValue("上线收容数");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(7);
        typeCell.setCellValue("单耗");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(8);
        nameCell.setCellValue("最大库存");
        nameCell.setCellStyle(titleCellStyle);
        Cell timeCell=titleRow.createCell(9);
        timeCell.setCellValue("拉动库存");
        timeCell.setCellStyle(titleCellStyle);
        Cell time2Cell=titleRow.createCell(10);
        time2Cell.setCellValue("配额率(%)");
        time2Cell.setCellStyle(titleCellStyle);
        Cell time3Cell=titleRow.createCell(11);
        time3Cell.setCellValue("箱型");
        time3Cell.setCellStyle(titleCellStyle);
        Cell cell12=titleRow.createCell(12);
        cell12.setCellValue("箱长(mm)");
        cell12.setCellStyle(titleCellStyle);
        Cell cell13=titleRow.createCell(13);
        cell13.setCellValue("箱宽(mm)");
        cell13.setCellStyle(titleCellStyle);
        Cell cell14=titleRow.createCell(14);
        cell14.setCellValue("箱高(mm)");
        cell14.setCellStyle(titleCellStyle);
        Cell cell15=titleRow.createCell(15);
        cell15.setCellValue("上线箱长(mm)");
        cell15.setCellStyle(titleCellStyle);
        Cell cell16=titleRow.createCell(16);
        cell16.setCellValue("上线箱宽(mm)");
        cell16.setCellStyle(titleCellStyle);
        Cell cell17=titleRow.createCell(17);
        cell17.setCellValue("上线箱高(mm)");
        cell17.setCellStyle(titleCellStyle);
        Cell cell18=titleRow.createCell(18);
        cell18.setCellValue("上线满箱重量(kg)");
        cell18.setCellStyle(titleCellStyle);
        Cell cell19=titleRow.createCell(19);
        cell19.setCellValue("满箱重量(kg)");
        cell19.setCellStyle(titleCellStyle);
        Cell cell20=titleRow.createCell(20);
        cell20.setCellValue("返空率(%)");
        cell20.setCellStyle(titleCellStyle);
        Cell cell21=titleRow.createCell(21);
        cell21.setCellValue("单托箱数");
        cell21.setCellStyle(titleCellStyle);
        Cell cell22=titleRow.createCell(22);
        cell22.setCellValue("单托层数");
        cell22.setCellStyle(titleCellStyle);
        Cell cell23=titleRow.createCell(23);
        cell23.setCellValue("托盘体积占比(%)");
        cell23.setCellStyle(titleCellStyle);
        Cell cell24=titleRow.createCell(24);
        cell24.setCellValue("托盘长(mm)");
        cell24.setCellStyle(titleCellStyle);
        Cell cell25=titleRow.createCell(25);
        cell25.setCellValue("托盘宽(mm)");
        cell25.setCellStyle(titleCellStyle);
        Cell cell26=titleRow.createCell(26);
        cell26.setCellValue("托盘高(mm)");
        cell26.setCellStyle(titleCellStyle);
        Cell cell27=titleRow.createCell(27);
        cell27.setCellValue("包装描述");
        cell27.setCellStyle(titleCellStyle);
        Cell cell28=titleRow.createCell(28);
        cell28.setCellValue("接收方");
        cell28.setCellStyle(titleCellStyle);

        //创建主体内容的单元格样式和字体
        CellStyle bodyCellStyle = wb.createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//左右居中
        bodyCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//上下居中
        Font bodyFont = wb.createFont();//创建字体
        bodyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);//不加粗
        bodyFont.setFontName("宋体");//设置字体
        bodyFont.setColor(Font.COLOR_RED);//字体颜色为红色
        bodyCellStyle.setFont(bodyFont);//把字体放入单元格样式

        //1.提示行内容
        Row titileRow = sheet.createRow(2);
        Cell titileCell0 = titileRow.createCell(0);
        titileCell0.setCellValue("以下列的值必须从下面的值中选择或按照以下要求填写(所有记录填完后把当前所有的红字内容清除)");
        titileCell0.setCellStyle(bodyCellStyle);
        titileCell0.setCellType(Cell.CELL_TYPE_STRING);
        //2.工厂名称行：
        Row factoryRow = sheet.createRow(3);
        Cell cell0 = factoryRow.createCell(0);
        cell0.setCellValue("所属工厂：");
        cell0.setCellStyle(bodyCellStyle);
        cell0.setCellType(Cell.CELL_TYPE_STRING);
        List<Factory> factoryList=factoryMapper.selectByCondition("","","","","");
        for(int k=0;k<factoryList.size();k++){
            Cell factoryCell= factoryRow.createCell(k+1);
            factoryCell.setCellValue(k+1+"."+factoryList.get(k).getFactoryname());
            factoryCell.setCellStyle(bodyCellStyle);
            factoryCell.setCellType(Cell.CELL_TYPE_STRING);
        }

        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = "物料信息模板.xlsx";
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

    /**
     * 根据工厂id物料名称查询。
     * 计划添加时使用
     * @param factoryId
     * @param goodName
     * @return
     */
    @Override
    public Result goodLikeNameAndFactoryId(int factoryId, String goodName) {
        if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,100}$")){
            return ResultUtil.error(1,"物料名称只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*-)");
        }
        List<Good> goods=goodMapper.selectLikeNameAndFactoryid(goodName,factoryId);
        if(goods.isEmpty()){
            return ResultUtil.success();
        }
        return ResultUtil.success(goods);
    }
}
