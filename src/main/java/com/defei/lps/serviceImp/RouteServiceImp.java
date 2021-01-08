package com.defei.lps.serviceImp;

import com.defei.lps.dao.*;
import com.defei.lps.entity.*;
import com.defei.lps.result.Result;
import com.defei.lps.result.ResultUtil;
import com.defei.lps.service.RouteService;
import com.defei.lps.uploadUtil.RouteExcelUpload;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.shiro.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RouteServiceImp implements RouteService {
    @Autowired
    private RouteMapper routeMapper;
    @Autowired
    private UserRouteMapper userRouteMapper;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private FactoryMapper factoryMapper;
    @Autowired
    private AreaMapper areaMapper;
    @Autowired
    private RouteWarehouseMapper routeWarehouseMapper;
    @Autowired
    private WarehouseMapper warehouseMapper;

    /**
     * 添加线路
     * @param routeName 线路名称
     * @param routeNumber   线路编号
     * @param areaId    区域id
     * @param factoryId 工厂id
     * @param describes 描述
     * @param warehouse 途径的中转仓名称集合，格式：中转仓名称,中转仓名称,...   越前面途径越早，序号越小，序号从1开始
     * @return
     */
    @Override
    public Result add(String routeName, String routeNumber, String describes,int areaId,int factoryId,String warehouse) {
        //校验参数
        if(!routeName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_+*-]{1,20}$")){
            return ResultUtil.error(1,"线路名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_+*-)");
        }else if(!routeNumber.matches("^[0-9A-Za-z@#_-]{1,9}$")){
            return ResultUtil.error(1,"线路编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!describes.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_-]{0,50}$")){
            return ResultUtil.error(1,"描述只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }
        Area area=areaMapper.selectByPrimaryKey(areaId);
        if(area==null){
            return ResultUtil.error(1,"选择的区域不存在");
        }
        Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
        if(factory==null){
            return ResultUtil.error(1,"选择的工厂不存在");
        }
        //检查传入的中转仓是否存在
        List<Warehouse> warehouseList=new ArrayList<>();
        if(!warehouse.equals("")){
            String[] warehouseList1=warehouse.split(",");
            for(String warehouseName:warehouseList1){
                Warehouse warehouse1=warehouseMapper.selectByWarehousename(warehouseName);
                if(warehouse1==null){
                    return ResultUtil.error(1,"中转仓："+warehouseName+"不存在，刷新也后重试");
                }else {
                    //如果中转仓存在，则放入集合
                    warehouseList.add(warehouse1);
                }
            }
        }
        //看是否有重复编号的线路
        Route route=routeMapper.selectByRoutenumber(routeNumber);
        if(route!=null){
            return ResultUtil.error(1,"线路编号已经存在");
        }
        Route route1=routeMapper.selectByRoutename(routeName);
        if(route1!=null){
            return ResultUtil.error(1,"线路名称已经存在");
        }
        //看是否已经有重复的区域id+工厂id的线路。每个区域对每个工厂只能有一条线路
        Route route2=routeMapper.selectByFactoryidAndAreaid(factoryId,areaId);
        if(route2!=null){
            return ResultUtil.error(1,""+area.getAreaname()+"到"+factory.getFactoryname()+"的线路已经存在");
        }
        //保存线路信息
        Route route3=new Route();
        route3.setRoutename(routeName);
        route3.setRoutenumber(routeNumber);
        route3.setArea(area);
        route3.setFactory(factory);
        route3.setDescribes(describes);
        routeMapper.insertSelective(route3);
        //获取刚保存的线路的id
        int routeId=routeMapper.selectByRoutename(routeName).getId();
        //看是否有中转仓，如果有则保存线路-中转仓关系记录
        for(int i=0;i<warehouseList.size();i++){
            RouteWarehouse routeWarehouse=new RouteWarehouse();
            routeWarehouse.setRouteid(routeId);
            routeWarehouse.setWarehouseid(warehouseList.get(i).getId());
            routeWarehouse.setSortnumber(i+1);
            routeWarehouseMapper.insertSelective(routeWarehouse);
        }
        //把新增线路和系统超级管理中绑定
        User user=userMapper.selectByUserName("furiadmin");
        if(user!=null){
            UserRoute userRoute=new UserRoute();
            userRoute.setUserid(user.getId());
            userRoute.setRouteid(routeId);
            userRouteMapper.insertSelective(userRoute);
        }
        return ResultUtil.success();
    }

    /**
     * 上传线路
     * @param excelFile
     * @return
     */
    @Override
    public Result upload(MultipartFile excelFile) {
        RouteExcelUpload re=new RouteExcelUpload();
        List<Map<String,Object>> list=re.getList(excelFile);
        if(list==null){
            return ResultUtil.error(1,re.getMessage());
        }
        User user=userMapper.selectByUserName("furiadmin");
        //保存线路，线路编号重复的不保存
        int count=0;
        for(Map<String,Object> map:list){
            Route route=(Route) map.get("route");
            Route route1=routeMapper.selectByRoutenumber(route.getRoutenumber());
            //如果线路编号不存在，可以保存线路信息
            if(route1==null){
                List<Warehouse> warehouseList=(List<Warehouse>)map.get("warehouse");
                //保存线路信息
                routeMapper.insertSelective(route);
                //保存线路-中转仓关系记录
                int routeId=routeMapper.selectByRoutename(route.getRoutename()).getId();
                for(int i=0;i<warehouseList.size();i++){
                    RouteWarehouse routeWarehouse=new RouteWarehouse();
                    routeWarehouse.setRouteid(routeId);
                    routeWarehouse.setWarehouseid(warehouseList.get(i).getId());
                    routeWarehouse.setSortnumber(i+1);
                    routeWarehouseMapper.insertSelective(routeWarehouse);
                }
                //把新增线路和系统超级管理中绑定
                if(user!=null){
                    UserRoute userRoute=new UserRoute();
                    userRoute.setUserid(user.getId());
                    userRoute.setRouteid(routeId);
                    userRouteMapper.insertSelective(userRoute);
                }
                count++;
            }
        }
        return ResultUtil.success("总共上传"+list.size()+"条记录，成功新增"+count+"条");
    }

    /**
     * 修改线路信息
     * 不能修改出发区域、到货工厂
     * @param id
     * @param routeName
     * @param routeNumber
     * @param areaId    区域id
     * @param factoryId 工厂id
     * @param describes
     * @return
     */
    @Override
    public Result update(int id, String routeName,String routeNumber,String describes,String warehouse) {
        //校验参数
        if(!routeName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_-]{1,20}$")){
            return ResultUtil.error(1,"线路名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!routeNumber.matches("^[0-9A-Za-z@#_-]{1,9}$")){
            return ResultUtil.error(1,"线路编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }else if(!describes.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_-]{0,50}$")){
            return ResultUtil.error(1,"描述只能是1-50位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }
        //检查传入的中转仓是否存在
        List<Warehouse> warehouseList=new ArrayList<>();
        if(!warehouse.equals("")){
            String[] warehouseList1=warehouse.split(",");
            for(String warehouseName:warehouseList1){
                Warehouse warehouse1=warehouseMapper.selectByWarehousename(warehouseName);
                if(warehouse1==null){
                    return ResultUtil.error(1,"中转仓："+warehouseName+"不存在，刷新也后重试");
                }else {
                    //如果中转仓存在，则放入集合
                    warehouseList.add(warehouse1);
                }
            }
        }
        //看是否存在
        Route route=routeMapper.selectByPrimaryKey(id);
        if(route==null){
            return ResultUtil.error(1,"线路不存在，刷新页面后再试");
        }
        //看修改后的线路编号是否存在
        if(!route.getRoutenumber().equals(routeNumber)){
            Route route1=routeMapper.selectByRoutenumber(routeNumber);
            if(route1!=null){
                return ResultUtil.error(1,"修改后的线路编号已存在");
            }
        }
        if(!route.getRoutename().equals(routeName)){
            Route route1=routeMapper.selectByRoutename(routeName);
            if(route1!=null){
                return ResultUtil.error(1,"修改后的线路名称已存在");
            }
        }
        //修改线路信息
        route.setRoutename(routeName);
        route.setRoutenumber(routeNumber);
        route.setDescribes(describes);
        routeMapper.updateByPrimaryKeySelective(route);
        //修改线路-中转仓关系表记录
        routeWarehouseMapper.deleteByRouteid(route.getId());
        for(int i=0;i<warehouseList.size();i++){
            RouteWarehouse routeWarehouse=new RouteWarehouse();
            routeWarehouse.setRouteid(route.getId());
            routeWarehouse.setWarehouseid(warehouseList.get(i).getId());
            routeWarehouse.setSortnumber(i+1);
            routeWarehouseMapper.insertSelective(routeWarehouse);
        }
        return ResultUtil.success();
    }

    /**
     * 删除线路信息
     * @param id
     * @return
     */
    @Override
    public Result delete(int id) {
        //看是否存在
        Route route=routeMapper.selectByPrimaryKey(id);
        if(route==null){
            return ResultUtil.error(1,"线路不存在，刷新页面后再试");
        }
        //删除凡是用到了该线路编号的表记录
        //TODO
        //删除线路-中转仓关系表记录
        routeWarehouseMapper.deleteByRouteid(id);
        //删除线路用户关系表记录
        userRouteMapper.deleteByRouteid(id);
        //删除线路信息
        routeMapper.deleteByPrimaryKey(id);
        return ResultUtil.success();
    }

    /**
     * 条件分页查询
     * @param routeName
     * @param routeNumber
     * @param areaId    区域id
     * @param factoryId 工厂id
     * @param currentPage
     * @return
     */
    @Override
    public Result findAll(String routeName, String routeNumber,int areaId,int factoryId, int currentPage) {
        if(!routeName.matches("^[0-9A-Za-z\\u4e00-\\u9fa5@#_-]{0,20}$")){
            return ResultUtil.error(1,"线路名称只能是1-20位的数字、大小写字母、汉字、特殊字符(@#_-)");
        }else if(!routeNumber.matches("^[0-9A-Za-z@#_-]{0,9}$")){
            return ResultUtil.error(1,"线路编号只能是1-9位的数字、大小写字母、特殊字符(@#_-)");
        }
        if(areaId!=0){
            Area area=areaMapper.selectByPrimaryKey(areaId);
            if(area==null){
                return ResultUtil.error(1,"选择的区域不存在");
            }
        }
        if(factoryId!=0){
            Factory factory=factoryMapper.selectByPrimaryKey(factoryId);
            if(factory==null){
                return ResultUtil.error(1,"选择的工厂不存在");
            }
        }
        //查询起始下标
        int index=(currentPage-1)*30;
        //分页条件查询
        List<Route> list=routeMapper.selectLimitByCondition(routeName,routeNumber ,areaId,factoryId,index);
        if(!list.isEmpty()) {
            //总数量
            int totalCount=routeMapper.selectCountByCondition(routeName,routeNumber ,areaId,factoryId);
            int totalPage=0;
            if(totalCount%30==0) {
                totalPage=totalCount/30;
            }else {
                totalPage=totalCount/30+1;
            }
            Map map=new HashMap();
            map.put("currentPage",currentPage);
            map.put("totalPage",totalPage);
            map.put("totalCount",totalCount);
            //返回的集合内容
            List<Map<String,Object>> resultList=new ArrayList<>();
            for(Route route:list){
                List<Warehouse> warehouseList=warehouseMapper.selectByRouteid(route.getId());
                Map map1=new HashMap();
                map1.put("route",route);
                if(warehouseList.isEmpty()){
                    map1.put("warehouse","");
                }else {
                    map1.put("warehouse",warehouseList);
                }
                resultList.add(map1);
            }
            //集合内容
            map.put("list",resultList);
            return ResultUtil.success(map);
        }
        return ResultUtil.success();
    }

    /**
     * 获取当前账号的所有能看的线路
     * 主要是为页面提供下拉框的数据
     * @return
     */
    @Override
    public Result currentRoute() {
        String userName= (String) SecurityUtils.getSubject().getPrincipal();
        User user=userMapper.selectByUserName(userName);
        if(user==null){
            return ResultUtil.error(1,"请刷新页面后重试");
        }
        List<Route> routeList=routeMapper.selectByUserid(user.getId());
        if(routeList.isEmpty()){
            return ResultUtil.error(1,"当前账号没有分配线路");
        }
        return ResultUtil.success(routeList);
    }

    /**
     * 查询所有的线路，以工厂分组，返回zTree格式数据
     * 用户管理页面，以zTree格式返回
     * zTree格式：[{id:1,pid:0,name:"aaaa"},{id:2,pid:0,name:"bbbb"},...]
     * @return
     */
    @Override
    public Result routeGroupFactorynumber() {
        //查询所有线路
        List<Route> routeList=routeMapper.selectAll();
        if(routeList.isEmpty()){
            return ResultUtil.error(1,"系统中没有线路");
        }
        //集合存放工厂对象
        List<Factory> factoryList=new ArrayList<>();
        for(Route route:routeList){
            if(!factoryList.contains(route.getFactory())){
                factoryList.add(route.getFactory());
            }
        }
        int factoryId=1;
        int routeId=factoryList.size()+1;
        List<Map<String,Object>> resultList=new ArrayList<>();
        for(Factory factory:factoryList){
            Map<String,Object> map=new HashMap<>();
            map.put("id",factoryId);
            map.put("pid",0);
            map.put("name",factory.getFactoryname());
            //保存工厂信息
            resultList.add(map);
            //保存工厂信息之下的线路信息
            for(Route route:routeList){
                if(factory.getId()==route.getFactory().getId()){
                    Map<String,Object> map1=new HashMap<>();
                    map1.put("id",routeId);
                    map1.put("pid",factoryId);
                    map1.put("name",route.getRoutename());
                    resultList.add(map1);
                    routeId++;
                }
            }
            factoryId++;
        }
        return ResultUtil.success(resultList);
    }

    /**
     * 根据id查询
     * @param id
     * @return
     */
    @Override
    public Result routeById(int id) {
        Route route=routeMapper.selectByPrimaryKey(id);
        if(route==null){
            return ResultUtil.error(1,"线路不存在");
        }
        List<Warehouse> warehouseList=warehouseMapper.selectByRouteid(route.getId());
        Map map=new HashMap();
        map.put("route",route);
        if(warehouseList.isEmpty()){
            map.put("warehouse","");
        }else {
            map.put("warehouse",warehouseList);
        }
        return ResultUtil.success(map);
    }

    /**
     * 下载
     * @param routeName
     * @param routeNumber
     * @param areaId
     * @param factoryId
     * @param response
     */
    @Override
    public void routeDownload(String routeName, String routeNumber, int areaId, int factoryId, HttpServletResponse response) {
        //获取结果
        List<Route> list=routeMapper.selectByCondition(routeName,routeNumber,areaId,factoryId);
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"线路信息表");
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
        goodCodeCell.setCellValue("线路名称");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("线路编号");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("描述");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("出发区域");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("终点工厂");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("途径中转仓");
        countCell.setCellStyle(titleCellStyle);

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
                //名称
                Cell cell0 = bodyRow.createCell(0);
                cell0.setCellValue(list.get(i).getRoutename());
                cell0.setCellStyle(bodyCellStyle);
                cell0.setCellType(Cell.CELL_TYPE_STRING);
                //编号
                Cell cell1 = bodyRow.createCell(1);
                cell1.setCellValue(list.get(i).getRoutenumber());
                cell1.setCellStyle(bodyCellStyle);
                cell1.setCellType(Cell.CELL_TYPE_STRING);
                //描述
                Cell cell2 = bodyRow.createCell(2);
                cell2.setCellValue(list.get(i).getDescribes());
                cell2.setCellStyle(bodyCellStyle);
                cell2.setCellType(Cell.CELL_TYPE_STRING);
                //出发区域
                Cell cell3 = bodyRow.createCell(3);
                cell3.setCellValue(list.get(i).getArea().getAreaname());
                cell3.setCellStyle(bodyCellStyle);
                cell3.setCellType(Cell.CELL_TYPE_STRING);
                //工厂
                Cell cell4 = bodyRow.createCell(4);
                cell4.setCellValue(list.get(i).getFactory().getFactoryname());
                cell4.setCellStyle(bodyCellStyle);
                cell4.setCellType(Cell.CELL_TYPE_STRING);
                //添加中转仓
                Cell cell5 = bodyRow.createCell(5);
                List<Warehouse> warehouseList=warehouseMapper.selectByRouteid(list.get(i).getId());
                String house="";
                for(Warehouse warehouse:warehouseList){
                    house+=","+warehouse.getWarehousename();
                }
                if(!house.equals("")){
                    house=house.substring(1);
                }
                cell5.setCellValue(house);
                cell5.setCellStyle(bodyCellStyle);
                cell5.setCellType(Cell.CELL_TYPE_STRING);
            }
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = list.get(0).getFactory().getFactoryname()+"线路信息.xlsx";
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
     * 批量上传模板下载
     * @param response
     */
    @Override
    public void routeModalDownload(HttpServletResponse response) {
        //创建Excel工作簿对象,此处选择SXSSFWorkbook,创建的excel以.xlsx结尾，支持2007、2010及以后版本
        SXSSFWorkbook wb = new SXSSFWorkbook();
        //创建表
        Sheet sheet = wb.createSheet();
        //给sheet设置名称
        wb.setSheetName(0,"sheet1");
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
        goodCodeCell.setCellValue("线路名称");
        goodCodeCell.setCellStyle(titleCellStyle);
        Cell goodNameCell = titleRow.createCell(1);
        goodNameCell.setCellValue("线路编号");
        goodNameCell.setCellStyle(titleCellStyle);
        Cell supplierCodeCell=titleRow.createCell(2);
        supplierCodeCell.setCellValue("描述");
        supplierCodeCell.setCellStyle(titleCellStyle);
        Cell supplierNameCell = titleRow.createCell(3);
        supplierNameCell.setCellValue("出发区域");
        supplierNameCell.setCellStyle(titleCellStyle);
        Cell boxCodeCell = titleRow.createCell(4);
        boxCodeCell.setCellValue("终点工厂");
        boxCodeCell.setCellStyle(titleCellStyle);
        Cell countCell = titleRow.createCell(5);
        countCell.setCellValue("途径中转仓");
        countCell.setCellStyle(titleCellStyle);

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
        cell0.setCellValue("终点工厂：");
        cell0.setCellStyle(bodyCellStyle);
        cell0.setCellType(Cell.CELL_TYPE_STRING);
        List<Factory> factoryList=factoryMapper.selectByCondition("","","","","");
        for(int k=0;k<factoryList.size();k++){
            Cell factoryCell= factoryRow.createCell(k+1);
            factoryCell.setCellValue(k+1+"."+factoryList.get(k).getFactoryname());
            factoryCell.setCellStyle(bodyCellStyle);
            factoryCell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //3.区域名称行
        Row areaRow = sheet.createRow(4);
        Cell cell1 = areaRow.createCell(0);
        cell1.setCellValue("出发区域：");
        cell1.setCellStyle(bodyCellStyle);
        cell1.setCellType(Cell.CELL_TYPE_STRING);
        List<Area> areaList=areaMapper.selectAll();
        for(int k=0;k<areaList.size();k++){
            Cell areaCell= areaRow.createCell(k+1);
            areaCell.setCellValue(k+1+"."+areaList.get(k).getAreaname());
            areaCell.setCellStyle(bodyCellStyle);
            areaCell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //4.中转仓
        Row warehouseRow = sheet.createRow(5);
        Cell warehouseCell1 = warehouseRow.createCell(0);
        warehouseCell1.setCellValue("途径中转仓：(如果有多个中转仓，那么中转仓名称之间用英文输入法状态下的,号分隔开)");
        warehouseCell1.setCellStyle(bodyCellStyle);
        warehouseCell1.setCellType(Cell.CELL_TYPE_STRING);
        List<Warehouse> warehouseList=warehouseMapper.selectAll();
        for(int k=0;k<warehouseList.size();k++){
            Cell warehouseCell= warehouseRow.createCell(k+1);
            warehouseCell.setCellValue(k+1+"."+warehouseList.get(k).getWarehousename());
            warehouseCell.setCellStyle(bodyCellStyle);
            warehouseCell.setCellType(Cell.CELL_TYPE_STRING);
        }
        //设置响应内容编码
        response.setCharacterEncoding("UTF-8");
        //设置响应的内容格式
        response.setContentType("aplication/x-download");
        //设置文件名称
        String fileName = "线路信息模板.xlsx";
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


}
