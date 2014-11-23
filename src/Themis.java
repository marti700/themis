
package themis;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.Parent;
import javafx.scene.Group;
import javafx.stage.Stage;
import screensframework.*;

public class Themis extends Application{
    
    public static String screen1ID = "MainScreen";
    public static String screenFile = "../FXMLFiles/MainScreen.fxml";
    public static String screen2ID = "MainScreen1";
    public static String screenFile2 = "MainScreen.fxml";
    
    @Override
    public void start(Stage primaryStage){
        ScreensController mainContainer = new ScreensController();

        mainContainer.loadScreen(Themis.screen1ID, Themis.screenFile);
        mainContainer.setScreen(Themis.screen1ID);
        
        //mainContainer.loadScreen(Themis.screen2ID, Themis.screenFile2);
        //mainContainer.setScreen(Themis.screen2ID);


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
