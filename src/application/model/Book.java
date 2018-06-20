package application.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.audits.AuditTrailEntry;
import application.model.audits.AuditTrailModel;
import application.model.gateway.AuditTrailTableGateway;
import application.model.gateway.AuthorTableGateway;
import application.model.gateway.BookTableGateway;
import application.model.gateway.TableGateway;
import application.util.exception.AppException;
import application.view.ViewManager;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;

public class Book extends AuditTrailModel<Book> {
	private static Logger logger = LogManager.getLogger(Book.class);
	private BookTableGateway gateway;
	
	private Integer id;
	private SimpleStringProperty title;
	private SimpleStringProperty summary;
	private SimpleIntegerProperty yearPublished;
	private SimpleObjectProperty<Publisher> publisher;
	private SimpleStringProperty isbn;
	private SimpleObjectProperty<LocalDate> dateAdded;

	private AuthorTableGateway authorGateway;
	private AuditTrailTableGateway<Book> auditGateway;
	
	public Book() {
		title = new SimpleStringProperty();
		summary = new SimpleStringProperty();
		yearPublished = new SimpleIntegerProperty();
		publisher = new SimpleObjectProperty<Publisher>();
		isbn = new SimpleStringProperty();
		dateAdded = new SimpleObjectProperty<LocalDate>();
		id = null;
		
		authorGateway = new AuthorTableGateway();
		auditGateway = new AuditTrailTableGateway<>();
		setupListeners();
	}
	
	public Book(String title, String summary, int yearPublished, Publisher publisher, String isbn, LocalDate dateAdded) {
		this();
		setTitle(title);
		setSummary(summary);
		setYearPublished(yearPublished);
		setPublisher(publisher);
		setISBN(isbn);
		setDateAdded(dateAdded);
	}
	
	public static Book create(ResultSet rs, String alias) throws SQLException {
		if(alias == null)
			alias = "";
		
		int id = rs.getInt(alias + "id");
		String title = rs.getString(alias + "title");
		String summary = rs.getString(alias + "summary");
		int yearPublished = rs.getInt(alias + "year_published");
		String isbn = rs.getString(alias + "isbn");
		Date dateAdded = rs.getDate(alias + "date_added");
		
		Publisher publisher = Publisher.create(rs, "p.");

		Book book = new Book(title, summary, yearPublished, publisher, isbn, dateAdded.toLocalDate());
		book.setId(id);
		return book;
	}
	
	@Override
	protected void setupListeners() {
		title.addListener(changeListener);
		summary.addListener(changeListener);
		yearPublished.addListener(changeListener);
		publisher.addListener(changeListener);
		isbn.addListener(changeListener);
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
						logger.info("book hasn't been changed, nothing to update");
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
		return getTitle();
	}
	
	//validators

	@Override
	public boolean validate() throws AppException {
		if(!validateId())
			throw new AppException("Invalid Id.");
		if(!validateTitle())
			throw new AppException("Invalid title.");
		if(!validateSummary()) 
			throw new AppException("Invalid summary.");
		if(!validateYearPublished()) 
			throw new AppException("Invalid year published.");
		if(!validateISBN())
			throw new AppException("Invalid isbn.");
		
		return true;
	}
	
	public boolean validateId() {
		return id == null || id >= 0; //potentially null, but still valid for new users
	}
	
	public boolean validateTitle() {
		String title = getTitle();
		return title != null && title.length() <= 255 && !title.trim().equals("");	
	}
	
	public boolean validateSummary() {
		String summary = getSummary();
		return summary != null && summary.length() <= 65536;
	}
	
	public boolean validateYearPublished() {
		Integer yearPublished = getYearPublished();
		return yearPublished != null && yearPublished <= LocalDateTime.now().getYear();
	}
	
	public boolean validateISBN() {
		String isbn = getISBN();
		return isbn != null && isbn.length() <= 13;
	}
	
	// accessors
	@Override
	public Integer getId() {
		return id;
	}
	
	@Override
	public void setId(Integer id) {
		this.id = id;
	}
	
	public String getAuditTrailTableName() {
		return "book_audit_trail";
	}

	public String getAuditTrailRefId() {
		return "book_id";
	}
	
	public String getTitle() {
		return title.get();
	}
	
	public void setTitle(String title) {
		this.title.set(title);
	}
	
	public String getSummary() {
		return summary.get();
	}
	
	public void setSummary(String summary) {
		this.summary.set(summary);
	}
	
	public int getYearPublished() {
		return yearPublished.get();
	}
	
	public void setYearPublished(int yearPublished) {
		this.yearPublished.set(yearPublished);
	}
	
	public Publisher getPublisher() {
		return publisher.get();
	}
	
	public void setPublisher(Publisher publisher) {
		this.publisher.set(publisher);
	}
	
	public String getISBN() {
		return isbn.get();
	}
	
	public void setISBN(String isbn) {
		this.isbn.set(isbn);
	}
	
	public LocalDate getDateAdded() {
		return dateAdded.get();
	}
	
	public void setDateAdded(LocalDate dateAdded) {
		this.dateAdded.set(dateAdded);
	}
	
	// property accessors

	public SimpleStringProperty titleProperty() {
		return title;
	}
	
	public SimpleStringProperty summaryProperty() {
		return summary;
	}
	
	public SimpleIntegerProperty yearPublishedProperty() {
		return yearPublished;
	}
	
	public SimpleObjectProperty<Publisher> publisherProperty() {
		return publisher;
	}
	
	public SimpleStringProperty isbnProperty() {
		return isbn;
	}
	
	public SimpleObjectProperty<LocalDate> dateAddedProperty() {
		return dateAdded;
	}
	
	public List<Author> getAuthors() throws AppException {
		List<Author> authors = this.gateway.getAuthors(this);
		
		for(Author author : authors)
			author.setGateway(authorGateway);
		
		return authors;
	}
	
	public List<AuthorBook> getAuthorBooks() throws AppException {
		return this.gateway.getAuthorBooks(this);
	}
	
	@Override
	public List<AuditTrailEntry<Book>> getAudits() throws AppException {
		List<AuditTrailEntry<Book>> audits = null;
		
		audits = this.gateway.getAuditTrail(this);
		for(AuditTrailEntry<Book> entry : audits)
			entry.setGateway(auditGateway);
			
		return audits;
	}
	
	@Override
	public AuditTrailTableGateway<Book> getAuditGateway() {
		return this.auditGateway;
	}
	
	// gateway accessors

	@Override
	public BookTableGateway getGateway() {
		return this.gateway;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (BookTableGateway)gateway;
	}
	
	@Override
	protected void markObjectDirty() { 
		clean();
		createAudit("Book added");
	}
}
