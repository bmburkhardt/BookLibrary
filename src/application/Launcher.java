package application;

import java.io.IOException;

import application.controller.LoginController;
import application.view.AppView;
import application.view.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Launcher extends Application {
	
	@Override
	public void start(Stage stage) throws Exception {
		SessionManager.getInstance(); //instantiate connection to wildfly

		Platform.runLater(new Runnable() { //guarantee rest of application always runs on Java FX thread
            @Override public void run() {
	        		ViewManager vm = ViewManager.getInstance(false);
	        		vm.setStage(stage);
	        		try {
					vm.present(AppView.LOGIN_VIEW, new LoginController());
		        		stage.show();
				} catch (IOException e) {
					e.printStackTrace();
				}
            }
        });
	}

	@Override
	public void init() throws Exception {
		super.init();
	}

	@Override
	public void stop() throws Exception {
		super.stop();
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
