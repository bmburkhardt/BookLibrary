package application.model.gateway;

import java.util.List;

import application.model.Model;
import application.util.exception.AppException;

public abstract class TableGateway<T extends Model> {
	public abstract void add(T obj) throws AppException;
	public abstract void update(T obj) throws AppException;
	
	public List<T> get() throws AppException {
		return this.get(0, 50);
	}
	
	public abstract List<T> get(int page, int pageSize) throws AppException;
	
	public abstract void delete(T obj) throws AppException;
}
