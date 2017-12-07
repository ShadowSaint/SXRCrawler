package crawler;

import com.csvreader.CsvReader;
import dao.SXRDao;
import dao.SXRDaoImpl;
import domain.CsvDO;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.DBUtil;
import util.DeleteFileUtil;
import util.GRQUtil;
import util.ZipUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shadow on 2017/8/14.
 */
public class Crawler {

    final static String DIR_ROOT_PATH = "C:\\GuRuoQi"+File.separator;

    public static void main(String[] args) {
        Connection conn= DBUtil.getConnection();
        run(conn);
        DBUtil.closeConnection();
    }

    private static void run(Connection conn){
        SXRDao dao=new SXRDaoImpl();
        getCsvList("http://mis.nyiso.com/public/P-24Alist.htm",conn,dao,0);
        getCsvList("http://mis.nyiso.com/public/P-24Blist.htm",conn,dao,1);
        createExcel(conn,dao);
    }

    private static void getCsvList(String sourceUrl,Connection conn,SXRDao sxrDao,int type) {
        try {
            //获取数据库内最新的时间
            String dbDateTimeString= sxrDao.getNewestDBDateTime(conn,type);
            //删除掉最新日期的那一月数据,方便断点续传
            sxrDao.removeNewestDBDateTimeData(conn,dbDateTimeString,type);
            //除错处理
            if (GRQUtil.checkNull(dbDateTimeString)||"error".equals(dbDateTimeString)){
                return;
            }
            LocalDate dbDateTime=LocalDate.parse(dbDateTimeString,DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Document doc= Jsoup.connect(sourceUrl).ignoreContentType(true).get();
            Elements elements=doc.select("a");
            for (int i=elements.size()-1;i>=0;i--){
                Element element=elements.get(i);
                String text=element.text();
                if (GRQUtil.checkNull(text)){
                    continue;
                }
                try {
                    LocalDate itemDateTime=LocalDate.parse("01-"+text,DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    if (!itemDateTime.isBefore(dbDateTime)){
                        String url="http://mis.nyiso.com/public/"+element.attr("href");
                        if (url.toLowerCase().contains("hb00_hb07")||
                                url.toLowerCase().contains("hb08_hb15")||
                                url.toLowerCase().contains("hb16_hb23")){
                            continue;
                        }
                        String fileName = url.split("/")[url.split("/").length - 1];
                        //如果不这么切割的话,形如这样的网址就没法下载了
                        //http://cms-bucket.nosdn.127.net/73be74f2bc8a47c39e300fec9da7908020170307162629.png?imageView&thumbnail=550x0
                        fileName = fileName.split("\\?")[0];
                        String dirPath= DIR_ROOT_PATH +"file"+File.separator;
                        //一开始先删掉,防止断点续传时出问题
                        DeleteFileUtil.DeleteFolder(dirPath);
                        String filePath=dirPath+fileName;
                        GRQUtil.downloadFromUrl(url,filePath);
                        List<File> fileList = ZipUtil.unZip(filePath,dirPath+"children"+File.separator);
                        if (fileList.size()==0){
                            //异常,结束
                            return ;
                        }
                        for (File item:fileList){
                            getCsvDO(item,type,sxrDao,conn);
                        }
                        DeleteFileUtil.DeleteFolder(dirPath);
                        DeleteFileUtil.DeleteFolder(filePath);
                        fileList.clear();
                    }
                }catch (Exception e){
                    return ;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return ;
        }
    }

    private static void getCsvDO(File file,int type,SXRDao sxrDao,Connection conn){
        try {
            CsvReader csvReader=new CsvReader(file.getAbsolutePath());
            csvReader.readRecord();
            while (csvReader.readRecord()){
                CsvDO csvDO=new CsvDO();
                String date=csvReader.get(0);
                date=date.split(" ")[0];
                LocalDate localDate=LocalDate.parse(date,DateTimeFormatter.ofPattern("MM/dd/yyyy"));
                date=localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                csvDO.setDate(date);
                csvDO.setName(csvReader.get(1));
                csvDO.setPtid(csvReader.get(2));
                csvDO.setMcc(csvReader.get(5));
                csvDO.setType(type);
                sxrDao.insertCSVDO(conn,csvDO);
                DeleteFileUtil.DeleteFolder(file.getAbsolutePath());
                System.out.println(LocalDateTime.now()+" 正在录入:");
                String message="名字:"+csvDO.getName()+"  PTIC:"+csvDO.getPtid()+"  时间:"+csvDO.getDate()+"  MCC:"+csvDO.getMcc();
                if (type==0){
                    message+=" 的 Zonal 数据";
                }else {
                    message+=" 的 Generator 数据";
                }
                System.out.println(message);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void createExcel(Connection conn,SXRDao sxrDao){
        try {
            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet=wb.createSheet("sheet1");
            sheet.setDefaultRowHeight((short)300);
            sheet.setDefaultColumnWidth(10);

            //居中样式
            XSSFCellStyle style1 = wb.createCellStyle(); // 样式对象
            style1.setVerticalAlignment(VerticalAlignment.CENTER);// 垂直
            style1.setAlignment(HorizontalAlignment.CENTER);// 水平
            Font font1 = wb.createFont();
            font1.setFontHeightInPoints((short)11);   //设置字体大小
            font1.setFontName("宋体");//字体
            font1.setBold(false);//加粗
            style1.setFont(font1);
            style1.setWrapText(true);//设置是否能换行,能true
            style1.setBorderBottom((short)1);//设置下划线，参数是黑线的宽度
            style1.setBorderLeft((short)1);   //设置左边框
            style1.setBorderRight((short)1);   //设置右边框
            style1.setBorderTop((short)1);   //设置下边框

            //获取时间列表
            List<String> dateList=new ArrayList<>();
            LocalDate todayLocalDate=LocalDate.now();
            LocalDate dateLocalDate=LocalDate.parse("2010-01-01",DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            while (!dateLocalDate.isAfter(todayLocalDate)){
                dateList.add(dateLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                dateLocalDate=dateLocalDate.plusDays(1);
            }

            //表头
            XSSFRow row0=sheet.createRow(0);
            XSSFCell cell_0_0=row0.createCell(0);
            cell_0_0.setCellStyle(style1);
            cell_0_0.setCellValue("PTID");
            XSSFCell cell_0_1=row0.createCell(1);
            cell_0_1.setCellStyle(style1);
            cell_0_1.setCellValue("name");
            for (int i=0;i<dateList.size();i++){
                XSSFCell cell=row0.createCell(i+2);
                cell.setCellStyle(style1);
                cell.setCellValue(dateList.get(i));
            }

            //获取所有公司
            List<String> nameList=sxrDao.getAllNameList(conn);
            //循环所有公司,每个公司一行
            for (int i=0;i<nameList.size();i++){
                XSSFRow row=sheet.createRow(i+1);
                //PTID
                XSSFCell cell_0=row.createCell(0);
                cell_0.setCellStyle(style1);
                String ptid=sxrDao.getPtidByName(conn,nameList.get(i));
                cell_0.setCellValue(ptid);
                //name
                XSSFCell cell_1=row.createCell(1);
                cell_1.setCellStyle(style1);
                cell_1.setCellValue(nameList.get(i));
                //开始循环天数
                for (int j=0;j<dateList.size();j++){
                    XSSFCell cell=row.createCell(j+2);
                    cell.setCellStyle(style1);
                    String mcc=sxrDao.getTotalMccByNameAndDate(conn,nameList.get(i),dateList.get(j));
                    cell.setCellValue(mcc);
                    System.out.println(LocalDateTime.now()+" 正在创建");
                    String message="第"+(i+1)+"家公司:"+nameList.get(i)+" , "+dateList.get(j)+" 数据";
                    System.out.println(message);
                }
            }
            String filepath = DIR_ROOT_PATH+"excel"+File.separator;
            //文件保存位置
            File saveDir = new File(filepath);
            if (!saveDir.exists()) {
                saveDir.mkdirs();
            }
            File file=new File(filepath+"result.xlsx");
            FileOutputStream fileOutputStream=new FileOutputStream(file);
            wb.write(fileOutputStream);
            wb.close();
            fileOutputStream.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
