
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
    public static String clientDetailScreen = "clientDetailScreen";
    public static String clientDetailScreenFile = "../FXMLFiles/ClientDetailScreen.fxml";
    public static String documentTypeSelector = "DocumentTypeSelector";
    public static String documentTypeSelectorFile = "../FXMLFiles/DocumentTypeSelector.fxml";
    public static String notesManagerScreen = "NotesManagerScreen";    
    public static String notesManagerScreenFile = "../FXMLFiles/NotesManagementScreen.fxml";
    public static String addEditNotesScreen = "AddEditScreen";
    public static String addEditNotesScreenFile = "../FXMLFiles/AddEditNote.fxml";


    @Override
    public void start(Stage primaryStage){
        
        //connects to database
        DatabaseConnector.connectToDatabase();

        ScreensController mainContainer = new ScreensController();

        //load screens from fxml files
        mainContainer.loadScreen(Themis.mainScreen, Themis.mainScreenFile);
        mainContainer.setScreen(Themis.mainScreen);
        
        mainContainer.loadScreen(Themis.newEditClientScreen, Themis.newEditClientScreenFile);
        mainContainer.setScreen(Themis.newEditClientScreen);
        
        mainContainer.loadScreen(Themis.clientDetailScreen, Themis.clientDetailScreenFile);
        mainContainer.setScreen(Themis.clientDetailScreen);
        
        mainContainer.loadScreen(Themis.documentTypeSelector, Themis.documentTypeSelectorFile);
        mainContainer.setScreen(Themis.documentTypeSelector);

        mainContainer.loadScreen(Themis.notesManagerScreen, Themis.notesManagerScreenFile);
        mainContainer.setScreen(Themis.notesManagerScreen);

        mainContainer.loadScreen(Themis.addEditNotesScreen, Themis.addEditNotesScreenFile);
        mainContainer.setScreen(Themis.addEditNotesScreen);

        mainContainer.setScreen(Themis.mainScreen);

        //prepare the screens to show
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
