package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.Author;
import application.model.audits.AuditTrailEntry;
import application.util.ConnectionFactory;
import application.util.exception.AppException;

public class AuthorTableGateway extends TableGateway<Author> {
	private static Logger logger = LogManager.getLogger(AuthorTableGateway.class);
	
	private Connection conn = null;
	
	public AuthorTableGateway() { }
	
	public AuthorTableGateway(Connection conn) {
		this.conn = conn;
	}

	
	public void add(Author author) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("INSERT INTO authors(id, first_name, last_name, dob, gender, web_site) VALUES(NULL, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, author.getFirstName());
			ps.setString(2, author.getLastName());
			ps.setDate(3, java.sql.Date.valueOf(author.getDateOfBirth()));
			ps.setString(4, author.getGender());
			ps.setString(5, author.getWebsite());
			int affectedRows = ps.executeUpdate();

	        if (affectedRows == 0) {
	            throw new SQLException("Creating author failed, no rows affected.");
	        }

	        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
	        		if (generatedKeys.next()) {
					int id = (int)generatedKeys.getLong(1);
					logger.info("updating new author id reference to " + id);
					author.setId(id);
        			} else {
        				throw new SQLException("Creating author failed, no ID obtained.");
    				}
	        }
			
			logger.info("addAuthor executed for: " + author.getFullName());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("addAuthor failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("addAuthor failed");
			}
		}
	}
	
	public void update(Author author) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("UPDATE authors SET first_name = ?, last_name = ?, dob = ?, gender = ?, web_site = ? where id = ?");
			ps.setString(1, author.getFirstName());
			ps.setString(2, author.getLastName());
			ps.setDate(3, java.sql.Date.valueOf(author.getDateOfBirth()));
			ps.setString(4, author.getGender());
			ps.setString(5, author.getWebsite());
			ps.setInt(6, author.getId());
			ps.executeUpdate();
			
			author.setLastModified(this.getAuthorLastModifiedById(author.getId()));
			
			logger.info("updateAuthor executed for: " + author.getFullName());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("updateAuthor failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("updateAuthor failed");
			}
		}
	}
	
	public List<Author> get(int page, int pageSize) throws AppException {
		List<Author> authors = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			ps = conn.prepareStatement("select * from authors order by last_name, first_name");
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Author author = Author.create(rs, null);
				author.setGateway(this);
				authors.add(author);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Author table fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Author table fetch failed");
			}
		}
		
		return authors;
	}
	
	public void delete(Author author) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			ps = conn.prepareStatement("delete from authors where id = ?");
			ps.setInt(1, author.getId());

			int count = ps.executeUpdate();
			if(count == 0)
				throw new AppException("Delete failed, author was not found in DB");
		}  catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Author delete failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Author delete failed");
			}
		}
	}

	public List<AuditTrailEntry<Author>> getAuditTrail(Author author) throws AppException {
		List<AuditTrailEntry<Author>> audits = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
		
			ps = conn.prepareStatement("select * from author_audit_trail where author_id = ? order by date_added ASC");
			ps.setInt(1, author.getId());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				Timestamp dateAdded = rs.getTimestamp("date_added");
				String message = rs.getString("entry_msg");
				
				AuditTrailEntry<Author> audit = new AuditTrailEntry<>(message, dateAdded.toLocalDateTime());
				audit.setId(id);
				audit.setModel(author);
				audits.add(audit);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("AuditTrail fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("AuditTrail fetch failed");
			}
		}
		
		return audits;
	}
	
	public LocalDateTime getAuthorLastModifiedById(int id) throws AppException {
		LocalDateTime date = null;
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("select * from authors where id = ?");
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			rs.next();
			Timestamp ts = rs.getTimestamp("last_modified");
			date = ts.toLocalDateTime();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Get last author modify failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Get last author modify failed");
			}
		}
		return date;
	}
	
	
}
