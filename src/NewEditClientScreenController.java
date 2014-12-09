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
    boolean newWasPressed = false;
    boolean editWasPressed = false;

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
       
    @FXML
    private Button addClientButton;

    public TextField getClientNamesTextField(){return clientNamesTextField;}
    public TextField getClientLastNamesTextField() {return clientLastNamesTextField;}
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
    public void setClientAddressTextArea(String clientAddress) {clientAddressTextArea.setText(clientAddress);}


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
        System.out.println(MainScreenController.allClients.get(MainScreenController.getSelectedClientIndex() ));
        
        //edit button was pressed in the mainscreen
        if (editWasPressed) {
            System.out.println("Estamos Editando");
            //edit client infomation
            client.editClient(clientNamesTextField.getText(), clientLastNamesTextField.getText(), clientNationalityTextField.getText(), clientMaritalStatusTextField.getText(), 
                            clientJobTextField.getText(), clientIdPassportTextField.getText(), clientAddressTextArea.getText(), MainScreenController.allClients
                                                                                                                                .get(MainScreenController.getSelectedClientIndex())
                                                                                                                                .getClientId());
            
            // create a client obect 
            Client editedClient = new Client(MainScreenController.allClients.get(MainScreenController.getSelectedClientIndex()).getClientId(), clientNamesTextField.getText(), 
                                             clientLastNamesTextField.getText(), clientNationalityTextField.getText(), clientMaritalStatusTextField.getText(), clientJobTextField.getText(), 
                                             clientIdPassportTextField.getText(), clientAddressTextArea.getText());
            
            //refresh tableview by updating allclients observable list
            MainScreenController.allClients.set(MainScreenController.getSelectedClientIndex(),editedClient);
            
        }
        else {
            System.out.println("Estamos Nuevos");
            //insert client in database
            client.insertClient(clientNamesTextField.getText(), clientLastNamesTextField.getText(), clientNationalityTextField.getText(), clientMaritalStatusTextField.getText(), 
                            clientJobTextField.getText(), clientIdPassportTextField.getText(), clientAddressTextArea.getText());

            //add the new client to observable list, so the table view can be refreshed
            MainScreenController.allClients.add(new Client(client.getClientId(clientIdPassportTextField.getText()), clientNamesTextField.getText(), clientLastNamesTextField.getText(), 
                                                            clientNationalityTextField.getText(), clientMaritalStatusTextField.getText(), clientJobTextField.getText(), 
                                                            clientIdPassportTextField.getText(), clientAddressTextArea.getText()));
        } 
    }
}
