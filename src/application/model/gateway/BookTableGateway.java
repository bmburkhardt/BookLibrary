package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.Author;
import application.model.AuthorBook;
import application.model.Book;
import application.model.Publisher;
import application.model.audits.AuditTrailEntry;
import application.util.ConnectionFactory;
import application.util.exception.AppException;

public class BookTableGateway extends TableGateway<Book> {

	private static Logger logger = LogManager.getLogger(BookTableGateway.class);
	
	private Connection conn = null;
	private AuthorBookTableGateway authorBookGateway = null;
	
	public BookTableGateway() {
		authorBookGateway = new AuthorBookTableGateway();
	}

	@Override
	public void add(Book book) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("INSERT INTO book(id, title, summary, year_published, publisher_id, isbn) VALUES(NULL, ?, ?, ?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, book.getTitle());
			ps.setString(2, book.getSummary());
			ps.setInt(3, book.getYearPublished());
			ps.setInt(4, book.getPublisher().getId());
			ps.setString(5, book.getISBN());
	        int affectedRows = ps.executeUpdate();

	        if (affectedRows == 0) {
	            throw new SQLException("Creating book failed, no rows affected.");
	        }

	        try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
	        		if (generatedKeys.next()) {
					int id = (int)generatedKeys.getLong(1);
					logger.info("updating new book id reference to " + id);
					book.setId(id);
        			} else {
        				throw new SQLException("Creating book failed, no ID obtained.");
    				}
	        }
			
			logger.info("addBook executed for: " + book.getTitle());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Book insert failed: " + e.getMessage());
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Book insert failed");
			}
		}
	}

	@Override
	public void update(Book book) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("UPDATE book SET title = ?, summary = ?, year_published = ?, publisher_id = ?, isbn = ? where id = ?");
			ps.setString(1, book.getTitle());
			ps.setString(2, book.getSummary());
			ps.setInt(3,  book.getYearPublished());
			ps.setInt(4, book.getPublisher().getId());
			ps.setString(5, book.getISBN());
			ps.setInt(6, book.getId());
			ps.executeUpdate();
			
			logger.info("updateBook executed for: " + book.getTitle());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("updateBook failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("updateBook failed");
			}
		}	
	}

	@Override
	public List<Book> get(int page, int pageSize) throws AppException {
		return search(null, page, pageSize);
	}
	
	public List<Book> search(String searchVar, int page, int pageSize) throws AppException {
		List<Book> books = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
		
			String query = "select * from book b inner join publisher p on (b.publisher_id = p.id)";
			if(searchVar != null)
				query += " where title like ?";
			query += " order by b.title asc";
			query += " limit " + pageSize + " offset " + (pageSize * page);
			
			ps = conn.prepareStatement(query);
			if(searchVar != null)
				ps.setString(1,  "%" + searchVar + "%");
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Book book = Book.create(rs, null);
				book.setGateway(this);
				books.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Book table fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Book table fetch failed");
			}
		}
		
		return books;
	}
	
	public List<Book> getPublisherBooks(Publisher pub) throws AppException {
		List<Book> books = new ArrayList<Book>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			ps = conn.prepareStatement("select * from book where publisher_id = ?");
			ps.setInt(1, pub.getId());
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				String title = rs.getString("title");
				String isbn = rs.getString("isbn");
				
				Book book = new Book();
				book.setId(id);
				book.setTitle(title);
				book.setISBN(isbn);
				book.setGateway(this);
				books.add(book);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Publisher books table fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Publisher books table fetch failed");
			}
		}
		
		return books;
	}
	
	public int maxNumberRecords(String searchVar) {
		int rv = 0;
		conn = ConnectionFactory.createConnection();
		
		try {
			Statement statement = conn.createStatement();
			ResultSet rs;
			if(searchVar != null) {
				rs = statement.executeQuery("select COUNT(*) from book where title like '%" + searchVar + "%'");
			}
			else {
				rs = statement.executeQuery("select COUNT(*) from book");
			}
			rs.next();
			rv = rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return rv;
	}

	@Override
	public void delete(Book book) throws AppException {
		PreparedStatement ps = null;
		PreparedStatement ps1 = null;
		try {
			conn = ConnectionFactory.createConnection();

			ps1 = conn.prepareStatement("delete from book_audit_trail where book_id = ?");
			ps1.setInt(1, book.getId());
			ps1.executeUpdate();
			
			ps = conn.prepareStatement("delete from book where id = ?");
			ps.setInt(1, book.getId());
			int count = ps.executeUpdate();
			if(count == 0)
				throw new AppException("Delete failed, book was not found in DB");
		}  catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Book delete failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Book delete failed");
			}
		}
		
	}
	
	public List<AuditTrailEntry<Book>> getAuditTrail(Book book) throws AppException {
		List<AuditTrailEntry<Book>> audits = new ArrayList<>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
		
			ps = conn.prepareStatement("select * from book_audit_trail where book_id = ? order by date_added ASC");
			ps.setInt(1, book.getId());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				int id = rs.getInt("id");
				Timestamp dateAdded = rs.getTimestamp("date_added");
				String message = rs.getString("entry_msg");
				
				AuditTrailEntry<Book> audit = new AuditTrailEntry<>(message, dateAdded.toLocalDateTime());
				audit.setId(id);
				audit.setModel(book);
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
	
	public List<Author> getAuthors(Book book) throws AppException {
		List<AuthorBook> authorBooks = getAuthorBooks(book);

		List<Author> authors = new ArrayList<>();
		if(book.getId() == null)
			return authors;
		
		for(AuthorBook ab : authorBooks)
			authors.add(ab.getAuthor());
		
		return authors;
	}
	
	public List<AuthorBook> getAuthorBooks(Book book) throws AppException {
		List<AuthorBook> authorBooks = new ArrayList<>();
		if(book.getId() == null)
			return authorBooks;
		
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
		
			String sql = "select * from author_book ab " +
						 "inner join authors a on (ab.author_id = a.id) " +
						 "where book_id = ? " +
						 "order by last_name ASC ";
			
			ps = conn.prepareStatement(sql);
			ps.setInt(1, book.getId());
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				Author author = Author.create(rs, "a.");
				int royalty = (int)(rs.getFloat("royalty") * 100000);
				
				logger.debug(author);
				
				AuthorBook authorBook = new AuthorBook(author, book, royalty);
				authorBook.setGateway(authorBookGateway);
				authorBooks.add(authorBook);
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
		
		return authorBooks;
	}
}
