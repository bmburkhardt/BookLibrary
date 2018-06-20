package application.model;

import application.model.gateway.TableGateway;
import application.util.exception.AppException;

public abstract class Model {
	public abstract void save();
	public abstract boolean validate() throws AppException;

	// id accessor
	public abstract Integer getId();
	public abstract void setId(Integer id);
	
	// gateway accessor
	public abstract TableGateway<?> getGateway();
	public abstract void setGateway(TableGateway<?> gateway);
}
