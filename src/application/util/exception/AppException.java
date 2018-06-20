package application.util.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class AppException extends Exception {
	private static Logger logger = LogManager.getLogger(AppException.class);
	
	private static final long serialVersionUID = 1L;
	
	public AppException(String string) {
		super(string);
		
		logger.debug(string);
	}
}
