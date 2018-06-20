package application.model.gateway;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.audits.AuditTrailEntry;
import application.model.audits.AuditTrailModel;
import application.util.ConnectionFactory;
import application.util.exception.AppException;

public class AuditTrailTableGateway<T extends AuditTrailModel<?>> extends TableGateway<AuditTrailEntry<T>> {
	private static Logger logger = LogManager.getLogger(AuditTrailTableGateway.class);
	
	private Connection conn = null;
	
	public AuditTrailTableGateway() {}

	@Override
	public void add(AuditTrailEntry<T> obj) throws AppException {
		PreparedStatement ps = null;
		try {
			AuditTrailModel<?> model = obj.getModel();
			conn = ConnectionFactory.createConnection();
			ps = conn.prepareStatement("INSERT INTO " + model.getAuditTrailTableName() + "(id, " + model.getAuditTrailRefId() + ", entry_msg) VALUES(NULL, ?, ?)");
			ps.setInt(1, obj.getModel().getId());
			ps.setString(2, obj.getMessage());
			ps.executeUpdate();

			logger.info("saving audit: " + obj.getMessage());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new AppException("AuditTrailEntry save failed");
		} finally {
			try {
				if(ps != null)
					ps.close();
				
				if(conn != null)
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
				throw new AppException("AuditTrailEntry save failed");
			}
		}
	}

	@Override
	public void update(AuditTrailEntry<T> obj) throws AppException {
		throw new AppException("Gateway does not implement update functionality.");
	}

	@Override
	public List<AuditTrailEntry<T>> get(int page, int pageSize) throws AppException {
		throw new AppException("Gateway does not implement get functionality, Book class implements this fetch.");
	}

	@Override
	public void delete(AuditTrailEntry<T> obj) throws AppException {
		throw new AppException("Gateway does not implement delete functionality.");
	}

}
