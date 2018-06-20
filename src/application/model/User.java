package application.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.gateway.TableGateway;
import application.model.gateway.UserTableGateway;
import application.util.exception.AppException;

public class User extends Model {
	private static Logger logger = LogManager.getLogger(User.class);
	private UserTableGateway gateway;
	
	private Integer id;
	private String username;
	private String password; //hashed
	private Role role;

	public User() {
		id = null;
		
		gateway = new UserTableGateway();
	}
	
	public User(String username, String password, Role role) {
		this();
		setUsername(username);
		setPassword(password);
		setRole(role);
	}
	
	public static User create(ResultSet rs, String alias) throws SQLException {
		if(alias == null)
			alias = "";
		
		int id = rs.getInt(alias + "id");
		String username = rs.getString(alias + "username");
		String password = rs.getString(alias + "password");
		
		Role role = Role.create(rs, "r.");

		User user = new User(username, password, role);
		user.setId(id);
		return user;
	}

	@Override
	public void save() {
		logger.warn("user does not implement saving");
	}

	@Override
	public boolean validate() throws AppException {
		throw new AppException("user does not implement validate");
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	/* gateway accessors */

	@Override
	public UserTableGateway getGateway() {
		return this.gateway;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (UserTableGateway)gateway;
	}
}
