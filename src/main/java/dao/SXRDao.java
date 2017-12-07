package dao;

import domain.CsvDO;

import java.sql.Connection;
import java.time.LocalDate;
import java.util.List;

/**
 * Created by Shadow on 2017/8/14.
 */
public interface SXRDao {
    String getNewestDBDateTime(Connection conn,int type);
    int insertCSVDO(Connection conn,CsvDO csvDO);
    void removeNewestDBDateTimeData(Connection conn,String time,int type);
    List<String> getAllNameList(Connection conn);
    String getPtidByName(Connection conn,String name);
    String getTotalMccByNameAndDate(Connection conn,String name,String date);
}
