package application.model;


import java.sql.ResultSet;
import java.sql.SQLException;

import application.model.gateway.PublisherTableGateway;
import application.model.gateway.TableGateway;
import javafx.beans.property.SimpleStringProperty;

public class Publisher extends Model {
	private PublisherTableGateway gateway;
	
	private Integer id;
	private SimpleStringProperty publisherName;
	
	public Publisher() {
		publisherName = new SimpleStringProperty();
		id = null;
	}
	
	public Publisher(String publisherName) {
		this();
		setPublisherName(publisherName);
	}
	
	public static Publisher create(ResultSet rs, String alias) throws SQLException {
		if(alias == null)
			alias = "";
		
		String publisherName = rs.getString(alias + "publisher_name");
		int id = rs.getInt(alias + "id");
		
		Publisher publisher = new Publisher(publisherName);
		publisher.setId(id);
		return publisher;
	}
	
	@Override
	public String toString() {
		return getPublisherName();
	}

	@Override
	public void save() { } // does not save

	@Override
	public boolean validate() { return false; } //does not validate
	
	// accessors

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public void setPublisherName(String publisherName) {
		this.publisherName.set(publisherName);
	}

	public String getPublisherName() {
		return publisherName.get();
	}
	
	public SimpleStringProperty getPublisherNameProperty() {
		return publisherName;
	}
	
	// gateway accessors

	@Override
	public PublisherTableGateway getGateway() {
		return this.gateway;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (PublisherTableGateway)gateway;
	}
	

}
