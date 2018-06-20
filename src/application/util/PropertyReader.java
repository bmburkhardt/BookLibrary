package application.util;

import java.io.FileInputStream;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PropertyReader {
	private static Logger logger = LogManager.getLogger(PropertyReader.class);
	private static PropertyReader instance;
	
	private Properties prop = new Properties();
	
	private PropertyReader() throws Exception {
		String rootPath = Thread.currentThread().getContextClassLoader().getResource("").getPath();
		
		FileInputStream fis = new FileInputStream(rootPath + "properties.xml");
		prop.loadFromXML(fis);
		fis.close();
	}
	
	/*
	 * Getters
	 */
	
	public String getDatabaseDriver() {
		return prop.getProperty("db_driver");
	}
	
	public String getDatabaseURL() {
		return prop.getProperty("db_connection");
	}
	
	public String getDatabaseUsername() {
		return prop.getProperty("db_user");
	}
	
	public String getDatabasePassword() {
		return prop.getProperty("db_password");
	}
	
	public static PropertyReader getInstance() {
		if(instance == null) {
			try {
				instance = new PropertyReader();
			} catch(Exception e) {
				logger.error("Something went wrong...");
				e.printStackTrace();
			}
		}
		
		return instance;
	}
}
