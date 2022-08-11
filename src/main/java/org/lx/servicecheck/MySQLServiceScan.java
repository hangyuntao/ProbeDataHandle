package org.lx.servicecheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MySQLServiceScan implements ServiceScan {

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "mysql";
    }

    @Override
    public boolean isActive(String ip, int port) {
        System.out.println("run MySQLServiceScan " + ip);
        Connection conn = null;
        try {
//            DriverManager.setLoginTimeout(2);
            conn = DriverManager.getConnection(getAddress(ip, port), "root", "123");
            return true;
        } catch (SQLException e) {
//			e.printStackTrace();
//			System.out.println(e.getErrorCode());
            if (e.getErrorCode() == 0) {
                return false;
            } else {
                return true;
            }
        } finally {
            System.out.println("finish MySQLServiceScan " + ip);
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                conn = null;
            }
        }
    }

    private String getAddress(String ip, int port) {
        return "jdbc:mysql://" + ip + ":" + port + "/mysql?connectTimeout=5000&socketTimeout=5000&maxWait=5000";
    }

}
