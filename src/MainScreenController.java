package themis;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import screensframework.*;

public class MainScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    
    
    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    @FXML
    private void showNewClientScreen(ActionEvent event){
        controller.setScreen(Themis.newEditClientScreen);
        NewEditClientScreenController addEditScreen = (NewEditClientScreenController) controller.getLoader("NewEditClientScreen").getController();
        addEditScreen.titleHeader.setText("Nuevo Cliente");
        
    }

    @FXML
    private void showEditClientScreen(ActionEvent event){
        controller.setScreen(Themis.newEditClientScreen);
        NewEditClientScreenController addEditScreen = (NewEditClientScreenController) controller.getLoader("NewEditClientScreen").getController();
        addEditScreen.titleHeader.setText("Editar Cliente");
        
    }
}
