package application.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.gateway.TableGateway;
import application.util.exception.AppException;

public class Role extends Model {
	private static Logger logger = LogManager.getLogger(Role.class);

	private Integer id;
	private String name;

	public Role() {
		id = null;
	}
	
	public Role(String name) {
		this();
		setName(name);
	}
	
	public static Role create(ResultSet rs, String alias) throws SQLException {
		if(alias == null)
			alias = "";
		
		int id = rs.getInt(alias + "id");
		String name = rs.getString(alias + "name");
		
		Role role = new Role(name);
		role.setId(id);
		return role;
	}

	
	@Override
	public void save() {
		logger.warn("role does not implement saving");
	}

	@Override
	public boolean validate() throws AppException {
		throw new AppException("role does not implement validate");
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/* permissions */
	
	public boolean canDelete() {
		if(getName().equals("Administrator"))
			return true;
		
		return false;
	}

	public boolean canCreate() {
		if(getName().equals("Administrator") || getName().equals("Data Entry"))
			return true;
		
		return false;
	}
	
	public boolean canGenerateReport() {
		if(getName().equals("Administrator") || getName().equals("Data Entry"))
			return true;
		
		return false;
	}
	
	/* gateway stuff */

	@Override
	public TableGateway<?> getGateway() {
		logger.error("role does not implement a gateway, use UserGateway");
		return null;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		logger.error("role does not implement a gateway, use UserGateway");
	}

}
