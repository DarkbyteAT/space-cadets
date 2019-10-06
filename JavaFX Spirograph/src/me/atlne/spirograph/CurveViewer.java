package me.atlne.spirograph;
	
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


public class CurveViewer extends Application {
	@Override
	public void start(Stage stage) {
		try {
			//Loads FXML scene
			Parent root = FXMLLoader.load(getClass().getResource("Main.fxml"));
			Scene scene = new Scene(root, 800, 800);
			//Sets window title
			stage.setTitle("ECS Spirograph Viewer");
			stage.setScene(scene);
			stage.show();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
