package application;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import application.model.User;
import application.model.gateway.UserTableGateway;
import application.util.exception.AppException;

public class SessionManager {
	private static SessionManager instance;
	
	private UserTableGateway gateway;
	private User me;

	private LoginBeanRemote bean;
	private InitialContext context;
	
	private SessionManager() {
		Properties props = new Properties();
		props.put(Context.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
		props.put(Context.PROVIDER_URL, "http-remoting://localhost:8080");	
		props.put("jboss.naming.client.ejb.context", "true");
		try {
			context = new InitialContext(props);
			bean = (LoginBeanRemote) context.lookup("assignmentAuthService/LoginBean!application.LoginBeanRemote");

			this.gateway = new UserTableGateway();
		} catch (NamingException e) {
			e.printStackTrace();
			//Platform.exit();
		}
	}
	
	public static SessionManager getInstance() {
		if(instance == null)
			instance = new SessionManager();
		
		return instance;
	}
	
	/**
	 * 
	 * @return if login successful
	 */
	public boolean login(String username, String password) throws AppException {
		if(bean == null)
			throw new AppException("Wildfly server not detected, start it, then restart application.");
		
		int userId = bean.login(username, password);
		if(userId > 0)
			me = gateway.fetch(userId);
		
		return me != null;
	}
	
	public User getMe() {
		return this.me;
	}
	
	public void logout() {
		me = null;
	}
}
