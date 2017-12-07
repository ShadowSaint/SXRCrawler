package dao;

import domain.CsvDO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shadow on 2017/8/14.
 */
public class SXRDaoImpl implements SXRDao {
    @Override
    public String getNewestDBDateTime(Connection conn,int type) {
        try {
            String sql = "select date from sxr_data where type = "+type+" order by date desc limit 0,1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs=ps.executeQuery();
            while (rs.next()){
                return rs.getString("date");
            }
            return "2010-01-01";
        }catch (Exception e){
            return "error";
        }
    }

    @Override
    public int insertCSVDO(Connection conn,CsvDO csvDO) {
        try {
            String sql="insert into sxr_data (gmt_create,gmt_modified,name,ptid,date,mcc,type) values (now(),now(),'"+csvDO.getName()+"','"+csvDO.getPtid()+"','"+csvDO.getDate()+"','"+csvDO.getMcc()+"',"+csvDO.getType()+")";
            conn.createStatement().executeUpdate(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
        return -1;
    }

    @Override
    public void removeNewestDBDateTimeData(Connection conn,String time, int type) {
        try {
            time=time.split("-")[0]+"-"+time.split("-")[1]+"-01";
            String sql = "delete from sxr_data where type = "+type+" and date >= '"+time+"'";
            conn.createStatement().executeUpdate(sql);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public List<String> getAllNameList(Connection conn) {
        try {
            List<String> nameList=new ArrayList<>();
            String sql="select name from sxr_data group by name order by name";
            ResultSet rs=conn.createStatement().executeQuery(sql);
            while (rs.next()){
                nameList.add(rs.getString("name"));
            }
            return nameList;
        }catch (Exception e){
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    @Override
    public String getPtidByName(Connection conn, String name) {
        try {
            String sql = "select ptid from sxr_data where name = '"+name+"' and ptid is not null and ptid != '' limit 0,1";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs=ps.executeQuery();
            while (rs.next()){
                return rs.getString("ptid");
            }
            return "";
        }catch (Exception e){
            e.printStackTrace();
            return "";
        }
    }

    @Override
    public String getTotalMccByNameAndDate(Connection conn, String name, String date) {
        try {
            String sql = "select mcc from sxr_data where date = '"+date+"' and name = '"+name+"'";
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs=ps.executeQuery();
            double total=0;
            while (rs.next()){
                total+=Double.valueOf(rs.getString("mcc").trim());
            }
            return String.valueOf(total);
        }catch (Exception e){
            return "#N/A";
        }
    }
}
