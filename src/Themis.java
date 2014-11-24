
package themis;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Group;
import javafx.stage.Stage;
import screensframework.*;

public class Themis extends Application{
    
    public static String mainScreen = "MainScreen";
    public static String mainScreenFile = "../FXMLFiles/MainScreen.fxml";
    public static String newClientScreen = "NewClientScreen";
    public static String newClientScreenFile = "../FXMLFiles/NewClientScreen.fxml";
    
    @Override
    public void start(Stage primaryStage){
        ScreensController mainContainer = new ScreensController();

        mainContainer.loadScreen(Themis.mainScreen, Themis.mainScreenFile);
        mainContainer.setScreen(Themis.mainScreen);
        
        mainContainer.loadScreen(Themis.newClientScreen, Themis.newClientScreenFile);
        mainContainer.setScreen(Themis.newClientScreen);
        
        mainContainer.setScreen(Themis.mainScreen);

        Group root = new Group();
        root.getChildren().addAll(mainContainer);
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String [] args){
        launch(args);
    }

}