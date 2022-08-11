package org.lx.servicecheck;

import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleConnection.ConnectionValidation;
import oracle.jdbc.pool.OracleDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class OracleServiceScan implements ServiceScan {

    static {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public String getName() {
        return "oracle";
    }

    //	@Override
//	public boolean isActive(String ip, ServerJobParam param) {
//		System.out.println("run OracleServiceScan " + ip);
//		Connection conn = null;
//		try {
//			conn = DriverManager.getConnection(getAddress(ip, param.getPort()));
//			return true;
//		} catch (SQLException e) {
//			e.printStackTrace();
//			int code = e.getErrorCode();
//			System.out.println(code);
//			if (code != 17002 && code != 17001) {
//				return true;
//			}
//			return false;
//		} finally {
//			if (conn != null) {
//				try {
//					conn.close();
//				} catch (SQLException e) {
//					e.printStackTrace();
//				}
//				conn = null;
//			}
//		}
//	}
    @Override
    public boolean isActive(String ip, int port) {
        System.out.println("run oracleServiceScan " + ip);
        try {
            OracleDataSource ods = new OracleDataSource();
            ods.setURL(getAddress(ip, port));
            ods.setUser("system");
            ods.setPassword("oracle");
            ods.setLoginTimeout(2);//单位秒

            Properties properties =
                    ods.getConnectionProperties();
            properties.setProperty("oracle.net.CONNECT_TIMEOUT", "5000");
            properties.setProperty("oracle.jdbc.ReadTimeout", "5000");

            Connection conn = ods.getConnection();
            boolean isValid = ((OracleConnection) conn).isValid(ConnectionValidation.SOCKET, 2);
            System.out.println("Connection isValid = " + isValid);
            return isValid;
        } catch (SQLException ex) {
            System.out.println("finish oracleServiceScan " + ip);
            int code = ex.getErrorCode();
            System.out.println(code);
            if (code != 17002 && code != 17001) {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    private String getAddress(String ip, int port) {
        return "jdbc:oracle:thin:@" + ip + ":" + port + ":orce";
    }

}
