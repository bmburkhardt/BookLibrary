package application.controller;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.SessionManager;
import application.model.Book;
import application.model.gateway.BookTableGateway;
import application.model.gateway.PublisherTableGateway;
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
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class BookListController extends ViewController {
	private static Logger logger = LogManager.getLogger(BookListController.class);
	
	@FXML private ListView<Book> bookList;
    @FXML private Button searchButton;
    @FXML private TextField bookSearchTitle;
    @FXML private Button deleteBook;
    @FXML private Button firstPage;
    @FXML private Button prevPage;
    @FXML private Button nextPage;
    @FXML private Button lastPage;
    @FXML private Label entryNumber;
    
    private BookTableGateway gateway;
    private PublisherTableGateway pubGateway;
    private ObservableList<Book> books;
    private int pageNm = 0;
    private int maxRecords;

    public BookListController() {
		this.gateway = new BookTableGateway();
		this.pubGateway = new PublisherTableGateway();
		fetch(null, 0);
    }

    public BookListController(ObservableList<Book> books) {
		this();
		this.books = books;
    }

    @FXML
	void onEntryClick(MouseEvent event) {
	    	if(event.getClickCount() == 2) {
		    	try {
		    		Book book = bookList.getSelectionModel().getSelectedItem();
				if(book != null) {
					ViewManager vm = ViewManager.getInstance();
					vm.present(AppView.BOOK_DETAIL_VIEW, new BookDetailController(book, pubGateway.get()));
				}
		    	} catch (IOException e) {
		    		e.printStackTrace();
		    	} catch (AppException e) {
				e.printStackTrace();
				ViewManager.showError(e);
			}
	    	}
	}
    
    @FXML
    void onBookDeleteClicked(ActionEvent event) {
    		Book book = bookList.getSelectionModel().getSelectedItem();
		if(book != null) {
	    		Alert alert = new Alert(AlertType.WARNING, "Are you sure you wish to delete book, '" + book.getTitle() + "'?", ButtonType.OK, ButtonType.CANCEL);
	    		Optional<ButtonType> result = alert.showAndWait();
	    		if (result.get() == ButtonType.OK) {
	    			try {
					this.gateway.delete(book);
					maxRecords = gateway.maxNumberRecords(null);
					if(maxRecords < 50) {
		    				entryNumber.setText("Fetched records 1" + " to " + maxRecords + " out of " + maxRecords);
		    			}
		    			else {
		    				entryNumber.setText("Fetched records 1" + " to " + (pageNm+1)*50 + " out of " + maxRecords);
		    			}
				} catch (AppException e) {
					ViewManager.showError(e);
				}
				fetch(null, pageNm);
	    		}
		}
    }
    
    @FXML
    void onSearchClick(ActionEvent event) {
    		logger.info("calling search for " + bookSearchTitle.getText());
    		fetch(bookSearchTitle.getText(), 0);
    		if(pageNm > maxRecords / 50) {
    			pageNm = maxRecords / 50;
    		}
    		if((pageNm+1)*50 > maxRecords) {
    			entryNumber.setText("Fetched records " + (pageNm*50+1) + " to " + maxRecords + " out of " + maxRecords);
    		}
    		else {
    			entryNumber.setText("Fetched records " + (pageNm*50+1) + " to " + (pageNm+1)*50 + " out of " + maxRecords);
    		}
    		//entryNumber.setText("Size is " + maxRecords);
    }
    
    @FXML
    void onFirstPageClicked(ActionEvent event) {
    		pageNm = 0;
    		if(maxRecords < 50) {
    			entryNumber.setText("Fetched records 1" + " to " + maxRecords + " out of " + maxRecords);
    		}
    		else {
    			entryNumber.setText("Fetched records 1" + " to " + (pageNm+1)*50 + " out of " + maxRecords);
    		}
    		fetch(bookSearchTitle.getText(), 0);

    }

    @FXML
    void onLastPageClicked(ActionEvent event) {
    		pageNm = maxRecords / 50;
    		entryNumber.setText("Fetched records " + (pageNm*50+1) + " to " + maxRecords + " out of " + maxRecords);
    		fetch(bookSearchTitle.getText(), pageNm);
    }

    @FXML
    void onNextPageClicked(ActionEvent event) {
    		pageNm++;
    		if(pageNm > maxRecords / 50) {
    			pageNm = maxRecords / 50;
    		}
    		if((pageNm+1)*50 > maxRecords) {
    			entryNumber.setText("Fetched records " + (pageNm*50+1) + " to " + maxRecords + " out of " + maxRecords);
    		}
    		else {
    			entryNumber.setText("Fetched records " + (pageNm*50+1) + " to " + (pageNm+1)*50 + " out of " + maxRecords);
    		}
    		fetch(bookSearchTitle.getText(), pageNm);
    }

    @FXML
    void onPrevPageClicked(ActionEvent event) {
    		pageNm--;
    		if(pageNm < 0) {
    			pageNm = 0;
    		}
    		entryNumber.setText("Fetched records " + (pageNm*50+1) + " to " + (pageNm+1)*50 + " out of " + maxRecords);
    		fetch(bookSearchTitle.getText(), pageNm);
    }
    
    @FXML
    void onAction(ActionEvent event) {
    		onSearchClick(event);
    }

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		maxRecords = gateway.maxNumberRecords(null);
		if(maxRecords < 50) {
			entryNumber.setText("Fetched records 1" + " to " + maxRecords + " out of " + maxRecords);
		}
		else {
			entryNumber.setText("Fetched records 1" + " to " + (pageNm+1)*50 + " out of " + maxRecords);
		}
		this.bookList.setItems(books);
		
		if(!SessionManager.getInstance().getMe().getRole().canDelete())
			deleteBook.setDisable(true);
	}
	
	public void fetch(String searchVar, int pageNumber) {
		try {
			maxRecords = gateway.maxNumberRecords(searchVar);
			List<Book> books = gateway.search(searchVar, pageNumber, 50);
	    		logger.info("searching books: " + books);
	    		if(this.books == null)
	    			this.books = FXCollections.observableArrayList(books);
	    		else
	    			this.books.setAll(books);
		} catch (AppException e) {
			ViewManager.showError(e);
		}
	}
}