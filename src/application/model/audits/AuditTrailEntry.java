package application.model.audits;

import java.time.LocalDateTime;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.Model;
import application.model.gateway.AuditTrailTableGateway;
import application.model.gateway.TableGateway;
import application.util.exception.AppException;
import application.view.ViewManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class AuditTrailEntry<T extends AuditTrailModel<?>> extends Model {
	private static Logger logger = LogManager.getLogger(AuditTrailEntry.class);
	private AuditTrailTableGateway<T> gateway;
	
	private Integer id;
	private SimpleObjectProperty<LocalDateTime> dateAdded;
	private SimpleStringProperty message;
	private AuditTrailModel<?> model;
	
	public AuditTrailEntry() {
		this.dateAdded = new SimpleObjectProperty<>();
		this.message = new SimpleStringProperty();
	}
	
	public AuditTrailEntry(String message) {
		this();
		setMessage(message);
	}
	
	public AuditTrailEntry(String message, LocalDateTime dateAdded) {
		this();
		setMessage(message);
		setDateAdded(dateAdded);
	}
	
	@Override
	public String toString() {
		return "\"" + getMessage() + "\" on " + getDateAdded();
	}

	@Override
	public void save() {
		try {
			logger.info("save called");
			if(validate()) {
				logger.info("Validated all input fields!");
				
				if(this.getId() == null) 
					gateway.add(this);
				else
					gateway.update(this);
			}
		} catch (AppException e) {				
			e.printStackTrace();
			ViewManager.showError(e);
		}
	}

	@Override
	public boolean validate() {
		return true;
	}

	@Override
	public Integer getId() {
		return this.id;
	}

	@Override
	public void setId(Integer id) {
		this.id = id;
	}

	public AuditTrailModel<?> getModel() {
		return this.model;
	}

	public void setModel(AuditTrailModel<?> model) {
		this.model = model;
	}

	public LocalDateTime getDateAdded() {
		return dateAdded.get();
	}

	public void setDateAdded(LocalDateTime dateAdded) {
		this.dateAdded.set(dateAdded);
	}

	public String getMessage() {
		return message.get();
	}

	public void setMessage(String message) {
		this.message.set(message);
	}
	
	public SimpleStringProperty messageProperty() {
		return this.message;
	}
	
	public SimpleObjectProperty<LocalDateTime> dateAddedProperty() {
		return this.dateAdded;
	}

	@Override
	public TableGateway<?> getGateway() {
		return this.gateway;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (AuditTrailTableGateway<T>)gateway;
	}

}
