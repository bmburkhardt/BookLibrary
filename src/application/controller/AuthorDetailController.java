package application.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.SessionManager;
import application.model.Author;
import application.model.audits.AuditTrailEntry;
import application.util.BirthdayConverter;
import application.util.exception.AppException;
import application.view.AppView;
import application.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.converter.LocalDateTimeStringConverter;

public class AuthorDetailController extends ViewController {
	private static Logger logger = LogManager.getLogger(AuthorDetailController.class);
	
	@FXML private Button saveAuthor;
    @FXML private TextField firstName;
    @FXML private TextField lastName;
    @FXML private TextField dateOfBirth;
    @FXML private ComboBox<String> gender;
    @FXML private TextField website;
    @FXML private TextField lastModified;

    private Author author;
	
	public AuthorDetailController() {
	}
	
	public AuthorDetailController(Author author) {
		this();
		
		this.author = author;
	}
	
	@FXML 
	void onSaveAuthorClicked(ActionEvent event) {
		//logger.info("calling saveAuthorClicked for " + author.getFullName());

		try {
			author.validate(); //will throw error if cant validate
			
			author.save();
		
			// Switch back to the AUTHOR_LIST_VIEW
			ViewManager vm = ViewManager.getInstance();
			vm.present(AppView.AUTHOR_LIST_VIEW, new AuthorListController());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Author list failed to load.");
		} catch(AppException e) {
			ViewManager.showError(e);
			logger.info("Could not validate input fields!");
		}
	}

	@FXML
    void onAuditTrailClicked(ActionEvent event) {
		logger.info("Attempting to view audit trail.");
		
		try {
			// Switch to the AUDIT_TRAIL_VIEW
			List<AuditTrailEntry<Author>> audits = author.getAudits();
			if(audits.size() > 0) {
				logger.info("audits: " + audits);
				ViewManager vm = ViewManager.getInstance();
				vm.present(AppView.AUDIT_TRAIL_VIEW, new AuditTrailController<Author>(author, audits));
			} else
				ViewManager.showError("This book has no audit trail.");
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Audit Trail failed to load.");
		} catch (AppException e) {
			e.printStackTrace();
			ViewManager.showError(e);
		}
    }
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		//logger.info("calling initializable()");
		
		gender.setItems(FXCollections.observableArrayList("Male", "Female", "Unknown"));
		
		firstName.textProperty().bindBidirectional(author.firstNameProperty());
		lastName.textProperty().bindBidirectional(author.lastNameProperty());
		dateOfBirth.textProperty().bindBidirectional(author.dateOfBirthProperty(), new BirthdayConverter(dateOfBirth));
		website.textProperty().bindBidirectional(author.websiteProperty());
		gender.valueProperty().bindBidirectional(author.genderProperty());
		//if(author.lastModifiedProperty() != null)
		lastModified.textProperty().bindBidirectional(author.lastModifiedProperty(), new LocalDateTimeStringConverter());

		if(author.getId() == null)
			gender.getSelectionModel().select(2);

		if(!SessionManager.getInstance().getMe().getRole().canCreate()) {
			saveAuthor.setDisable(true);
			gender.setDisable(true);
		}
	}
}