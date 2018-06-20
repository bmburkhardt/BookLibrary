package application.model;

import java.text.NumberFormat;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.gateway.AuthorBookTableGateway;
import application.model.gateway.TableGateway;
import application.util.exception.AppException;
import application.util.exception.DuplicateKeyException;
import application.view.ViewManager;
import javafx.beans.property.SimpleObjectProperty;

public class AuthorBook extends Model {
	private static Logger logger = LogManager.getLogger(AuthorBook.class);
	private AuthorBookTableGateway gateway;
	
	private Author author;
	private Book book;
	private int royaltyInt;
	private SimpleObjectProperty<String> royalty;
	//private boolean newRecord = true;
	private SimpleObjectProperty<Boolean> newRecord;

	public AuthorBook() {
		royalty = new SimpleObjectProperty<String>();
		newRecord = new SimpleObjectProperty<Boolean>();
	}
	
	public AuthorBook(Author author, Book book, int royalty) {
		this();
		setAuthor(author);
		setBook(book);
		setRoyalty(royalty);
		setNewRecord(false);
	}

	@Override
	public void save() {
		try {
			logger.info("save called");
			if(validate()) {
				logger.info("Validated all input fields!");
				
				gateway.add(this);
			}
		} catch (DuplicateKeyException e) {
			logger.info("duplicate, need to update");
				
			try {
				gateway.update(this);
				book.createAudit("Royalty changed to " + (royaltyInt / 100000.f));
			} catch (AppException e1) {
				ViewManager.showError(e1);
			}
		} catch (AppException e) {
			ViewManager.showError(e);
		}
	}

	@Override
	public boolean validate() throws AppException {
		if(!validateRoyalty())
			throw new AppException("Royalty must be between 0.0 and 1.0");
		
		return true;
	}
	
	public boolean validateRoyalty()  {
		return royaltyInt > 0 && royaltyInt < 100000;
	}
	
	@Override
	public String toString() {
		return this.getAuthor().getFullName() + " authored " + this.getBook().getTitle();
	}

	@Override
	public Integer getId() {
		return null; //does not implement
	}

	@Override
	public void setId(Integer id) {
		//does not implement
	}

	public Book getBook() {
		return book;
	}

	public void setBook(Book book) {
		this.book = book;
	}

	public Author getAuthor() {
		return author;
	}

	public void setAuthor(Author author) {
		this.author = author;
	}

	public int getRoyalty() {
		return royaltyInt;
	}

	public void setRoyalty(int royalty) {
		royaltyInt = royalty;
		
	    NumberFormat percentFormatter = NumberFormat.getPercentInstance();
	    percentFormatter.setMinimumFractionDigits(1);
	    percentFormatter.setMaximumFractionDigits(4);
	    this.royalty.set(percentFormatter.format(royalty / 100000.f));
	}

	public boolean isNewRecord() {
		return newRecord.get();
	}

	public void setNewRecord(boolean newRecord) {
		this.newRecord.set(newRecord);
	}
	
	// property accessors
	
	public SimpleObjectProperty<String> royaltyProperty() {
		return royalty;
	}
	
	public SimpleObjectProperty<Boolean> newRecordProperty() {
		return newRecord;
	}

	@Override
	public AuthorBookTableGateway getGateway() {
		return this.gateway;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (AuthorBookTableGateway)gateway;
	}
}
