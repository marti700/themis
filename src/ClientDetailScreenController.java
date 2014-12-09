package themis;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import screensframework.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;

public class ClientDetailScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    //Client client = new Client();

    @FXML
    Label clientNameLabel;

    @FXML
    Label nationalityLabel;

    @FXML
    Label maritalStatusLabel;
    
    @FXML
    Label jobLabel;

    @FXML
    Label idPassportLabel;

    @FXML
    Label addressLabel;

    public void setClientNameLabelText(String clientName){clientNameLabel.setText(clientName);}
    public void setNationalityLabelText(String nationality){nationalityLabel.setText(nationality);}
    public void setMaritalStatusLabelText(String maritalStatus) {maritalStatusLabel.setText(maritalStatus);}
    public void setJobLabelText(String job) {jobLabel.setText(job);}
    public void setIdPassportLabelText(String idPassport) {idPassportLabel.setText(idPassport);}
    public void setAddressLabelText(String address) {addressLabel.setText(address);}

    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    
    //event handlers
    @FXML
    private void showMainScreen(ActionEvent event){ 
        controller.setScreen(Themis.mainScreen);
    }

    @FXML
    private void addDocument(ActionEvent event){
        controller.setScreen(Themis.documentTypeSelector);
    }
}
