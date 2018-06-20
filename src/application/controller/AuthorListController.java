package application.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.SessionManager;
import application.model.Author;
import application.model.gateway.AuthorTableGateway;
import application.util.exception.AppException;
import application.view.AppView;
import application.view.ViewManager;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;

public class AuthorListController extends ViewController {
	private static Logger logger = LogManager.getLogger(AuthorListController.class);

    @FXML private ListView<Author> authorList;
    @FXML private Button deleteAuthor;
    
    private AuthorTableGateway gateway;
    private ObservableList<Author> authors;
    
    public AuthorListController() {
    		this.gateway = new AuthorTableGateway();
    		
    		fetch();
    }
    
	public AuthorListController(ObservableList<Author> authors) {
    		this();
    		this.authors = authors;
    }
    
    @FXML
    void onEntryClick(MouseEvent event) {
	    	if(event.getClickCount() == 2) {
		    	try {
		    		Author author = authorList.getSelectionModel().getSelectedItem();
				if(author != null) {
					ViewManager vm = ViewManager.getInstance();
					vm.present(AppView.AUTHOR_DETAIL_VIEW, new AuthorDetailController(author));
				
					logger.info(author.getFirstName() + " " + author.getLastName() + " was clicked");
				}
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	}	
	    	}
    }

    @FXML
    void onDeleteClick(ActionEvent event) {
		Author author = authorList.getSelectionModel().getSelectedItem();
		if(author != null) {
	    		Alert alert = new Alert(AlertType.WARNING, "Are you sure you wish to delete author, '" + author.getFullName() + "'?", ButtonType.OK, ButtonType.CANCEL);
	    		Optional<ButtonType> result = alert.showAndWait();
	    		if (result.get() == ButtonType.OK) {
	    			try {
					this.gateway.delete(author);
				} catch (AppException e) {
					ViewManager.showError(e);
				}
				fetch();
	    		}
		}
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.authorList.setItems(authors);

		if(!SessionManager.getInstance().getMe().getRole().canDelete())
			deleteAuthor.setDisable(true);
	}
	
	public void fetch() {
		try {
			List<Author> authors = gateway.get();
	    		logger.info("loading authors: " + authors);
	    		if(this.authors == null)
	    			this.authors = FXCollections.observableArrayList(authors);
	    		else
	    			this.authors.setAll(authors);
		} catch (AppException e) {
			ViewManager.showError(e);
		}
	}
}