package application.model;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.gateway.BookTableGateway;
import application.model.gateway.ReportObjectTableGateway;
import application.model.gateway.TableGateway;
import application.util.exception.AppException;

public class ReportObject extends Model {
	
	private static Logger logger = LogManager.getLogger(ReportObject.class);
	private ReportObjectTableGateway gateway;

	private String title;
	private String isbn;
	private String author;
	private double royalty;

	public ReportObject() {
	}
	
//	public Role(String name) {
//		this();
//		setName(name);
//	}

	
	@Override
	public void save() {
		logger.warn("ReportObject does not implement saving");
	}

	@Override
	public boolean validate() throws AppException {
		throw new AppException("ReportObject does not implement validate");
	}
	
	@Override
	public String toString() {
		return getTitle();
	}

	@Override
	public Integer getId() {
		return null;
	}

	@Override
	public void setId(Integer id) {
		// does not implement
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getISBN() {
		return isbn;
	}

	public void setISBN(String isbn) {
		this.isbn = isbn;
	}
	
	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}
	
	public double getRoyalty() {
		return royalty;
	}

	public void setRoyalty(double royalty) {
		this.royalty = royalty;
	}
	
	/* gateway stuff */
	
	@Override
	public ReportObjectTableGateway getGateway() {
		return this.gateway;
	}

	@Override
	public void setGateway(TableGateway<?> gateway) {
		this.gateway = (ReportObjectTableGateway)gateway;
	}

}
