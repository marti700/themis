
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
    public static String newEditClientScreen = "NewEditClientScreen";
    public static String newEditClientScreenFile = "../FXMLFiles/NewEditClientScreen.fxml";
    
    @Override
    public void start(Stage primaryStage){
        ScreensController mainContainer = new ScreensController();

        mainContainer.loadScreen(Themis.mainScreen, Themis.mainScreenFile);
        mainContainer.setScreen(Themis.mainScreen);
        
        mainContainer.loadScreen(Themis.newEditClientScreen, Themis.newEditClientScreenFile);
        mainContainer.setScreen(Themis.newEditClientScreen);
        
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
