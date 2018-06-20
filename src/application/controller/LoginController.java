package application.controller;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import application.SessionManager;
import application.util.exception.AppException;
import application.view.AppView;
import application.view.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

public class LoginController extends ViewController {
	private static Logger logger = LogManager.getLogger(LoginController.class);

	@FXML private TextField username;
	@FXML private TextField password;
	@FXML private Button login;

    @FXML
	public void onLoginPressed() {
    		System.out.println("login pressed");
		try {
			if(SessionManager.getInstance().login(username.getText(), password.getText())) {
	        		ViewManager vm = ViewManager.getInstance(true);
	        		vm.closeStage();
	        		
	        		try {
					vm.present(AppView.AUTHOR_LIST_VIEW, new AuthorListController());
					vm.getPrimaryStage().show();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else
				ViewManager.showWarning("Invalid login credentials.");
		} catch (AppException e) {
			e.printStackTrace();
        		ViewManager.showError(e);
		}
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		logger.info("calling initializable");

	}
}
