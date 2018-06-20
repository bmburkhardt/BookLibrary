package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.AuthorBook;
import application.model.Book;
import application.model.ReportObject;
import application.util.ConnectionFactory;
import application.util.exception.AppException;

public class ReportObjectTableGateway extends TableGateway<ReportObject> {

	private static Logger logger = LogManager.getLogger(ReportObjectTableGateway.class);
	
	private Connection conn = null;

	@Override
	public void add(ReportObject obj) throws AppException {
		// Does not implement
	}

	@Override
	public void update(ReportObject obj) throws AppException {
		// Does not implement
	}

	@Override
	public List<ReportObject> get(int page, int pageSize) throws AppException {
		// Does not implement
		return null;
	}

	@Override
	public void delete(ReportObject obj) throws AppException {
		// Does not implement
	}
	
	public List<ReportObject> getSets(int pubId) throws AppException {
		List<ReportObject> reportObjects = new ArrayList<ReportObject>();
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("select b.title, b.isbn, ab.royalty, a.first_name, a.last_name " +
					 				   "from book b, authors a, author_book ab where b.publisher_id = ? " +
					 				   "and ab.book_id = b.id and a.id = ab.author_id order by b.title");
			ps.setInt(1, pubId);
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				String title = rs.getString("b.title");
				String isbn = rs.getString("b.isbn");
				String author = (rs.getString("a.first_name") + " " + rs.getString("a.last_name"));
				//int royalty = (rs.getInt("ab.royalty"));
				double royalty = (rs.getFloat("ab.royalty"));
				ReportObject reportObject = new ReportObject();
				reportObject.setTitle(title);
				reportObject.setISBN(isbn);
				reportObject.setAuthor(author);
				reportObject.setRoyalty(royalty);
				reportObject.setGateway(this);
				reportObjects.add(reportObject);
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("Report objects table fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("Report objects table fetch failed");
			}
		}
		
		return reportObjects;
	}

}
