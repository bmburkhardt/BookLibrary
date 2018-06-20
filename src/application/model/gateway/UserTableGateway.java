package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.User;
import application.util.ConnectionFactory;
import application.util.exception.AppException;

public class UserTableGateway extends TableGateway<User> {
	private static Logger logger = LogManager.getLogger(UserTableGateway.class);
	
	private Connection conn = null;

	@Override
	public void add(User obj) throws AppException {
		throw new AppException("user gateway does not implement get");
	}

	@Override
	public void update(User obj) throws AppException {
		throw new AppException("user gateway does not implement get");
	}

	@Override
	public List<User> get(int page, int pageSize) throws AppException {
		throw new AppException("user gateway does not implement get");
	}

	@Override
	public void delete(User obj) throws AppException {
		throw new AppException("user gateway does not implement get");
	}

	public User fetch(int userId) throws AppException {
		PreparedStatement ps = null;
		try {
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("select * from user u inner join role r on (u.role_id = r.id) where u.id = ? limit 1");
			ps.setInt(1, userId);
			
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				User user = User.create(rs, null);
				logger.info("found user " + user.getUsername());
				user.setGateway(this);
				return user;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("User fetch failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("User fetch failed");
			}
		}
		
		return null;
	}
}
