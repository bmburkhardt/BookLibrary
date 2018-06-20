package application.controller;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.model.Author;
import application.model.Book;
import application.model.audits.AuditTrailEntry;
import application.model.audits.AuditTrailModel;
import application.model.gateway.PublisherTableGateway;
import application.util.exception.AppException;
import application.view.AppView;
import application.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class AuditTrailController<T extends AuditTrailModel<?>> extends ViewController {
	private static Logger logger = LogManager.getLogger(AuditTrailController.class);

	@FXML private Label titleLabel;
	@FXML private TableView<AuditTrailEntry<T>> tableView;
	@FXML private Button backButton;

	private AuditTrailModel<?> model;
	private ObservableList<AuditTrailEntry<T>> audits;

	public AuditTrailController() {
	}

	public AuditTrailController(AuditTrailModel<?> model, List<AuditTrailEntry<T>> audits) {
		this();

		this.model = model;
		this.audits = FXCollections.observableArrayList(audits);
	}

	@FXML
	void onBackPressed(ActionEvent event) {
		logger.info("back button pressed");
		try {
			// navigate back to BOOK_DETAIL_VIEW
			ViewManager vm = ViewManager.getInstance();
			if(model instanceof Book) {
				Book book = (Book)model;
				vm.present(AppView.BOOK_DETAIL_VIEW, new BookDetailController(book, 
													new PublisherTableGateway().get()));
			} else if(model instanceof Author)
				vm.present(AppView.AUTHOR_DETAIL_VIEW, new AuthorDetailController((Author)model));
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AppException e) {
			e.printStackTrace();
			ViewManager.showError(e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.info("calling initializable()");

		titleLabel.setText("Audit Trail for " + model);
		
        TableColumn<AuditTrailEntry<T>, LocalDateTime> dateCol = new TableColumn<>("Date Added");
        dateCol.setPrefWidth(150);
        dateCol.setCellValueFactory(new PropertyValueFactory<AuditTrailEntry<T>, LocalDateTime>("dateAdded"));

        TableColumn<AuditTrailEntry<T>, String> messageCol = new TableColumn<>("Message");
        messageCol.setPrefWidth(445);
        messageCol.setCellValueFactory(new PropertyValueFactory<AuditTrailEntry<T>, String>("message"));

		tableView.getColumns().setAll(dateCol, messageCol);
		
		tableView.setItems(audits);
	}

}
