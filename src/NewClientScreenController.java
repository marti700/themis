package themis;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import screensframework.*;

public class NewClientScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;

    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    /*@FXML
    private void showNewClientScreen(ActionEvent event){
        controller.setScreen(ScreensFramework.NewClientScreen);
    }*/
}
