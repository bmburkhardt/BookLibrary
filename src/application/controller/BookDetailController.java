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
import application.model.AuthorBook;
import application.model.Book;
import application.model.Publisher;
import application.model.audits.AuditTrailEntry;
import application.model.gateway.AuthorBookTableGateway;
import application.model.gateway.AuthorTableGateway;
import application.util.DateConverter;
import application.util.YearConverter;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class BookDetailController extends ViewController {

	private static Logger logger = LogManager.getLogger(BookDetailController.class);
	
	@FXML private Button viewAuditTrail;
	@FXML private Button saveBook;
	@FXML private TextField title;
	@FXML private TextField isbn;
	@FXML private TextField yearPublished;
	@FXML private TextField dateAdded;
	@FXML private ComboBox<Publisher> publisher;
	@FXML private TextArea summary;
	@FXML private TableView<AuthorBook> tableView;
	@FXML private ComboBox<Author> addAuthor;
	@FXML private Button deleteAuthor;
	@FXML private Button confirmAddAuthor;
	@FXML private TextField setRoyalty;
	
	private AuthorBookTableGateway gateway;
	private AuthorTableGateway authorGateway;
	
	private Book book;
    private ObservableList<Publisher> publishers;
    private ObservableList<Author> authors;
    private ObservableList<AuthorBook> authorBooks;
    
    public BookDetailController() {
    		this.gateway = new AuthorBookTableGateway();
    		this.authorGateway = new AuthorTableGateway();
    }
    
    public BookDetailController(Book book, List<Publisher> publishers) {
		this();
		
		this.book = book;
		this.publishers = FXCollections.observableArrayList(publishers);
		updateAuthorBooks();
	}
    
    public void updateAuthorBooks() {
		try {
			if(this.authorBooks != null) {
				this.authorBooks.clear();
				this.authorBooks.addAll(book.getAuthorBooks());
			} else
				this.authorBooks = FXCollections.observableArrayList(book.getAuthorBooks());
			 
			logger.debug("authorBooks: " + this.authorBooks);
			if(tableView != null)
				tableView.setItems(authorBooks);
		} catch (AppException e) {
			ViewManager.showError(e);
			e.printStackTrace();
		}
    }
	
    @FXML
    void onAddAuthorClicked(ActionEvent event) {
    		logger.info("Adding author");
    		
    		Author author = addAuthor.valueProperty().get();
    		int royalty = (int)(Double.parseDouble(setRoyalty.getText()) * 100000);
    		
    		AuthorBook authorBook = new AuthorBook(author, this.book, royalty);
    		authorBook.setNewRecord(true);
    		authorBook.setGateway(gateway);
    		authorBook.save();
			
		updateAuthorBooks();
			
		book.createAudit("Author " + author.getFullName() + " added to book");
    }
	
	@FXML
    void onAuditTrailClicked(ActionEvent event) {
		logger.info("Attempting to view audit trail.");
		
		try {
			// Switch to the AUDIT_TRAIL_VIEW
			List<AuditTrailEntry<Book>> audits = book.getAudits();
			if(audits.size() > 0) {
				logger.info("audits: " + audits);
				ViewManager vm = ViewManager.getInstance();
				vm.present(AppView.AUDIT_TRAIL_VIEW, new AuditTrailController<Book>(book, audits));
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
	
	@FXML
	void onDeleteAuthorClicked(ActionEvent event) {
		AuthorBook authorBook = tableView.getSelectionModel().getSelectedItem();
		if(authorBook != null) {
			Author author = authorBook.getAuthor();
	    		Alert alert = new Alert(AlertType.WARNING, "Are you sure you wish to delete author, '" + author.getFullName() + "'?", ButtonType.OK, ButtonType.CANCEL);
	    		Optional<ButtonType> result = alert.showAndWait();
	    		if (result.get() == ButtonType.OK) {
	    			try {
					this.gateway.delete(authorBook);
					
					updateAuthorBooks();
					
					book.createAudit("Author " + author.getFullName() + " removed from book");
				} catch (AppException e) {
					ViewManager.showError(e);
				}
	    		}
		}
	}
	
	@FXML
    void onSaveBookClicked(ActionEvent event) {
		Publisher pub = publisher.valueProperty().get();
    		logger.info("Save book clicked for: " + book.getTitle() + " and publisher: " + pub);
    		try {
    			book.validate();
    			book.save();
    			// Switch back to the BOOK_LIST_VIEW
    			ViewManager vm = ViewManager.getInstance();
    			vm.present(AppView.BOOK_LIST_VIEW, new BookListController());
    		} catch (IOException e) {
    			e.printStackTrace();
    			logger.error("Book list failed to load.");
    		} catch (AppException e) {
    			ViewManager.showError(e);
    			logger.info("Could not validate input fields!");
    		}
    }
	
	
	@SuppressWarnings("unchecked")
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		logger.info("calling initializable");
		
		publisher.setItems(publishers);
		List<Author> authorList;
		try {
			authorList = authorGateway.get();
			authors = FXCollections.observableArrayList(authorList);
			addAuthor.setItems(authors);
		} catch (AppException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		title.textProperty().bindBidirectional(book.titleProperty());
		summary.textProperty().bindBidirectional(book.summaryProperty());
		isbn.textProperty().bindBidirectional(book.isbnProperty());
		yearPublished.textProperty().bindBidirectional(book.yearPublishedProperty(), new YearConverter());
		dateAdded.textProperty().bindBidirectional(book.dateAddedProperty(), new DateConverter());
		publisher.valueProperty().bindBidirectional(book.publisherProperty());
		
		if(book.getId() == null) {
			publisher.getSelectionModel().select(0);
			viewAuditTrail.setDisable(true);
		}
		
		TableColumn<AuthorBook, String> authorCol = new TableColumn<>("Author");
        authorCol.setPrefWidth(306);
        authorCol.setCellValueFactory(new PropertyValueFactory<AuthorBook, String>("author"));

        TableColumn<AuthorBook, String> royaltyCol = new TableColumn<>("Royalty");
        royaltyCol.setPrefWidth(88);
        royaltyCol.setCellValueFactory(new PropertyValueFactory<AuthorBook, String>("royalty"));  //needs to be String type so we can do the "#.##%" format
  
		tableView.getColumns().setAll(authorCol, royaltyCol);
		tableView.setItems(authorBooks);

		if(!SessionManager.getInstance().getMe().getRole().canCreate()) {
			saveBook.setDisable(true);
			deleteAuthor.setDisable(true);
			addAuthor.setDisable(true);
			confirmAddAuthor.setDisable(true);
			publisher.setDisable(true);
		}
	}


}