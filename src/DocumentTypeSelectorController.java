package themis;

import java.util.*;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Arrays;
import javafx.scene.input.MouseEvent;
import javafx.event.ActionEvent;
import javafx.event.*;
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
import javafx.collections.FXCollections;
import javafx.scene.control.ListView;
import javafx.collections.ObservableList; 
import java.util.HashMap;
import java.io.File;
import java.nio.file.*;
import java.awt.Desktop;
import java.awt.Desktop.*;

public class DocumentTypeSelectorController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    Client client = MainScreenController.getSelectedClient();
    Document document = new Document();    

    @FXML
    private ListView<String> documentTypeList;

    @FXML
    private ListView<String> documentSubTypeList;
    
    private ObservableList<String> documentTypes = FXCollections.observableArrayList();
    private HashMap<String,ObservableList<String>> documentSubTypes = new HashMap<String, ObservableList<String>>();

           //public TextField getClientNamesTextField(){return clientNamesTextField;}DocumentTypeSelector
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

        //Initialize observableArrayList values for the documentTypesList
        documentTypes.add("Acto de Venta");
        documentTypes.add("Contrato de Alquiler");
    
        //Initialize hasmap values
        documentSubTypes.put("Acto de Venta", FXCollections.observableArrayList ("Inmueble", "Vehiculo de Motor", "Personalizado"));
        documentSubTypes.put("Contrato de Alquiler", FXCollections.observableArrayList ("Apartamento", "Casa"));

        //populate the document type list with element items
        documentTypeList.setItems(documentTypes);
        //System.out.println(documentTypes.get(0));
        
        
        documentTypeList.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                if (e.isPrimaryButtonDown() && e.getClickCount() == 1){
                    //System.out.println("Me dieron");
                   documentSubTypeList.setItems(documentSubTypes.get(documentTypeList.getSelectionModel().getSelectedItem()));
                }
            }
            
        });
        /*documentTypes.add("Acto de Venta");
        documentTypes.add("Contrato de Alquiler");
        documentTypeComboBox.setItems(documentTypes);*/
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    private boolean haveSubtype(){
        if (documentSubTypes.get(documentTypeList.getSelectionModel().getSelectedItem()) != null)
            return true;
        else 
            return false;

    }
    private void copyNewedCreatedDocument()throws Exception{
        Path sourceFilePathWithSubType =  Paths.get("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Templates/".concat(documentTypeList.getSelectionModel()
                .getSelectedItem()).replaceAll(" ", ".").concat("/").concat(documentSubTypeList.getSelectionModel().getSelectedItem().replaceAll(" ",".").concat(".doc")));

        Path sourceFilePathWithoutSubType = Paths.get("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Templates/".concat(documentTypeList.getSelectionModel()
                .getSelectedItem().replaceAll(" ", ".")).concat("/").concat(documentTypeList.getSelectionModel().getSelectedItem().replaceAll(" ", ".").concat(".doc")));
        
        Path sourceFilePath;
        
        //To treat Documents that don't have a sub type
        //if Document have a subtype
        if (haveSubtype())
           sourceFilePath = sourceFilePathWithSubType;
        
        //If document don't have a subtype
        else
            sourceFilePath = sourceFilePathWithoutSubType;
        
        //build the Path
        Path destinationPath = Paths.get("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat( "/")
                .concat(sourceFilePath.getFileName().toString()));
        //Path destinationPath = new Path (destinationStringPath);

        //execute copy command 
        //java.lang.Runtime.getRuntime().exec(copyCommand); 
        Files.copy(sourceFilePath, destinationPath);

    }
   
    private void renameDocument(String documentName){
        File fileWithSubType = new File("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat("/")
                .concat(documentSubTypeList.getSelectionModel().getSelectedItem().replaceAll(" ",".")).concat(".doc"));
        
        File fileWithoutSybType = new File("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat("/")
                .concat(documentTypeList.getSelectionModel().getSelectedItem().replaceAll(" ",".")).concat(".doc"));

        File file = null;
        
        // if have subtype 
        if (haveSubtype())
            fileWithSubType.renameTo(new File ("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat("/")
                    .concat(documentName).concat(".doc")));
        // if no subtype
        else
            fileWithSubType.renameTo(new File ("/home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat("/")
                    .concat(documentName).concat(".doc")));
    }

    private void openDocument(String documentName) throws Exception{
        // Opens the a document
        java.lang.Runtime.getRuntime().exec("gnome-open /home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat("/")
                .concat(documentName).concat(".doc"));
        
        //Desktop.getDesktop().open(file);
    }
   
    //events handlers
    @FXML
    private void showClientDetailScreen(ActionEvent e){
       controller.setScreen(Themis.clientDetailScreen); 
    } 

    @FXML
    private void createDocument(ActionEvent e)throws Exception {
        client = MainScreenController.getSelectedClient();
        //Saves the document in the database
        /*------------------------------------------------------------------------------------------*/
        Calendar timeStamp = Calendar.getInstance();
        SimpleDateFormat timeStampFormat = new SimpleDateFormat("dd-MM-yyyy[HH:mm:ss]");
        
        String documentName = documentTypeList.getSelectionModel().getSelectedItem().concat(".").concat(client.getNames()).concat("-")
            .concat(timeStampFormat.format(timeStamp.getTime()));
        documentName = documentName.replaceAll(" ", ".");  //changes empty spaces with dots
        
        String documentType = documentTypeList.getSelectionModel().getSelectedItem();
        String documentPath = "/home/teodoro/Documents/Projects/JavaProjects/themis/src/Documents/".concat(client.getIdpassport()).concat("/").concat(documentName);
        int documentOwner = client.getClientId();

        document.insertDocument(documentName, documentType, documentPath, documentOwner);
        /*----------------------------------------------------------------------------------------------*/

        //Add the document to the documents observable list in order to display the recently added document to the used
        int documentId = document.getDocumentId(documentPath);
        ClientDetailScreenController.allClientDocuments.add(new Document(documentId, documentName, documentType, documentPath, documentOwner)); 

        //copy the document from the templates folder to the document folder
        copyNewedCreatedDocument();

        //change the copied document template name to the document name
        renameDocument(documentName); 
        
        //opens the document
        openDocument(documentName);
    }
}
