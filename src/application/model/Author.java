package application.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.audits.AuditTrailEntry;
import application.model.audits.AuditTrailModel;
import application.model.gateway.AuditTrailTableGateway;
import application.model.gateway.AuthorTableGateway;
import application.model.gateway.TableGateway;
import application.util.exception.AppException;
import application.view.ViewManager;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Author extends AuditTrailModel<Author> {
	private static Logger logger = LogManager.getLogger(Author.class);
	private AuthorTableGateway gateway;
	
	private Integer id;
	private SimpleStringProperty firstName;
	private SimpleStringProperty lastName;
	private SimpleStringProperty gender;
	private SimpleStringProperty website;
	private SimpleObjectProperty<LocalDate> dateOfBirth;
	private SimpleObjectProperty<LocalDateTime> lastModified;

	private AuditTrailTableGateway<Author> auditGateway;
	
	public Author() {
		firstName = new SimpleStringProperty();
		lastName = new SimpleStringProperty();
		dateOfBirth = new SimpleObjectProperty<LocalDate>();
		gender = new SimpleStringProperty();
		website = new SimpleStringProperty();
		lastModified = new SimpleObjectProperty<LocalDateTime>();
		id = null;

		auditGateway = new AuditTrailTableGateway<>();
		setupListeners();
	}
	
	public Author(String firstName, String lastName, LocalDate dob, String gender, String website) {
		this();
		setFirstName(firstName);
		setLastName(lastName);
		setDateOfBirth(dob);
		setGender(gender);
		setWebsite(website);
		//setLastModified(null);
	}
	
	public static Author create(ResultSet rs, String alias) throws SQLException {
		if(alias == null)
			alias = "";
		
		int id = rs.getInt(alias + "id");
		String firstName = rs.getString(alias + "first_name");
		String lastName = rs.getString(alias + "last_name");
		LocalDate dateOfBirth = rs.getDate(alias + "dob").toLocalDate();
		String gender = rs.getString(alias + "gender");
		String webSite = rs.getString(alias + "web_site");
		Timestamp ts = rs.getTimestamp("last_modified");

		Author author = new Author(firstName, lastName, dateOfBirth, gender, webSite);
		author.setId(id);
		author.setLastModified(ts.toLocalDateTime());
		return author;
	}

	@Override
	protected void setupListeners() {
		firstName.addListener(changeListener);
		lastName.addListener(changeListener);
		dateOfBirth.addListener(changeListener);
		gender.addListener(changeListener);
		website.addListener(changeListener);
	}
	
	@Override
	public void save() {
		try {
			logger.info("save called");
			if(validate()) {
				logger.info("Validated all input fields!");

				if(this.getId() == null) {
					gateway.add(this);
					
					markObjectDirty();
				} else {
					if(!this.isDirty())
						logger.info("author hasn't been changed, nothing to update");
					else
						gateway.update(this);
					
					saveChanges();
				}
			}
		} catch (AppException e) {				
			e.printStackTrace();
			ViewManager.showError(e);
		}
	}
	
	@Override
	public String toString() {
		return getFullName();
	}
	
	//validators
	
	@Override
	public boolean validate() throws AppException {
		if(!validateId())
			throw new AppException("Invalid Id.");
		if(!validateFirstName()) 
			throw new AppException("Invalid first name.");
		if(!validateLastName()) 
			throw new AppException("Invalid last name.");
		if(!validateDateOfBirth())
			throw new AppException("Invalid date of birth.");
		if(!validateGender())
			throw new AppException("Invalid gender.");
		if(!validateWebsite())
			throw new AppException("Invalid website.");
		if(!validateTimestamp())
			throw new AppException("Error: The author has recently been changed. Please reload the author list.");
		
		return true;
	}
	
	public boolean validateId() {
		return id == null || id >= 0; //potentially null, but still valid for new users
	}

	public boolean validateFirstName() {
		String firstName = getFirstName();
		return firstName != null && firstName.length() <= 100 && !firstName.trim().equals("");	
	}
	
	public boolean validateLastName() {
		String lastName = getLastName();
		return lastName != null && lastName.length() <= 100 && !lastName.trim().equals("");	
	}
	
	public boolean validateDateOfBirth() {
		LocalDate dob = getDateOfBirth();
		return dob != null && dob.isBefore(LocalDate.now());
	}
	
	public boolean validateGender() {
		String gender = getGender().toLowerCase();
		return gender.equals("male") || gender.equals("female") || gender.equals("unknown");
	}
	
	public boolean validateWebsite() {
		String website = getWebsite();
		return website == null || website.length() <= 100;	
	}
	
	public boolean validateTimestamp() {
		if(id == null) // unsaved, automatically okay
			return true;
		
		LocalDateTime originalTimestamp = getLastModified();
		try {
			LocalDateTime currentTimestamp = gateway.getAuthorLastModifiedById(id);
			logger.info("o ts:" + originalTimestamp);
			logger.info("c ts:" + currentTimestamp);
			return currentTimestamp.equals(originalTimestamp);
		} catch (AppException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	// accessors
	
	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getAuditTrailTableName() {
		return "author_audit_trail";
	}
	
	public String getAuditTrailRefId() {
		return "author_id";
	}
	
	public String getFirstName() {
		return firstName.get();
	}
	
	public void setFirstName(String firstName) {
		this.firstName.set(firstName);
	}
	
	public String getLastName() {
		return lastName.get();
	}
	
	public void setLastName(String lastName) {
		this.lastName.set(lastName);
	}
	
	public String getFullName() {
		return getFirstName() + " " + getLastName();
	}

	public LocalDate getDateOfBirth() {
		return this.dateOfBirth.get();
	}
	
	public void setDateOfBirth(LocalDate date) {
		this.dateOfBirth.set(date);
	}
	
	public LocalDateTime getLastModified() {
		return this.lastModified.get();
	}
	
	public void setLastModified(LocalDateTime ldt) {
		this.lastModified.set(ldt);
	}
	public String getGender() {
		return gender.get();
	}
	
	public void setGender(String gender) {
		this.gender.set(gender);
	}
	
	public String getWebsite() {
		return website.get();
	}
	
	public void setWebsite(String website) {
		this.website.set(website);
	}
	
	// property accessors
	
	public SimpleStringProperty firstNameProperty() {
		return firstName;
	}
	
	public SimpleStringProperty lastNameProperty() {
		return lastName;
	}
	
	public SimpleObjectProperty<LocalDate> dateOfBirthProperty() {
		return dateOfBirth;
	}
	
	public SimpleStringProperty genderProperty() {
		return gender;
	}
	
	public SimpleStringProperty websiteProperty() {
		return website;
	}
	
	public SimpleObjectProperty<LocalDateTime> lastModifiedProperty() {
		return lastModified;
	}
	
	// gateway accessors

	@Override
	public TableGateway<Author> getGateway() {
		return this.gateway;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (AuthorTableGateway)gateway;
	}

	@Override
	public List<AuditTrailEntry<Author>> getAudits() throws AppException {
		List<AuditTrailEntry<Author>> audits = null;
		
		audits = this.gateway.getAuditTrail(this);
		for(AuditTrailEntry<Author> entry : audits)
			entry.setGateway(auditGateway);
			
		return audits;
	}
	
	@Override
	public AuditTrailTableGateway<Author> getAuditGateway() {
		return this.auditGateway;
	}
	
	@Override
	protected void markObjectDirty() {
		clean();
		createAudit("Author added");
	}
}
