package application.view;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.SessionManager;
import application.controller.TopMenuController;
import application.util.exception.AppException;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuBar;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ViewManager {
	private static Logger logger = LogManager.getLogger(ViewManager.class);
	private static ViewManager instance;

	private Stage stage; /* current main stage */
	private VBox container; /* silent container for top menu */
	private MenuBar topMenu; 
	
	private ViewManager() {
	}
	
	private MenuBar loadMenu() {
		if(topMenu != null)
			return topMenu;
		
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(AppView.TOP_MENU_VIEW));
		loader.setController(new TopMenuController());
		try {
			return (MenuBar)loader.load();
		} catch (IOException e) {
			logger.debug(e.getStackTrace());
			logger.error("Menu failed to load.");
			return null;
		}
	}
	
	public void closeStage() {
		Stage stage = getPrimaryStage();
		stage.close();
	}

	/* Load and present a resource at the current stage */
	public Parent present(String resourceName) throws IOException {
		return present(resourceName, getPrimaryStage(), null);
	}

	/* Load and present a resource at the current stage with a controller */
	public Parent present(String resourceName, Object controller) throws IOException {
		return present(resourceName, getPrimaryStage(), controller);
	}

	/* Load and present a resource at a specific stage(possibly new) */
	public Parent present(String resourceName, Stage stage) throws IOException {
		return present(resourceName, stage, null);
	}

	/* Above overloading allows optional stage/controller parameters */
	public Parent present(String resourceName, Stage stage, Object controller) throws IOException {
		FXMLLoader loader = new FXMLLoader(this.getClass().getResource(resourceName));
		
		if(controller != null)
			loader.setController(controller);

		this.container = new VBox();
		Parent view = loader.load();
		Scene scene = null;
		if(resourceName == AppView.LOGIN_VIEW) {
			scene = new Scene(this.container, 300, 200);
			stage.setTitle(AppView.APPLICATION_TITLE);
		} else {
			scene = new Scene(this.container, 600, 450);
			stage.setTitle(AppView.APPLICATION_TITLE + " | " + SessionManager.getInstance().getMe().getUsername());
		}

		this.container.getChildren().clear();
		if(this.topMenu != null)
			this.container.getChildren().add(this.topMenu);
		this.container.getChildren().add(view);
		
		stage.setScene(scene);
		
		return view;
	}
	
	public static void showError(AppException e) {
		showError(e.getMessage());
	}
	
	public static void showError(String error) {
		Alert alert = new Alert(AlertType.ERROR, error, ButtonType.OK);
		alert.showAndWait();
	}
	
	public static void showWarning(String warning) {
		Alert alert = new Alert(AlertType.WARNING, warning, ButtonType.OK);
		alert.showAndWait();
	}
	/*
	 *  Getter Setters
	 */
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public Stage getPrimaryStage() {
		return this.stage;
	}
	
	public static ViewManager getInstance() {
		return ViewManager.getInstance(true);
	}
	
	public static ViewManager getInstance(boolean addTopMenu) {
		if(instance == null)
			instance = new ViewManager();

		instance.topMenu = addTopMenu ? instance.loadMenu() : null;
		
		return instance;
	}
}
