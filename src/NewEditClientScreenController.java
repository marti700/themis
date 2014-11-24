package themis;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import screensframework.*;
import javafx.scene.control.Label;

public class NewEditClientScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    
    @FXML
    public Label titleHeader;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    //setters 
    public  void setTitleHeader(String title){
        titleHeader.setText(title);
    }
    //event handlers
    @FXML
    private void showMainScreen(ActionEvent event){
        controller.setScreen(Themis.mainScreen);
    }
}
