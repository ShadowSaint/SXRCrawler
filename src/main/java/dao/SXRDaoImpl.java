package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by Shadow on 2017/8/14.
 */
public class SXRDaoImpl implements SXRDao {
    @Override
    public String getNewestDBDateTime(Connection conn) {
        try {
            String sql = "select date from sxr_data order by date desc limit 0,1";
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
}
