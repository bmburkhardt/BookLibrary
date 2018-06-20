package application.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ConnectionFactory {
	private static Logger logger = LogManager.getLogger(ConnectionFactory.class);
	
	public static Connection createConnection() {
		Connection conn = null;
		
		PropertyReader pm = PropertyReader.getInstance();
		try {
			DriverManager.setLoginTimeout(10);
			conn = DriverManager.getConnection(pm.getDatabaseURL(),
					pm.getDatabaseUsername(), pm.getDatabasePassword());
		} catch (SQLException e) {
			e.printStackTrace();
			logger.fatal("Connection Failed!");
			System.exit(-1);
		}
		
		return conn;
	}
}
