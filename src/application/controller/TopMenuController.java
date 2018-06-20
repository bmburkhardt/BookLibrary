package application.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.SessionManager;
import application.model.Author;
import application.model.Book;
import application.model.gateway.AuthorTableGateway;
import application.model.gateway.BookTableGateway;
import application.model.gateway.PublisherTableGateway;
import application.util.exception.AppException;
import application.view.AppView;
import application.view.ViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;

public class TopMenuController extends ViewController {
	private static Logger logger = LogManager.getLogger(TopMenuController.class);

    @FXML private MenuItem addAuthor;
    @FXML private MenuItem addBook;
    @FXML private MenuItem generateReport;
    
	private PublisherTableGateway pubGateway;
	String sessionId;
    
    public TopMenuController() {
		this.pubGateway = new PublisherTableGateway();
		System.out.println("Hello " + SessionManager.getInstance().getMe().getUsername());
    }

    @FXML
    void onClickAuthorList(ActionEvent event) {
		logger.info("Author List menu item clicked.");
		
		ViewManager vm = ViewManager.getInstance();
		try {
			vm.present(AppView.AUTHOR_LIST_VIEW, new AuthorListController());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Author list failed to load.");
		}
    }
    
    @FXML
    void onClickAddAuthor(ActionEvent event) {
		logger.info("Adding author");

		ViewManager vm = ViewManager.getInstance();
		try {
			Author author = new Author();
			author.setGateway(new AuthorTableGateway());
			vm.present(AppView.AUTHOR_DETAIL_VIEW, new AuthorDetailController(author));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Author detail failed to load.");
		}
    }
    
    @FXML
    void onClickBookList(ActionEvent event) {
		//logger.info("Book List menu item clicked.");
		
		try {
			ViewManager vm = ViewManager.getInstance();
			vm.present(AppView.BOOK_LIST_VIEW, new BookListController());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Book list failed to load.");
		}
    }
    
    @FXML
    void onClickAddBook(ActionEvent event) {
    		//logger.info("Adding book");

		try {
			Book book = new Book();
			book.setGateway(new BookTableGateway());

			ViewManager vm = ViewManager.getInstance();
			vm.present(AppView.BOOK_DETAIL_VIEW, new BookDetailController(book, pubGateway.get()));
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Book detail failed to load.");
		} catch (AppException e) {
			e.printStackTrace();
			ViewManager.showError(e);
		}
    }
    
    @FXML
    void onGenerateReportClicked(ActionEvent event) {
    		logger.info("Report menu clicked");

		ViewManager vm = ViewManager.getInstance();
		try {
			vm.present(AppView.REPORT_GENERATE_VIEW, new ReportController());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("Author detail failed to load.");
		}
    }

    @FXML
    void onLogoutClick(ActionEvent event) {
		logger.info("Logging out");
		SessionManager.getInstance().logout();
		
		ViewManager vm = ViewManager.getInstance(false);
		vm.closeStage();
		
		try {
			vm.present(AppView.LOGIN_VIEW, new LoginController());
			vm.getPrimaryStage().show();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }

    @FXML
    void onQuitClick(ActionEvent event) {
		logger.info("Quitting...");
    		System.exit(0);
    }
    
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		if(!SessionManager.getInstance().getMe().getRole().canCreate()) {
			addBook.setDisable(true);
			addAuthor.setDisable(true);
		}
		if(!SessionManager.getInstance().getMe().getRole().canGenerateReport())
			generateReport.setDisable(true);
	}
}
