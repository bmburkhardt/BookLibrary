package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.Author;
import application.model.AuthorBook;
import application.model.Book;
import application.util.ConnectionFactory;
import application.util.exception.AppException;
import application.util.exception.DuplicateKeyException;

public class AuthorBookTableGateway extends TableGateway<AuthorBook> {

	private static Logger logger = LogManager.getLogger(AuthorBookTableGateway.class);
	
	private Connection conn = null;

	@Override
	public void add(AuthorBook obj) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("INSERT INTO author_book(author_id, book_id, royalty) VALUES(?, ?, ?)");
			ps.setInt(1, obj.getAuthor().getId());
			ps.setInt(2, obj.getBook().getId());
			ps.setFloat(3, obj.getRoyalty() / 100000.0f);
	        int affectedRows = ps.executeUpdate();

	        if (affectedRows == 0) {
	            throw new SQLException("Creating author_book failed, no rows affected.");
	        }
			
			logger.info("added author " + obj.getAuthor().getFullName() + " to book " + obj.getBook().getTitle());
		} catch (SQLException e) {
			if(e.getMessage().startsWith("Duplicate entry"))
				throw new DuplicateKeyException(e.getMessage());
			else {
				e.printStackTrace();
				throw new AppException("Book insert failed: " + e.getMessage());
			}
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
	public void update(AuthorBook obj) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("UPDATE author_book SET royalty = ? WHERE author_id = ? and book_id = ?");
			ps.setFloat(1, obj.getRoyalty() / 100000.0f);
			ps.setInt(2, obj.getAuthor().getId());
			ps.setInt(3, obj.getBook().getId());
			ps.executeUpdate();
			
			logger.info("updated author royalty for " + obj.getAuthor().getFullName() + " to " + obj.getRoyalty() / 100000.0f);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Royalty update failed: " + e.getMessage());
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Royalty update failed");
			}
		}
		//throw new AppException("Does not implement update, make a new AuthorBook record.");
	}

	@Override
	public List<AuthorBook> get(int page, int pageSize) throws AppException {
		List<AuthorBook> authorBooks = new ArrayList<>();
		
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			String sql = "select * from author_book ab " +
						 "inner join authors a on (ab.author_id = a.id) " +
						 "inner join book b on (ab.book_id = b.id) " +
						 "inner join publisher p on (b.publisher_id = p.id) ";
			
			ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery(); 
		    
			while (rs.next()) {
				Author author = Author.create(rs, "a.");
				Book book = Book.create(rs, "b.");
				int royalty = (int)(rs.getFloat("royalty") * 100000);
				
				logger.debug(author);
				logger.debug(book);
				
				AuthorBook authorBook = new AuthorBook(author, book, royalty);
				authorBook.setGateway(this);
				authorBooks.add(authorBook);
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
		
		return authorBooks;
	}

	@Override
	public void delete(AuthorBook obj) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			
			ps = conn.prepareStatement("delete from author_book where author_id = ? and book_id = ?");
			ps.setInt(1, obj.getAuthor().getId());
			ps.setInt(2, obj.getBook().getId());
			
			int count = ps.executeUpdate();
			if(count == 0)
				throw new AppException("Delete failed, AuthorBook was not found in DB");
		}  catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("AuthorBook delete failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("AuthorBook delete failed");
			}
		}
	}

}
