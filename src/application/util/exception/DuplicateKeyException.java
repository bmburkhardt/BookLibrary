package application.util.exception;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DuplicateKeyException extends NullPointerException {
	private static Logger logger = LogManager.getLogger(DuplicateKeyException.class);
	
	private static final long serialVersionUID = 1L;
	
	public DuplicateKeyException(String string) {
		super(string);
		
		logger.debug(string);
	}
}
