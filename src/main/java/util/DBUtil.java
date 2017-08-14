package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by Shadow on 2016/8/30.
 */
public class DBUtil {
    private static Connection conn=null;
    /*
       * 功能：编写一个静态方法用于与数据库建立连接
       * 输入参数：无
       * 返回值：数据库连接对象
       * */
    public static Connection getConnection() {
        try{
            if (conn==null||conn.isClosed()){
                //定义连接数据库的URL资源
                String url = "jdbc:mysql://101.200.36.235:3306/gutongxue?serverTimezone=GMT";
                //定义连接数据库的用户名称与密码
                String username = "root";
                String password = "Grq1994711";
                //加载数据库连接驱动
                String className = "com.mysql.cj.jdbc.Driver";
                try {
                    Class.forName(className);
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                //获取数据库的连接对象
                try {
                    conn = DriverManager.getConnection(url, username, password);
                    System.out.println("数据库连接建立成功...");

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }catch (Exception e){
            conn=null;
            System.out.println("ConnectDB.getConnection Error:"+e.getMessage());
        }
        return conn;
    }

    public static void closeConnection() {
        try{
            if(conn!=null&&!conn.isClosed())
            {
                conn.close();
                System.out.println("数据库已关闭");
            }
        }catch (Exception e)
        {
            System.out.println("ConnectDB.closeConnection Error:"+e.getMessage());
        }
    }

}
