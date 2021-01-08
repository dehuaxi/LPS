package com.defei.lps.uploadUtil;

import com.defei.lps.dao.FactoryMapper;
import com.defei.lps.dao.SupplierMapper;
import com.defei.lps.entity.Factory;
import com.defei.lps.entity.Good;
import com.defei.lps.entity.Supplier;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;


/**
 *使用EXCEL进行上传，对每个记录进行验证合法性
 * @author 高德飞
 */
@Component//加上此注解，让spring容器启动时就加载该类
public class GoodExcelUpload {
    @Autowired
    private SupplierMapper supplierMapper;
    @Autowired
    private FactoryMapper factoryMapper;

    //静态初始化本类
    private static GoodExcelUpload goodExcelUpload;
    //在方法上加上注解@PostConstruct,这样方法就会在bean初始化之后被spring容器执行
    @PostConstruct
    public void init(){
        //声明的静态类=this
        goodExcelUpload=this;
    }

    private String message;

    //获取错误信息
    public String getMessage() {
        return message;
    }

    /**
     * 读取EXCEL内容
     *
     * @param file 传入的文件
     * @return 未检查排序单集合
     */
    public List<Good> getList(MultipartFile file) {
    	//定义集合
        List<Good> list = new ArrayList<>();
        //获取文件流
        InputStream is = null;
		try {
            is = file.getInputStream();
            //获取工作簿
            Workbook wb=null;
			//获取文件名
	        String fileName = file.getOriginalFilename();
			if (fileName.matches("^.+\\.(?i)(xls)$")) {
	        	wb = new HSSFWorkbook(is);
	        }else if (fileName.matches("^.+\\.(?i)(xlsx)$")) {
	        	wb = new XSSFWorkbook(is);
	        }else {
                message="不是excel文件";
                list=null;
                return list;
            }

            //得到第一个shell
            Sheet sheet = wb.getSheetAt(0);
            //得到EXCEL数据的总行数
            int totalRows = sheet.getPhysicalNumberOfRows();
            //获取第一行表头
            Row headRow=sheet.getRow(0);
            //从表头中找到需要的参数所在的列下标
            //1.物料编号
            int goodCodeIndex=-1;
            //2.物料名称
            int goodNameIndex=-1;
            //3.供应商编号
            int supplierCodeIndex=-1;
            //4.供应商名称
            int supplierNameIndex=-1;
            //5.所属工厂
            int factoryNameIndex=-1;
            //6.收容数
            int oneBoxCountIndex=-1;
            //上线收容数
            int packOneBoxCountIndex=-1;
            //单耗
            int oneCarCountIndex=-1;
            //最大库存
            int maxStockIndex=-1;
            //拉动库存
            int triggerStockIndex=-1;
            //配额
            int quotaRatioIndex=-1;
            //箱型
            int boxTypeIndex=-1;
            //长
            int boxLengthIndex=-1;
            //宽
            int boxWidthIndex=-1;
            //高
            int boxHeightIndex=-1;
            //上线长
            int packBoxLengthIndex=-1;
            //上线宽
            int packBoxWidthIndex=-1;
            //上线高
            int packBoxHeightIndex=-1;
            //上线重量
            int packBoxWeightIndex=-1;
            //重量
            int boxWeightIndex=-1;
            //返空率
            int returnRatioIndex=-1;
            //单托箱数
            int oneTrayBoxCountIndex=-1;
            //单托层数
            int oneTrayLayersCountIndex=-1;
            //托盘体积占比
            int trayRatioIndex=-1;
            //托盘长
            int trayLengthIndex=-1;
            //托盘宽
            int trayWidthIndex=-1;
            //托盘高
            int trayHeightIndex=-1;
            //包装描述
            int packRemarksIndex=-1;
            //接收方
            int receiverIndex=-1;
            for(int g=0;g<headRow.getPhysicalNumberOfCells();g++){
                if(headRow.getCell(g).getStringCellValue().equals("物料编号")){
                    goodCodeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("物料名称")){
                    goodNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("供应商编号")){
                    supplierCodeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("供应商名称")){
                    supplierNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("所属工厂")){
                    factoryNameIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("收容数")){
                    oneBoxCountIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("上线收容数")){
                    packOneBoxCountIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("单耗")){
                    oneCarCountIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("最大库存")){
                    maxStockIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("拉动库存")){
                    triggerStockIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("配额率(%)")){
                    quotaRatioIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("箱型")){
                    boxTypeIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("箱长(mm)")){
                    boxLengthIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("箱宽(mm)")){
                    boxWidthIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("箱高(mm)")){
                    boxHeightIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("上线箱长(mm)")){
                    packBoxLengthIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("上线箱宽(mm)")){
                    packBoxWidthIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("上线箱高(mm)")){
                    packBoxHeightIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("上线满箱重量(kg)")){
                    packBoxWeightIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("满箱重量(kg)")){
                    boxWeightIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("返空率(%)")){
                    returnRatioIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("单托箱数")){
                    oneTrayBoxCountIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("单托层数")){
                    oneTrayLayersCountIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("托盘体积占比(%)")){
                    trayRatioIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("托盘长(mm)")){
                    trayLengthIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("托盘宽(mm)")){
                    trayWidthIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("托盘高(mm)")){
                    trayHeightIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("包装描述")){
                    packRemarksIndex=g;
                }
                if(headRow.getCell(g).getStringCellValue().equals("接收方")){
                    receiverIndex=g;
                }
            }
            if(goodCodeIndex==-1){
                message="EXCEL中没有“物料编号”列";
                return null;
            }else if(goodNameIndex==-1){
                message="EXCEL中没有“物料名称”列";
                return null;
            }else if(supplierCodeIndex==-1){
                message="EXCEL中没有“供应商编号”列";
                return null;
            }else if(supplierNameIndex==-1){
                message="EXCEL中没有“供应商名称”列";
                return null;
            }else if(factoryNameIndex==-1){
                message="EXCEL中没有“所属工厂”列";
                return null;
            }else if(oneBoxCountIndex==-1){
                message="EXCEL中没有“收容数”列";
                return null;
            }else if(packOneBoxCountIndex==-1){
                message="EXCEL中没有“上线收容数”列";
                return null;
            }else if(oneCarCountIndex==-1){
                message="EXCEL中没有“单耗”列";
                return null;
            }else if(maxStockIndex==-1){
                message="EXCEL中没有“最大库存”列";
                return null;
            }else if(triggerStockIndex==-1){
                message="EXCEL中没有“拉动库存”列";
                return null;
            }else if(quotaRatioIndex==-1){
                message="EXCEL中没有“配额率(%)”列";
                return null;
            }else if(boxTypeIndex==-1){
                message="EXCEL中没有“箱型”列";
                return null;
            }else if(boxLengthIndex==-1){
                message="EXCEL中没有“箱长(mm)”列";
                return null;
            }else if(boxWidthIndex==-1){
                message="EXCEL中没有“箱宽(mm)”列";
                return null;
            }else if(boxHeightIndex==-1){
                message="EXCEL中没有“箱高(mm)”列";
                return null;
            }else if(packBoxLengthIndex==-1){
                message="EXCEL中没有“上线箱长(mm)”列";
                return null;
            }else if(packBoxWidthIndex==-1){
                message="EXCEL中没有“上线箱宽(mm)”列";
                return null;
            }else if(packBoxHeightIndex==-1){
                message="EXCEL中没有“上线箱高(mm)”列";
                return null;
            }else if(packBoxWeightIndex==-1){
                message="EXCEL中没有“上线满箱重量(kg)”列";
                return null;
            }else if(boxWeightIndex==-1){
                message="EXCEL中没有“满箱重量(kg)”列";
                return null;
            }else if(returnRatioIndex==-1){
                message="EXCEL中没有“返空率(%)”列";
                return null;
            }else if(oneTrayBoxCountIndex==-1){
                message="EXCEL中没有“单托箱数”列";
                return null;
            }else if(oneTrayLayersCountIndex==-1){
                message="EXCEL中没有“单托层数”列";
                return null;
            }else if(trayRatioIndex==-1){
                message="EXCEL中没有“托盘体积占比(%)”列";
                return null;
            }else if(trayLengthIndex==-1){
                message="EXCEL中没有“托盘长(mm)”列";
                return null;
            }else if(trayWidthIndex==-1){
                message="EXCEL中没有“托盘宽(mm)”列";
                return null;
            }else if(trayHeightIndex==-1){
                message="EXCEL中没有“托盘高(mm)”列";
                return null;
            }else if(packRemarksIndex==-1){
                message="EXCEL中没有“包装描述”列";
                return null;
            }else if(receiverIndex==-1){
                message="EXCEL中没有“接收方”列";
                return null;
            }
            if(totalRows>=2) {
                String result="";
                //循环表格，每行循环，取出每行中每列的数据，放入物料信息集合
                for (int i = 1; i < totalRows; i++) {
                    //获取行
                    Row row=sheet.getRow(i);
                    //行不为空时才读取该行数据
                    if(row!=null){
                        NumberFormat nf = NumberFormat.getInstance();
                        //1.物料编号
                        String goodCode="";
                        Cell goodCodeCell=row.getCell(goodCodeIndex);
                        if(goodCodeCell!=null) {
                            if (goodCodeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                goodCode = goodCodeCell.getStringCellValue().replace("\\s", "");
                            } else if(goodCodeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){
                                goodCode="";
                            }else if(goodCodeCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                goodCode=nf.format(goodCodeCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“物料编号”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //2.物料名称
                        String goodName="";
                        Cell goodNameCell=row.getCell(goodNameIndex);
                        if(goodNameCell!=null) {
                            if (goodNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                goodName = goodNameCell.getStringCellValue().replace(" ", "");
                            } else if(goodNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(goodNameCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                goodName=nf.format(goodNameCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“物料名称”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //3.供应商编号
                        String supplierCode="";
                        Cell supplierCodeCell=row.getCell(supplierCodeIndex);
                        if(supplierCodeCell!=null) {
                            if (supplierCodeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                supplierCode = supplierCodeCell.getStringCellValue().replace("\\s", "");
                            } else if(supplierCodeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(supplierCodeCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                supplierCode=nf.format(supplierCodeCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“供应商编号”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //4.供应商名称
                        String supplierName="";
                        Cell supplierNameCell=row.getCell(supplierNameIndex);
                        if(supplierNameCell!=null) {
                            if (supplierNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                supplierName = supplierNameCell.getStringCellValue().replace(" ", "");
                            } else if(supplierNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(supplierNameCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                supplierName=nf.format(supplierNameCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“供应商名称”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //5.所属工厂
                        String factoryName="";
                        Cell factoryNameCell=row.getCell(factoryNameIndex);
                        if(factoryNameCell!=null) {
                            if (factoryNameCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                factoryName = factoryNameCell.getStringCellValue().replace(" ", "");
                            } else if(factoryNameCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(factoryNameCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                factoryName=nf.format(factoryNameCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“所属工厂”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //6.收容数
                        String oneBoxCount="";
                        Cell oneBoxCountCell=row.getCell(oneBoxCountIndex);
                        if(oneBoxCountCell!=null) {
                            if (oneBoxCountCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                oneBoxCount = oneBoxCountCell.getStringCellValue().replace(" ", "");
                            } else if(oneBoxCountCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(oneBoxCountCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                oneBoxCount=nf.format(oneBoxCountCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“收容数”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //7.上线收容数
                        String packOneBoxCount="";
                        Cell packOneBoxCountCell=row.getCell(packOneBoxCountIndex);
                        if(packOneBoxCountCell!=null) {
                            if (packOneBoxCountCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                packOneBoxCount = packOneBoxCountCell.getStringCellValue().replace(" ", "");
                            } else if(packOneBoxCountCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(packOneBoxCountCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                packOneBoxCount=nf.format(packOneBoxCountCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“上线收容数”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //8.单耗
                        String oneCarCount="";
                        Cell oneCarCountCell=row.getCell(oneCarCountIndex);
                        if(oneCarCountCell!=null) {
                            if (oneCarCountCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                oneCarCount = oneCarCountCell.getStringCellValue().replace(" ", "");
                            } else if(oneCarCountCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                oneCarCount=nf.format(oneCarCountCell.getNumericCellValue()).replace(",","");
                            }else if(oneCarCountCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else {
                                result+=";第"+(i+1)+"行，“单耗”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //9.最大库存
                        String maxStock="";
                        Cell maxStockCell=row.getCell(maxStockIndex);
                        if(maxStockCell!=null) {
                            if (maxStockCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                maxStock = maxStockCell.getStringCellValue().replace(" ", "");
                            } else if(maxStockCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(maxStockCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                maxStock=nf.format(maxStockCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“最大库存”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //10.拉动库存
                        String triggerStock="";
                        Cell triggerStockCell=row.getCell(triggerStockIndex);
                        if(triggerStockCell!=null) {
                            if (triggerStockCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                triggerStock = triggerStockCell.getStringCellValue().replace(" ", "");
                            } else if(triggerStockCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(triggerStockCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                triggerStock=nf.format(triggerStockCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“拉动库存”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //11.配额
                        String quotaRatio="";
                        Cell quotaRatioCell=row.getCell(quotaRatioIndex);
                        if(quotaRatioCell!=null) {
                            if (quotaRatioCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                quotaRatio = quotaRatioCell.getStringCellValue().replace(" ", "");
                            } else if(quotaRatioCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(quotaRatioCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                quotaRatio=nf.format(quotaRatioCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“配额率(%)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //12.箱型
                        String boxType="";
                        Cell boxTypeCell=row.getCell(boxTypeIndex);
                        if(boxTypeCell!=null) {
                            if (boxTypeCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                boxType = boxTypeCell.getStringCellValue().replace(" ", "");
                            } else if(boxTypeCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(boxTypeCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                boxType=nf.format(boxTypeCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“箱型”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //13.长
                        String boxLength="";
                        Cell boxLengthCell=row.getCell(boxLengthIndex);
                        if(boxLengthCell!=null) {
                            if (boxLengthCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                boxLength = boxLengthCell.getStringCellValue().replace(" ", "");
                            } else if(boxLengthCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(boxLengthCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                boxLength=nf.format(boxLengthCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“箱长(mm)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //14.宽
                        String boxWidth="";
                        Cell boxWidthCell=row.getCell(boxWidthIndex);
                        if(boxWidthCell!=null) {
                            if (boxWidthCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                boxWidth = boxWidthCell.getStringCellValue().replace(" ", "");
                            } else if(boxWidthCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(boxWidthCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                boxWidth=nf.format(boxWidthCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“箱宽(mm)”必须为文本格式";
                                continue;
                            }
                        }

                        //15.高
                        String boxHeight="";
                        Cell boxHeightCell=row.getCell(boxHeightIndex);
                        if(boxHeightCell!=null) {
                            if (boxHeightCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                boxHeight = boxHeightCell.getStringCellValue().replace(" ", "");
                            } else if(boxHeightCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(boxHeightCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                boxHeight=nf.format(boxHeightCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“箱高(mm)”必须为文本格式";
                                continue;
                            }
                        }
                        //16.上线长
                        String packBoxLength="";
                        Cell packBoxLengthCell=row.getCell(packBoxLengthIndex);
                        if(packBoxLengthCell!=null) {
                            if (packBoxLengthCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                packBoxLength = packBoxLengthCell.getStringCellValue().replace(" ", "");
                            } else if(packBoxLengthCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(packBoxLengthCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                packBoxLength=nf.format(packBoxLengthCell.getNumericCellValue()).replace(",","");
                            }else {
                                result+=";第"+(i+1)+"行，“上线箱长(mm)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //17.上线宽
                        String packBoxWidth="";
                        Cell packBoxWidthCell=row.getCell(packBoxWidthIndex);
                        if(packBoxWidthCell!=null) {
                            if (packBoxWidthCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                packBoxWidth = packBoxWidthCell.getStringCellValue().replace(" ", "");
                            } else if(packBoxWidthCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(packBoxWidthCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                packBoxWidth=nf.format(packBoxWidthCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“上线箱宽(mm)”必须为文本或常规或者数字格式";
                                continue;
                            }
                        }

                        //18.上线高
                        String packBoxHeight="";
                        Cell packBoxHeightCell=row.getCell(packBoxHeightIndex);
                        if(packBoxHeightCell!=null) {
                            if (packBoxHeightCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                packBoxHeight = packBoxHeightCell.getStringCellValue().replace(" ", "");
                            } else if(packBoxHeightCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(packBoxHeightCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                packBoxHeight=nf.format(packBoxHeightCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“上线箱高(mm)”必须为文本或常规或者数字格式";
                                continue;
                            }
                        }

                        //19.上线重量
                        String packBoxWeight="";
                        Cell packBoxWeightCell=row.getCell(packBoxWeightIndex);
                        if(packBoxWeightCell!=null) {
                            if (packBoxWeightCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                packBoxWeight = packBoxWeightCell.getStringCellValue().replace(" ", "");
                            } else if(packBoxWeightCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else  {
                                result+=";第"+(i+1)+"行，“上线满箱重量(kg)”必须为文本格式";
                                continue;
                            }
                        }

                        //20.重量
                        String boxWeight="";
                        Cell boxWeightCell=row.getCell(boxWeightIndex);
                        if(boxWeightCell!=null) {
                            if (boxWeightCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                boxWeight = boxWeightCell.getStringCellValue().replace(" ", "");
                            } else if(boxWeightCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else  {
                                result+=";第"+(i+1)+"行，“满箱重量(kg)”必须为文本格式";
                                continue;
                            }
                        }

                        //21.返空率(%)
                        String returnRatio="";
                        Cell returnRatioCell=row.getCell(returnRatioIndex);
                        if(returnRatioCell!=null) {
                            if (returnRatioCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                returnRatio = returnRatioCell.getStringCellValue().replace(" ", "");
                            } else if(returnRatioCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(returnRatioCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                returnRatio=nf.format(returnRatioCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“返空率(%)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //22.单托箱数
                        String oneTrayBoxCount="";
                        Cell oneTrayBoxCountCell=row.getCell(oneTrayBoxCountIndex);
                        if(oneTrayBoxCountCell!=null) {
                            if (oneTrayBoxCountCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                oneTrayBoxCount = oneTrayBoxCountCell.getStringCellValue().replace(" ", "");
                            } else if(oneTrayBoxCountCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(oneTrayBoxCountCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                oneTrayBoxCount=nf.format(oneTrayBoxCountCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“单托箱数”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //23单托层数
                        String oneTrayLayersCount="";
                        Cell oneTrayLayersCountCell=row.getCell(oneTrayLayersCountIndex);
                        if(oneTrayLayersCountCell!=null) {
                            if (oneTrayLayersCountCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                oneTrayLayersCount = oneTrayLayersCountCell.getStringCellValue().replace(" ", "");
                            } else if(oneTrayLayersCountCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(oneTrayLayersCountCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                oneTrayLayersCount=nf.format(oneTrayLayersCountCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“单托层数”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //24.托盘体积占比(%)
                        String trayRatio="";
                        Cell trayRatioCell=row.getCell(trayRatioIndex);
                        if(trayRatioCell!=null) {
                            if (trayRatioCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                trayRatio = trayRatioCell.getStringCellValue().replace(" ", "");
                            } else if(trayRatioCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(trayRatioCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                trayRatio=nf.format(trayRatioCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“托盘体积占比(%)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //25.托盘长
                        String trayLength="";
                        Cell trayLengthCell=row.getCell(trayLengthIndex);
                        if(trayLengthCell!=null) {
                            if (trayLengthCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                trayLength = trayLengthCell.getStringCellValue().replace(" ", "");
                            } else if(trayLengthCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(trayLengthCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                trayLength=nf.format(trayLengthCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“托盘长(mm)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //26.托盘宽
                        String trayWidth="";
                        Cell trayWidthCell=row.getCell(trayWidthIndex);
                        if(trayWidthCell!=null) {
                            if (trayWidthCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                trayWidth = trayWidthCell.getStringCellValue().replace(" ", "");
                            } else if(trayWidthCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(trayWidthCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                trayWidth=nf.format(trayWidthCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“托盘宽(mm)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //27托盘高
                        String trayHeight="";
                        Cell trayHeightCell=row.getCell(trayHeightIndex);
                        if(trayHeightCell!=null) {
                            if (trayHeightCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                trayHeight = trayHeightCell.getStringCellValue().replace(" ", "");
                            } else if(trayHeightCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(trayHeightCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                trayHeight=nf.format(trayHeightCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“托盘体积占比(%)”必须为文本或常规或数字格式";
                                continue;
                            }
                        }

                        //28.包装描述
                        String packRemarks="";
                        Cell packRemarksCell=row.getCell(packRemarksIndex);
                        if(packRemarksCell!=null) {
                            if (packRemarksCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                packRemarks = packRemarksCell.getStringCellValue().replace(" ", "");
                            } else if(packRemarksCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(packRemarksCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                packRemarks=nf.format(packRemarksCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“包装描述”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //29.接收方
                        String receiver="";
                        Cell receiverCell=row.getCell(receiverIndex);
                        if(receiverCell!=null) {
                            if (receiverCell.getCellType() == HSSFCell.CELL_TYPE_STRING) {
                                //获取单元格内容,并去掉空格
                                receiver = receiverCell.getStringCellValue().replace(" ", "");
                            } else if(receiverCell.getCellType()==HSSFCell.CELL_TYPE_BLANK){

                            }else if(receiverCell.getCellType()==HSSFCell.CELL_TYPE_NUMERIC){
                                receiver=nf.format(receiverCell.getNumericCellValue()).replace(",","");
                            }else  {
                                result+=";第"+(i+1)+"行，“接收方”必须为文本或常规格式";
                                continue;
                            }
                        }

                        //只有当整行所有数据都为空就跳过
                        if(supplierCode.equals("")&&
                                supplierName.equals("")&&
                                supplierCode.equals("")&&
                                goodCode.equals("")&&
                                goodName.equals("")&&
                                factoryName.equals("")&&
                                oneBoxCount.equals("")&&
                                packOneBoxCount.equals("")&&
                                oneCarCount.equals("")&&
                                maxStock.equals("")&&
                                triggerStock.equals("")&&
                                quotaRatio.equals("")&&
                                boxType.equals("")&&
                                boxLength.equals("")&&
                                boxWidth.equals("")&&
                                boxHeight.equals("")&&
                                packBoxLength.equals("")&&
                                packBoxWidth.equals("")&&
                                packBoxHeight.equals("")&&
                                packBoxWeight.equals("")&&
                                boxWeight.equals("")&&
                                returnRatio.equals("")&&
                                oneTrayBoxCount.equals("")&&
                                oneTrayLayersCount.equals("")&&
                                trayRatio.equals("")&&
                                trayLength.equals("")&&
                                trayWidth.equals("")&&
                                trayHeight.equals("")&&
                                packRemarks.equals("")&&
                                receiver.equals("")){
                            continue;
                        }else {
                            if(!goodCode.matches("^[0-9A-Za-z#-]{1,30}$")){
                                result+=";第"+(i+1)+"行，“物料编号”只能是1-30位的数字、大小写字母、特殊字符(#-)";
                                continue;
                            }
                            if(!goodName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*()。.（）-]{0,100}$")){
                                result+=";第"+(i+1)+"行，“物料名称”只能是1-100位的数字、大小写字母、汉字、特殊字符(@#_*()。.（）-)";
                                continue;
                            }
                            if(!supplierCode.matches("^[0-9A-Z]{1,6}$")){
                                result+=";第"+(i+1)+"行，“供应商编号”必须为1-6位的数字、大写字母";
                                continue;
                            }
                            if(!supplierName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_()。.（）*-]{1,50}$")){
                                result+=";第"+(i+1)+"行，“供应商名称”只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_()。.（）*-)";
                                continue;
                            }
                            Factory factory=goodExcelUpload.factoryMapper.selectByFactoryname(factoryName);
                            if(factory==null){
                                result+=";第"+(i+1)+"行，“所属工厂”的值在系统中找不到";
                                continue;
                            }
                            //供应商是否存在,只有在工厂存在的前提下才检查
                            Supplier supplier=goodExcelUpload.supplierMapper.selectBySuppliercodeAndFactoryid(supplierCode,factory.getId());
                            if(supplier==null){
                                result+=";第"+(i+1)+"行，工厂:"+factoryName+"中找不到供应商:"+supplierName;
                                continue;
                            }
                            if(!oneBoxCount.matches("^[1-9]{1}[0-9]{0,10}$")){
                                result+=";第"+(i+1)+"行，“收容数”必须为正整数";
                                continue;
                            }
                            if(!packOneBoxCount.matches("^[0-9]{1,11}$")){
                                result+=";第"+(i+1)+"行，“上线收容数”必须为0或正整数";
                                continue;
                            }
                            if(!oneCarCount.matches("^[1-9]{1}[0-9]{0,10}$")){
                                result+=";第"+(i+1)+"行，“单耗”必须为正整数";
                                continue;
                            }
                            if(!maxStock.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“最大库存”必须为0或正整数";
                                continue;
                            }
                            if(!triggerStock.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“拉动库存”必须为0或正整数";
                                continue;
                            }
                            if(!quotaRatio.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“配额率(%)”必须为0-100的数字";
                                continue;
                            }else {
                                if(Integer.parseInt(quotaRatio)<0||Integer.parseInt(quotaRatio)>100){
                                    result+=";第"+(i+1)+"行，“配额率(%)”必须为0-100的数字";
                                    continue;
                                }
                            }
                            if(!boxType.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_*-]{0,10}$")){
                                result+=";第"+(i+1)+"行，“箱型”必须为1-10位的数字、大小写字母、汉字、特殊字符(@#_*-)";
                                continue;
                            }
                            if(!boxLength.matches("^[1-9]{1}[0-9]{0,10}$")){
                                result+=";第"+(i+1)+"行，“箱长(mm)”必须为正整数";
                                continue;
                            }
                            if(!boxWidth.matches("^[1-9]{1}[0-9]{0,10}$")){
                                result+=";第"+(i+1)+"行，“箱宽(mm)”必须为正整数";
                                continue;
                            }
                            if(!boxHeight.matches("^[1-9]{1}[0-9]{0,10}$")){
                                result+=";第"+(i+1)+"行，“箱高(mm)”必须为正整数";
                                continue;
                            }
                            if(!packBoxLength.matches("^[0-9]{1,11}$")){
                                result+=";第"+(i+1)+"行，“上线箱长(mm)”必须为大于等于0的整数";
                                continue;
                            }
                            if(!packBoxWidth.matches("^[0-9]{1,11}$")){
                                result+=";第"+(i+1)+"行，“上线箱宽(mm)”必须为大于等于0的整数";
                                continue;
                            }
                            if(!packBoxHeight.matches("^[0-9]{1,11}$")){
                                result+=";第"+(i+1)+"行，“上线箱高(mm)”必须为大于等于0的整数";
                                continue;
                            }
                            if(!packBoxWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
                                result+=";第"+(i+1)+"行，“上线满箱重量(kg)”必须是大于等于0的最多保留2位小数的数字";
                                continue;
                            }
                            if(!boxWeight.matches("^(([1-9]{1}[0-9]{0,6})|([0]{1}))([.]\\d{1,2})?$")){
                                result+=";第"+(i+1)+"行，“满箱重量(kg)”必须是大于0的最多保留2位小数的数字";
                                continue;
                            }else {
                                if(new BigDecimal(boxWeight).compareTo(BigDecimal.ZERO)<1){
                                    result+=";第"+(i+1)+"行，“满箱重量(kg)”必须是大于0的最多保留2位小数的数字";
                                    continue;
                                }
                            }
                            if(!returnRatio.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“返空率(%)”必须为0-100的数字";
                                continue;
                            }else {
                                if(Integer.parseInt(returnRatio)<0||Integer.parseInt(returnRatio)>100){
                                    result+=";第"+(i+1)+"行，“返空率(%)”必须为0-100的数字";
                                    continue;
                                }
                            }
                            if(!oneTrayBoxCount.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“单托箱数”必须为大于等于0的整数";
                                continue;
                            }
                            if(!oneTrayLayersCount.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“单托层数”必须为大于等于0的整数";
                                continue;
                            }
                            if(!trayRatio.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“托盘体积占比(%)”必须为0-100的数字";
                                continue;
                            }else {
                                if(Integer.parseInt(trayRatio)<0||Integer.parseInt(trayRatio)>100){
                                    result+=";第"+(i+1)+"行，“托盘体积占比(%)”必须为0-100的数字";
                                    continue;
                                }
                            }
                            if(!trayLength.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“托盘长(mm)”必须为大于等于0的整数";
                                continue;
                            }
                            if(!trayWidth.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“托盘长(mm)”必须为大于等于0的整数";
                                continue;
                            }
                            if(!trayHeight.matches("^(0|[1-9]{1}[0-9]{0,10})$")){
                                result+=";第"+(i+1)+"行，“托盘长(mm)”必须为大于等于0的整数";
                                continue;
                            }
                            if(new BigDecimal(oneTrayBoxCount).compareTo(BigDecimal.ZERO)==0){
                                //如果单托箱数=0，说明是非托盘件，那么托盘体积必须为0
                                if(new BigDecimal(trayRatio).compareTo(BigDecimal.ZERO)!=0){
                                    result+=";第"+(i+1)+"行，非托盘物料的“托盘体积占比(%)”必须为0";
                                    continue;
                                }
                                if(new BigDecimal(oneTrayLayersCount).compareTo(BigDecimal.ZERO)!=0){
                                    result+=";第"+(i+1)+"行，非托盘物料的“托盘层数”必须为0";
                                    continue;
                                }
                                if(new BigDecimal(trayLength).compareTo(BigDecimal.ZERO)!=0){
                                    result+=";第"+(i+1)+"行，非托盘物料的“托盘长(mm)”必须为0";
                                    continue;
                                }
                                if(new BigDecimal(trayWidth).compareTo(BigDecimal.ZERO)!=0){
                                    result+=";第"+(i+1)+"行，非托盘物料的“托盘宽(mm)”必须为0";
                                    continue;
                                }
                                if(new BigDecimal(trayHeight).compareTo(BigDecimal.ZERO)!=0){
                                    result+=";第"+(i+1)+"行，非托盘物料的“托盘高(mm)”必须为0";
                                    continue;
                                }
                            }else {
                                //如果单托箱数！=0，说明是托盘件，那么就需要填入非0 的托盘体积占比
                                if(new BigDecimal(trayRatio).compareTo(BigDecimal.ZERO)==0){
                                    result+=";第"+(i+1)+"行，托盘物料的“托盘体积占比(%)”不能为0";
                                    continue;
                                }
                                if(new BigDecimal(oneTrayLayersCount).compareTo(BigDecimal.ZERO)==0){
                                    result+=";第"+(i+1)+"行，托盘物料的“托盘层数”不能为0";
                                    continue;
                                }
                                if(new BigDecimal(trayLength).compareTo(BigDecimal.ZERO)==0){
                                    result+=";第"+(i+1)+"行，托盘物料的“托盘长(mm)”不能为0";
                                    continue;
                                }
                                if(new BigDecimal(trayWidth).compareTo(BigDecimal.ZERO)==0){
                                    result+=";第"+(i+1)+"行，托盘物料的“托盘宽(mm)”不能为0";
                                    continue;
                                }
                                if(new BigDecimal(trayHeight).compareTo(BigDecimal.ZERO)==0){
                                    result+=";第"+(i+1)+"行，托盘物料的“托盘高(mm)”不能为0";
                                    continue;
                                }
                            }
                            if(!packRemarks.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,:：，。.*-]{0,50}$")){
                                result+=";第"+(i+1)+"行，“包装描述”只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_,:：，。.*-)";
                                continue;
                            }
                            if(!receiver.matches("^[0-9A-Za-z\\u4e00-\\u9fa5#@_,:：，。.*-]{0,20}$")){
                                result+=";第"+(i+1)+"行，“接收方”只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_,:：，。.*-)";
                                continue;
                            }
                            //所有都有值，则放入集合
                            Good good=new Good();
                            good.setGoodname(goodName);
                            good.setGoodcode(goodCode);
                            good.setSupplier(supplier);
                            good.setOneboxcount(Integer.parseInt(oneBoxCount));
                            good.setBincount(Integer.parseInt(packOneBoxCount));
                            good.setOnecarcount(Integer.parseInt(oneCarCount));
                            good.setMaxstock(Integer.parseInt(maxStock));
                            good.setTriggerstock(Integer.parseInt(triggerStock));
                            good.setQuotaratio(Integer.parseInt(quotaRatio));
                            good.setBoxtype(boxType);
                            good.setBoxlength(Integer.parseInt(boxLength));
                            good.setBoxwidth(Integer.parseInt(boxWidth));
                            good.setBoxheight(Integer.parseInt(boxHeight));
                            good.setPackboxlength(Integer.parseInt(packBoxLength));
                            good.setPackboxwidth(Integer.parseInt(packBoxWidth));
                            good.setPackboxheight(Integer.parseInt(packBoxHeight));
                            good.setPackboxweight(new BigDecimal(packBoxWeight));
                            good.setBoxweight(new BigDecimal(boxWeight));
                            good.setReturnratio(Integer.parseInt(returnRatio));
                            good.setOnetrayboxcount(Integer.parseInt(oneTrayBoxCount));
                            good.setOnetraylayerscount(Integer.parseInt(oneTrayLayersCount));
                            good.setTrayratio(Integer.parseInt(trayRatio));
                            good.setTraylength(Integer.parseInt(trayLength));
                            good.setTraywidth(Integer.parseInt(trayWidth));
                            good.setTrayheight(Integer.parseInt(trayHeight));
                            good.setPackremarks(packRemarks);
                            good.setReceiver(receiver);
                            list.add(good);
                        }
                    }//行不为空判断
                }//行循环结束
                if(!result.equals("")) {
                    message=result.substring(1);
                    return null;
                }else {
                    //没有提示情况下，如果集合仍为空，那么说明没有数据
                    if(list.isEmpty()) {
                        message="EXCEL中没有需要的数据";
                        return null;
                    }
                }
            }else {
                list=null;
                message="EXCEL中没有数据";
            }
            //返回供应商对象集合
            return list;
		} catch (Exception e) {
			e.printStackTrace();
			message="文件读取错误";
			list=null;
			return list;
		}finally {
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="关闭文件流错误";
                    list=null;
                    return list;
                }
            }
        }
    }
}
