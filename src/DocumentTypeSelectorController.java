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

public class DocumentTypeSelectorController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    Client client = new Client();
    //boolean newWasPressed = false;
    //boolean editWasPressed = false;

    @FXML
    private ComboBox documentTypeComboBox;
    private ObservableList<String> documentTypes = new FX.Collections.ObservableListArrayList();


    public TextField getClientNamesTextField(){return clientNamesTextField;}
    /*public TextField getClientLastNamesTextField() {return clientLastNamesTextField;}
    public TextField getClientNationalityTextField() {return clientNationalityTextField;}
    public TextField getClientMaritalStatusTextField() {return clientMaritalStatusTextField;}
    public TextField getClientIdPassportTextField() {return clientIdPassportTextField;}
    public TextField getClientJobTextField() {return clientJobTextField;}
    public TextArea getClientAddressTextArea() {return clientAddressTextArea;}

    public void setClientNamesTextField(String clientNames){clientNamesTextField.setText(clientNames);}
    public void setClientLastNamesTextField(String clientLastNames) {clientLastNamesTextField.setText(clientLastNames);}
    public void setClientNationalityTextField(String clientNationality) {clientNationalityTextField.setText(clientNationality);}
    public void setClientMaritalStatusTextField(String clientMaritalStatus) {clientMaritalStatusTextField.setText(clientMaritalStatus);}
    public void setClientIdPassportTextField(String clientIdPassport) {clientIdPassportTextField.setText(clientIdPassport);}
    public void setClientJobTextField(String clientJob) {clientJobTextField.setText(clientJob);}
    public void setClientAddressTextArea(String clientAddress) {clientAddressTextArea.setText(clientAddress);}*/


    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO 
        documentTypes.add("Acto de Venta");
        documentTypes.add("Contrato de Alquiler");
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

   //events handlers

    @FXML
    private showMainScreen(ActionEvent e){
       controller.setScreen(Themis.MainScreen); 
    } 
}
