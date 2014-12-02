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

public class NewEditClientScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    Client client = new Client();

    @FXML
    public Label titleHeader;

    @FXML
    private TextField clientNamesTextField;

    @FXML
    private TextField clientLastNamesTextField;

    @FXML
    private TextField clientNationalityTextField;

    @FXML
    private TextField clientMaritalStatusTextField;
    
    @FXML
    private TextField clientIdPassportTextField;

    @FXML
    private TextField clientJobTextField;

    @FXML
    private TextArea clientAddressTextArea;
       
    /*@FXML
    private Button addClientButton;*/

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

    @FXML 
    private void addClient (ActionEvent event){

        //insert client in database
        client.insertClient(clientNamesTextField.getText(), clientLastNamesTextField.getText(), clientNationalityTextField.getText(), clientMaritalStatusTextField.getText(), 
                            clientIdPassportTextField.getText(), clientJobTextField.getText(), clientAddressTextArea.getText());

        //add the new client to observable list
        MainScreenController.allClients.add(new Client(clientNamesTextField.getText(), clientLastNamesTextField.getText(), clientNationalityTextField.getText(), 
                            clientMaritalStatusTextField.getText(), clientIdPassportTextField.getText(), clientJobTextField.getText(), clientAddressTextArea.getText()));
    }
}
