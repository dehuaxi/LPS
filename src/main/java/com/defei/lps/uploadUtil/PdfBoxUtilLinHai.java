package com.defei.lps.uploadUtil;


import com.defei.lps.dao.GeelyBillCacheMapper;
import com.defei.lps.dao.GeelyBillRecordMapper;
import com.defei.lps.dao.GoodMapper;
import com.defei.lps.entity.*;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * PDF文件解析类。按照PDF文件内容格式，解析出我想要的内容。
 *
 * @author Administrator
 */
@Component//次注解让spring容器扫描到
public class PdfBoxUtilLinHai {
    @Autowired
    private GoodMapper goodMapper;
    @Autowired
    private GeelyBillCacheMapper geelyBillCacheMapper;
    @Autowired
    private GeelyBillRecordMapper geelyBillRecordMapper;

    //private final static Logger logger = LoggerFactory.getLogger(PdfBoxUtilLinHai.class);

    //静态初始化当前类
    private static PdfBoxUtilLinHai pdfBoxUtil;
    //在方法上加上注解@PostConstruct,这样方法就会在bean初始化之后被spring容器执行
    @PostConstruct
    public void init(){
        //声明的静态类=this
        pdfBoxUtil=this;
    }

    private static String message="";

    public static String getMessage() {
        return message;
    }

