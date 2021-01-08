package com.defei.lps.serviceImp;

import com.defei.lps.dao.GeelyBillCacheMapper;
import com.defei.lps.dao.GeelyBillRecordMapper;
import com.defei.lps.dao.PlanCacheMapper;
import com.defei.lps.dao.PlanRecordMapper;
import com.defei.lps.entity.GeelyBillCache;
import com.defei.lps.entity.GeelyBillRecord;
import com.defei.lps.entity.PlanCache;
import com.defei.lps.entity.PlanRecorde;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.GeelyBillRecordService;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class GeelyGeelyBillRecordServiceImp implements GeelyBillRecordService {
    @Autowired
    private GeelyBillRecordMapper geelyBillRecordMapper;
    @Autowired
    private GeelyBillCacheMapper geelyBillCacheMapper;
    @Autowired
    private PlanCacheMapper planCacheMapper;
    @Autowired
    private PlanRecordMapper planRecordMapper;

    /**
     * 条件分页查询PD单完成记录
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param billNumber PD单单号
     * @param batch
     * @param needBind
     * @param bindBillNumber
     * @param uploadDate PD单上传时间
     * @param receiveDateStart 回执时间开始
     * @param receiveDateEnd 回执时间结束
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber,  String batch,String needBind,String bindBillNumber, String uploadDate, String receiveDateStart,String receiveDateEnd,int currentPage) {
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
            return ResultUtil.error(1,"吉利单号必须为1-20位的数字、大写字母");
        }else if(!bindBillNumber.matches("^[0-9A-Z]{0,20}$")){
            return ResultUtil.error(1,"补充吉利单号必须为1-20位的数字、大写字母");
        }else if(!batch.matches("^[0-9A-Z]{0,10}$")){
            return ResultUtil.error(1,"批次必须为1-10位的数字、大写字母");
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<GeelyBillRecord> list= geelyBillRecordMapper.selectLimitByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,batch,needBind,bindBillNumber,uploadDate,receiveDateStart,receiveDateEnd,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount= geelyBillRecordMapper.selectCountByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,batch,needBind,bindBillNumber,uploadDate,receiveDateStart,receiveDateEnd);
            int totalPage=0;
            if(totalCount%30==0) {
                totalPage=totalCount/30;
            }else {
                totalPage=totalCount/30+1;
            }
            //查询需要绑定的记录数量
            int needBindCount=geelyBillRecordMapper.selectCountNeedBind();
            //集合内容
            Map map=new HashMap();
            map.put("currentPage",currentPage);
            map.put("totalPage",totalPage);
            map.put("totalCount",totalCount);
            map.put("list",list);
            map.put("needBindCount",needBindCount);
            return ResultUtil.success(map);
        }
        return ResultUtil.success();
    }

    /**
     * 下载
     * @param goodCode
     * @param goodName
     * @param supplierCode
     * @param supplierName
     * @param billNumber
     * @param batch
     * @param needBind
     * @param bindBillNumber
     * @param uploadDate
     * @param receiveDateStart
     * @param receiveDateEnd
     * @param response
     */
    @Override
    public void geelyBillRecordDownload(String goodCode, String goodName, String supplierCode, String supplierName, String billNumber,  String batch,String needBind,String bindBillNumber,String uploadDate, String receiveDateStart, String receiveDateEnd, HttpServletResponse response) {
        //获取结果
        List<GeelyBillRecord> list= geelyBillRecordMapper.selectByCondition(goodCode,goodName,supplierCode,supplierName ,billNumber,batch,needBind,bindBillNumber,uploadDate,receiveDateStart,receiveDateEnd);
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"已回执吉利单据");
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
        goodCodeCell.setCellValue("吉利单号");
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
        countCell.setCellValue("单据数量");
        countCell.setCellStyle(titleCellStyle);
        Cell batchCell = titleRow.createCell(6);
        batchCell.setCellValue("实收数量");
        batchCell.setCellStyle(titleCellStyle);
        Cell typeCell=titleRow.createCell(7);
        typeCell.setCellValue("批次");
        typeCell.setCellStyle(titleCellStyle);
        Cell nameCell = titleRow.createCell(8);
        nameCell.setCellValue("是否加急");
        nameCell.setCellStyle(titleCellStyle);
        Cell timeCell=titleRow.createCell(9);
        timeCell.setCellValue("是否绑定补充单");
        timeCell.setCellStyle(titleCellStyle);
        Cell time2Cell=titleRow.createCell(10);
        time2Cell.setCellValue("补充单号");
        time2Cell.setCellStyle(titleCellStyle);
        Cell time3Cell=titleRow.createCell(11);
        time3Cell.setCellValue("上传时间");
        time3Cell.setCellStyle(titleCellStyle);
        Cell cell12=titleRow.createCell(12);
        cell12.setCellValue("回执时间");
        cell12.setCellStyle(titleCellStyle);
        Cell cell13=titleRow.createCell(13);
        cell13.setCellValue("备注");
        cell13.setCellStyle(titleCellStyle);

        //创建主体内容的单元格样式和字体
        CellStyle bodyCellStyle = wb.createCellStyle();
        bodyCellStyle.setAlignment(CellStyle.ALIGN_CENTER);//左右居中
        bodyCellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);//上下居中
        Font bodyFont = wb.createFont();//创建字体
        bodyFont.setBoldweight(Font.BOLDWEIGHT_NORMAL);//不加粗
        bodyFont.setFontName("宋体");//设置字体
        bodyCellStyle.setFont(bodyFont);//把字体放入单元格样式
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        //循环添加内容
        if (!list.isEmpty()) {
            for (int i = 0; i < list.size(); i++) {
                //每循环一次，创建一行。由于第一行是标题行，所以行下标从1开始
                Row bodyRow = sheet.createRow(i + 1);
                //给每行创建列，第一列下标为0,并给每列添加内容
                //PD单号
                Cell cell0 = bodyRow.createCell(0);
                cell0.setCellValue(list.get(i).getBillnumber());
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
                //PD单数量
                Cell cell6 = bodyRow.createCell(5);
                cell6.setCellValue(String.valueOf(list.get(i).getCount()));
                cell6.setCellStyle(bodyCellStyle);
                cell6.setCellType(Cell.CELL_TYPE_STRING);
                //实收数量
                Cell cell8 = bodyRow.createCell(6);
                cell8.setCellValue(String.valueOf(list.get(i).getReceivecount()));
                cell8.setCellStyle(bodyCellStyle);
                cell8.setCellType(Cell.CELL_TYPE_STRING);
                //批次
                Cell cell9 = bodyRow.createCell(7);
                cell9.setCellValue(list.get(i).getBatch());
                cell9.setCellStyle(bodyCellStyle);
                cell9.setCellType(Cell.CELL_TYPE_STRING);
                //加急
                Cell cell11 = bodyRow.createCell(8);
                cell11.setCellValue(list.get(i).getUrgent());
                cell11.setCellStyle(bodyCellStyle);
                cell11.setCellType(Cell.CELL_TYPE_STRING);
                //是否绑定补充单
                Cell cell121 = bodyRow.createCell(9);
                cell121.setCellValue(list.get(i).getNeedbind());
                cell121.setCellStyle(bodyCellStyle);
                cell121.setCellType(Cell.CELL_TYPE_STRING);
                //补充单
                Cell cell131 = bodyRow.createCell(10);
                cell131.setCellValue(list.get(i).getNeedbind());
                cell131.setCellStyle(bodyCellStyle);
                cell131.setCellType(Cell.CELL_TYPE_STRING);
                //上传时间
                Cell cell141 = bodyRow.createCell(11);
                cell141.setCellValue(list.get(i).getUploadtime());
                cell141.setCellStyle(bodyCellStyle);
                cell141.setCellType(Cell.CELL_TYPE_STRING);
                //完成时间
                Cell cell151 = bodyRow.createCell(12);
                cell151.setCellValue(simpleDateFormat.format(list.get(i).getReceivetime()));
                cell151.setCellStyle(bodyCellStyle);
                cell151.setCellType(Cell.CELL_TYPE_STRING);
                //备注
                Cell cell7 = bodyRow.createCell(13);
                cell7.setCellValue(list.get(i).getRemarks());
                cell7.setCellStyle(bodyCellStyle);
                cell7.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = receiveDateStart+"至"+receiveDateEnd+"已回执吉利单据.xlsx";
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
     * 把多送的吉利单号补的吉利单绑定到实收数>单据数的吉利单据记录上
     * @param id
     * @param billNumber
     * @return
     */
    @Override
    public Result geelyBillRecordBind(int id, String billNumber) {
        GeelyBillRecord geelyBillRecord=geelyBillRecordMapper.selectByPrimaryKey(id);
        if(geelyBillRecord==null){
            return ResultUtil.error(1,"所选的已回执吉利单据记录不存在");
        }
        geelyBillRecord.setNeedbind("否");
        geelyBillRecord.setBindbillnumber(billNumber);
        geelyBillRecordMapper.updateByPrimaryKeySelective(geelyBillRecord);
        //计算补的数量
        int newCount=geelyBillRecord.getReceivecount()-geelyBillRecord.getCount();
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date now=new Date();
        //添加绑定的吉利单据记录
        GeelyBillRecord geelyBillRecord1=new GeelyBillRecord();
        geelyBillRecord1.setBillnumber(billNumber);
        geelyBillRecord1.setGood(geelyBillRecord.getGood());
        geelyBillRecord1.setCount(newCount);
        geelyBillRecord1.setReceivecount(0);
        geelyBillRecord1.setBatch(geelyBillRecord.getBatch());
        geelyBillRecord1.setUrgent("否");
        geelyBillRecord1.setUploadtime(simpleDateFormat.format(now));
        geelyBillRecord1.setReceivetime(now);
        geelyBillRecord1.setNeedbind("否");
        geelyBillRecord1.setBindbillnumber("");
        geelyBillRecord1.setRemarks(geelyBillRecord.getBillnumber()+"的补充单，补充单实收数为0");
        geelyBillRecordMapper.insertSelective(geelyBillRecord1);
        return ResultUtil.success("绑定成功！");
    }

}
