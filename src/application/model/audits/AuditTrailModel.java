package application.model.audits;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import application.model.Model;
import application.model.gateway.AuditTrailTableGateway;
import application.util.exception.AppException;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;

public abstract class AuditTrailModel<T extends AuditTrailModel<?>> extends Model {
	public Map<String, String[]> dirty = new HashMap<String, String[]>();
	
	public abstract Integer getId();
	public abstract String getAuditTrailTableName();
	public abstract String getAuditTrailRefId();
	
	public abstract List<AuditTrailEntry<T>> getAudits() throws AppException;
	public abstract AuditTrailTableGateway<?> getAuditGateway();
	public void createAudit(String message) {
		AuditTrailEntry<T> audit = new AuditTrailEntry<>(message);
		audit.setModel(this);
		audit.setGateway(getAuditGateway());
		audit.save();
	}
	
	protected abstract void setupListeners();
	protected ChangeListener<Object> changeListener = new ChangeListener<Object>() {
		@Override
		public void changed(ObservableValue<?> o, Object oldVal, Object newVal) {
			if(oldVal != null && (o.getClass() != SimpleIntegerProperty.class || (Integer)oldVal != 0))
				markDirty(o, oldVal, newVal);
		}
	};
	public void saveChanges() {
		for(Entry<String, String[]> entry : dirty.entrySet()) {
			String[] change = entry.getValue();
			String message = entry.getKey() + " changed from " + change[0] + " to " + change[1];
			createAudit(message);
		}
		 
		clean();
	}

	protected String getPropertyName(ObservableValue<?> property) {
		try {
			for(Field field : getClass().getDeclaredFields()) {
				if(!Modifier.isStatic(field.getModifiers())) {
					field.setAccessible(true);
					
					Object curr = field.get(this);
					if(curr == property) //found our property
						return field.getName();
				}
			}
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		
		return "";
	}

	protected abstract void markObjectDirty();
	
	protected void markDirty(ObservableValue<?> property, Object oldValue, Object newValue) {
		String propertyName = getPropertyName(property);
		
		String[] old = dirty.get(propertyName);
		String from = old != null ? old[0].toString() : oldValue.toString();
		String to = newValue.toString();
		dirty.put(propertyName, new String[] {from, to});
	}
	
	public void clean() {
		dirty.clear();
	}
	
	public boolean isDirty() {
		return dirty.size() > 0;
	}
}