    /**
     * 获取PD单中得物料信息
     * @param file 上传文件
     * @return PD中内容BillCache集合
     */
    public static List<GeelyBillCache> getContext(MultipartFile file, Supplier supplier, Factory factory){
        InputStream is =null;
        //定义PDF对象
        PDDocument document =null;
        //定义文件输入流
        try {
            is = file.getInputStream();
            //是否排序
            boolean sort = true;
            //开始提取页
            int startPage = 1;
            //结束提取页
            int endPage = Integer.MAX_VALUE;
            //定义PDF对象
            document =PDDocument.load(is);
            if (document != null) {
                //定义一个PDFTextStripper对象,PDF文本内容剥离对象
                PDFTextStripper pts = null;
                try {
                    pts = new PDFTextStripper();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="读取文件失败,PDF文件Text对象错误";
                    return null;
                }
                //获取结束提取页
                endPage = document.getNumberOfPages();
                //给PDF文本剥离对象设置起始结束页
                pts.setStartPage(startPage);
                pts.setEndPage(endPage);
                //设置读取是否按照顺序读取
                pts.setSortByPosition(sort);
                try {
                    //用PDF文本剥离对象，剥离PDF对象的文本内容，将文本内容赋值给前面定义的String content
                    String content = pts.getText(document);
                    //System.out.println(content);
                    String[] lines = content.split("\\r?\\n");
                    if (lines[0].replace(" ", "").contains("交货记录单")) {
                        //单号
                        String billNumber="";
                        //工厂编号
                        String factoryNumber="";
                        //供应商编号
                        String supplierCode="";
                        for(String str:lines){
                            if(str.contains("单据编号")&&str.contains("PD")){
                                billNumber= str.replace("  ", " ").split(" ")[1];
                            }
                            if(str.contains("供应商名称")&&str.contains("供应商编号")){
                                supplierCode= str.replace(" ", "").split("：")[1].substring(0,6);
                            }
                            if(str.contains("工厂：")&&str.contains("-")){
                                factoryNumber= str.replace(" ", "").split("：")[3].split("-")[0];
                                break;
                            }
                        }
                        if(billNumber.equals("")){
                            message="找不到单号";
                            return null;
                        }else if(factoryNumber.equals("")){
                            message="PD单中的工厂编号获取不到";
                            return null;
                        }else if(!factoryNumber.equals(factory.getFactorynumber())){
                            message="PD单送达的工厂和所选的工厂不一致";
                            return null;
                        }else if(supplierCode.equals("")){
                            message="PD单中的供应商编号获取不到";
                            return null;
                        }else if(!supplier.getSuppliercode().equals(supplierCode)){
                            message="不是供应商("+supplier.getSuppliercode()+supplier.getSuppliername()+")的PD单";
                            return null;
                        }else {
                            //定义集合存放PD单内容信息集合
                            List<GeelyBillCache> list=new ArrayList<>();
                            //有单号了，获取物料信息。先获取表头行下标，再获取表格最后一行即合计行
                            int headIndex=0;
                            int lastIndex=0;
                            for (int i = 0; i < lines.length; i++) {
                                if(lines[i].contains("序号")&&lines[i].contains("物料编号")&&lines[i].contains("物料名称")&&lines[i].contains("批次号")&&lines[i].contains("送货数")){
                                    headIndex=i;
                                }
                                if(lines[i].contains("合计")){
                                    lastIndex=i;
                                    break;
                                }
                            }
                            //从表头开始循环，如果第一个是数字，
                            for (int i =headIndex; i <=lastIndex; i++) {
                                //用空格分割行
                                String[] line = lines[i].replace("  ", " ").split(" ");
                                //分割后如果第一个是数字，且分割后数量大于等于4，那么就是物料行
                                if(line[0].matches("^[1-9]{1}[0-9]{0,10}$")&&line.length >= 4){
                                    //物料编号
                                    String goodCode="";
                                    //物料收容数
                                    int oneBoxCount=0;
                                    //物料的需求数量
                                    int count=0;
                                    //批次
                                    String batch="";
                                    //分以下情况
                                    //1.物料编号<=13位没换行，物料名称没换行
                                    //如果行以空格分割后第二个是数字或者大写字母组成且第三个是6位纯数字组成，那么说明第二个是物料编号且是长度<=13的物料编号
                                    if(line[1].matches("^[0-9A-Z]{1,13}$")&&line[3].matches("^[0-9]{6}$")){
                                        goodCode=line[1];
                                        batch=line[3];
                                        for(int g=4;g<line.length;g++){
                                            if(line[g].matches("^[1-9]{1}[0-9]{0,10}$")){
                                                oneBoxCount=Integer.parseInt(line[g]);
                                                count=Integer.parseInt(line[g+1]);
                                                break;
                                            }
                                        }
                                    }
                                    //2.物料编号<=13位没换行，物料名称换行。分割后第二个是数字或大写字母组成，第三个是6位数字
                                    if(line[1].matches("^[0-9A-Z]{1,13}$")&&line[2].matches("^[0-9]{6}$")){
                                        goodCode=line[1];
                                        batch=line[2];
                                        for(int g=3;g<line.length;g++){
                                            if(line[g].matches("^[1-9]{1}[0-9]{0,10}$")){
                                                oneBoxCount=Integer.parseInt(line[g]);
                                                count=Integer.parseInt(line[g+1]);
                                                break;
                                            }
                                        }
                                    }
                                    //3.物料编号>13位换行了，物料名称没换行。分割后第二个不是数字和大写字母组成，第三个是6位数字
                                    if(!line[1].matches("^[0-9A-Z]{1,13}$")&&line[2].matches("^[0-9]{6}$")){
                                        //如果分割后第二个不是数字和大写字母组成且第三个是6位纯数字，那么说明，物料编号超过13位换行了，第二个是物料名称没换行
                                        goodCode=lines[i-1].replace("  ", " ").split(" ")[0]+lines[i+1].replace("  ", " ").split(" ")[0];
                                        batch=line[2];
                                        for(int g=3;g<line.length;g++){
                                            if(line[g].matches("^[1-9]{1}[0-9]{0,10}$")){
                                                oneBoxCount=Integer.parseInt(line[g]);
                                                count=Integer.parseInt(line[g+1]);
                                                break;
                                            }
                                        }
                                    }
                                    //4.物料编号>13位换行了，物料名称换行。分割后第二位是6位纯数字
                                    if(line[1].matches("^[0-9]{6}$")){
                                        goodCode=lines[i-1].replace("  ", " ").split(" ")[0]+lines[i+1].replace("  ", " ").split(" ")[0];
                                        batch=line[1];
                                        for(int g=2;g<line.length;g++){
                                            if(line[g].matches("^[1-9]{1}[0-9]{0,10}$")){
                                                oneBoxCount=Integer.parseInt(line[g]);
                                                count=Integer.parseInt(line[g+1]);
                                                break;
                                            }
                                        }
                                    }
                                    //看传入的收容数和系统中的是否一致
                                    Good good=pdfBoxUtil.goodMapper.selectByGoodcodeAndSupplierid(goodCode,supplier.getId());
                                    if(good==null){
                                        message="工厂("+factory.getFactoryname()+")中没有物料编号("+goodCode+")供应商编号("+supplier.getSuppliercode()+")的物料信息";
                                        list=null;
                                        return list;
                                    }else {
                                        //验证是否已经完成了
                                        GeelyBillRecord geelyBillRecord =pdfBoxUtil.geelyBillRecordMapper.selectByGoodidAndBillnumber(good.getId(),billNumber);
                                        if(geelyBillRecord !=null){
                                            message="PD单"+billNumber+"中的物料编号"+goodCode+"的记录已经送到工厂了且回执了";
                                            list=null;
                                            return list;
                                        }else {
                                            //物料信息存在，生成PD在途记录
                                            GeelyBillCache geelyBillCache1 =new GeelyBillCache();
                                            geelyBillCache1.setBillnumber(billNumber);
                                            geelyBillCache1.setGood(good);
                                            geelyBillCache1.setCount(count);
                                            geelyBillCache1.setBatch(batch);
                                            geelyBillCache1.setUrgent("否");
                                            list.add(geelyBillCache1);
                                        }
                                    }
                                }//物料行判断完毕
                            }//获取物料信息完毕
                            //返回得到的所有PD单中物料信息
                            return list;
                        }//单号是否存在判断完毕
                    }else {
                        message="不是PD单";
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    message="读取文件失败,获取文件文本内容失败";
                    return null;
                }
            }else {
                message="读取文件失败,文件流转为PDF对象失败";
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            message="读取文件失败,流转化失败";
            return null;
        }finally {
            if(document!=null){
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="读取文件失败,关闭PDF Document失败";
                    return null;
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="读取文件失败,关闭流失败";
                    return null;
                }
            }
        }
    }

    /**
     * 获取PD单中得供应商编号
     * @param file
     * @return
     */
    public static String getSupplierCode(MultipartFile file){
        InputStream is =null;
        //定义PDF对象
        PDDocument document =null;
        //定义文件输入流
        try {
            is = file.getInputStream();
            //是否排序
            boolean sort = true;
            //开始提取页
            int startPage = 1;
            //结束提取页
            int endPage = Integer.MAX_VALUE;
            //定义PDF对象
            document =PDDocument.load(is);
            if (document != null) {
                //定义一个PDFTextStripper对象,PDF文本内容剥离对象
                PDFTextStripper pts = null;
                try {
                    pts = new PDFTextStripper();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="读取文件失败,PDF文件Text对象错误";
                    return null;
                }
                //获取结束提取页
                endPage = document.getNumberOfPages();
                //给PDF文本剥离对象设置起始结束页
                pts.setStartPage(startPage);
                pts.setEndPage(endPage);
                //设置读取是否按照顺序读取
                pts.setSortByPosition(sort);
                try {
                    //用PDF文本剥离对象，剥离PDF对象的文本内容，将文本内容赋值给前面定义的String content
                    String content = pts.getText(document);
                    //System.out.println(content);
                    String[] lines = content.split("\\r?\\n");
                    if (lines[0].replace(" ", "").contains("交货记录单")) {
                        //供应商编号
                        String supplierCode="";
                        for(String str:lines){
                            if(str.contains("供应商名称")&&str.contains("供应商编号")){
                                supplierCode= str.replace(" ", "").split("：")[1].substring(0,6);
                            }
                        }
                        return supplierCode;
                    }else {
                        message="不是PD单";
                        return null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    message="读取文件失败,获取文件文本内容失败";
                    return null;
                }
            }else {
                message="读取文件失败,文件流转为PDF对象失败";
                return null;
            }
        } catch (IOException e) {
            e.printStackTrace();
            message="读取文件失败,流转化失败";
            return null;
        }finally {
            if(document!=null){
                try {
                    document.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="读取文件失败,关闭PDF Document失败";
                    return null;
                }
            }
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    message="读取文件失败,关闭流失败";
                    return null;
                }
            }
        }
    }
}