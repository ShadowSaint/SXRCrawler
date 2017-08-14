package dao;

import java.sql.Connection;

/**
 * Created by Shadow on 2017/8/14.
 */
public interface SXRDao {
    String getNewestDBDateTime(Connection conn);
}
