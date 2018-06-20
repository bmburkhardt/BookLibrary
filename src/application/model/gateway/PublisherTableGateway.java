package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.Book;
import application.model.Publisher;
import application.util.ConnectionFactory;
import application.util.exception.AppException;

public class PublisherTableGateway extends TableGateway<Publisher> {
	
	private static Logger logger = LogManager.getLogger(PublisherTableGateway.class);
	
	private Connection conn = null;
	
	public PublisherTableGateway() { }
	
	public PublisherTableGateway(Connection conn) {
		this.conn = conn;
	}
	
	@Override
	public void add(Publisher obj) throws AppException {
		throw new AppException("Gateway does not implement add functionality.");
	}

	@Override
	public void update(Publisher obj) throws AppException {
		throw new AppException("Gateway does not implement update functionality.");
	}

	@Override
	public List<Publisher> get(int page, int pageSize) throws AppException {
		List<Publisher> publishers = new ArrayList<Publisher>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			ps = conn.prepareStatement("select * from publisher");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String publisherName = rs.getString("publisher_name");
				
				Publisher publisher = new Publisher(publisherName);
				publisher.setId(id);
				publisher.setGateway(this);
				publishers.add(publisher);
			}
			logger.debug("publishers: " + publishers);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Publisher table fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Publisher table fetch failed");
			}
		}
		
		return publishers;
	}
	
	public List<Publisher> getPublishers() throws AppException {
		List<Publisher> publishers = new ArrayList<Publisher>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			ps = conn.prepareStatement("select * from publisher");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String publisherName = rs.getString("publisher_name");
				
				Publisher publisher = new Publisher(publisherName);
				publisher.setId(id);
				publisher.setGateway(this);
				publishers.add(publisher);
			}
			logger.debug("publishers: " + publishers);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Publisher table fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Publisher table fetch failed");
			}
		}
		
		return publishers;
	}
	
	
	public Publisher getById(int id) {
		return null;
	}

	@Override
	public void delete(Publisher obj) throws AppException {
		throw new AppException("Gateway does not implement delete functionality.");
	}

}
