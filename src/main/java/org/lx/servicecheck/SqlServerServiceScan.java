package org.lx.servicecheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SqlServerServiceScan implements ServiceScan {

	static {
		try {
			Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	@Override
	public String getName() {
		return "sqlserver";
	}

	@Override
	public boolean isActive(String ip, int  port) {
		System.out.println("run SqlServerServiceScan " + ip);
		Connection conn = null;
		try {
//			DriverManager.setLoginTimeout(2);
			conn = DriverManager.getConnection(getAddress(ip, port));
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println(e.getErrorCode());
			if (e.getErrorCode() == 0) {
				return false;
			} else {
				return true;
			}
		} finally {
			System.out.println("finish SqlServerServiceScan " + ip);
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
		return "jdbc:sqlserver://"+ip+":"+port+";user=sa;password=123;loginTimeout=5;connectTimeout=5;socketTimeout=5";
	}

}
