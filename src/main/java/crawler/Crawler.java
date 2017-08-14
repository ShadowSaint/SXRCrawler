package crawler;

import dao.SXRDao;
import dao.SXRDaoImpl;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import util.DBUtil;
import util.GRQUtil;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Created by Shadow on 2017/8/14.
 */
public class Crawler {

    public static void main(String[] args) {
        Connection conn= DBUtil.getConnection();
        run(conn);
        DBUtil.closeConnection();
    }

    private static void run(Connection conn){
        SXRDao dao=new SXRDaoImpl();
        //获取数据库内最新的时间
        String dbDateTimeString= dao.getNewestDBDateTime(conn);
        //除错处理
        if (GRQUtil.checkNull(dbDateTimeString)||"error".equals(dbDateTimeString)){
            return;
        }
        LocalDate dbDateTime=LocalDate.parse(dbDateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        getZonalList(dbDateTime);
    }

    private static void getZonalList(LocalDate dbDateTime) {
        try {
            Document doc= Jsoup.connect("http://mis.nyiso.com/public/P-24Alist.htm").ignoreContentType(true).get();
            Elements elements=doc.select("a");
            for (int i=elements.size()-1;i>=0;i--){
                Element element=elements.get(i);
                String text=element.text();
                try {
                    LocalDate itemDateTime=LocalDate.parse("01-"+text,DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    if (!itemDateTime.isBefore(dbDateTime)){
                        String url="http://mis.nyiso.com/public/"+element.attr("href");
                        String fileName = url.split("/")[url.split("/").length - 1];
                        //如果不这么切割的话,形如这样的网址就没法下载了
                        //http://cms-bucket.nosdn.127.net/73be74f2bc8a47c39e300fec9da7908020170307162629.png?imageView&thumbnail=550x0
                        fileName = fileName.split("\\?")[0];
                        File file=new File(Crawler.class.getResource ("").getFile());
                        file=file.getParentFile();
                        String filePath=file+File.separator+"file"+File.separator+fileName;
                        System.out.println(filePath);
                    }
                }catch (Exception e){
                    continue;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }


    }

}
