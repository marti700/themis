package themis;

import java.util.HashMap;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.event.*;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import screensframework.*;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClientDetailScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    private static boolean tableViewColumnAlreadyLoaded = false;    
    public static ObservableList<Document> allClientDocuments = FXCollections.observableArrayList();

    @FXML
    private Label clientNameLabel;

    @FXML
    private Label nationalityLabel;

    @FXML
    private Label maritalStatusLabel;
    
    @FXML
    private Label jobLabel;

    @FXML
    private Label idPassportLabel;

    @FXML
    private Label addressLabel;

    @FXML
    private TableView<Document> documentsTable;

    public static TableView<Document> clientDocumentsTable = null; 

    public void setClientNameLabelText(String clientName){clientNameLabel.setText(clientName);}
    public void setNationalityLabelText(String nationality){nationalityLabel.setText(nationality);}
    public void setMaritalStatusLabelText(String maritalStatus) {maritalStatusLabel.setText(maritalStatus);}
    public void setJobLabelText(String job) {jobLabel.setText(job);}
    public void setIdPassportLabelText(String idPassport) {idPassportLabel.setText(idPassport);}
    public void setAddressLabelText(String address) {addressLabel.setText(address);}

    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
        //for some reason is not posible declare a fxml injected control as static (it comes null)
        //so, i declared a static tableview and then i make it point to the same tableview that comes from the fxml
        clientDocumentsTable = documentsTable;
        
        
        //open a document on doube clic
        documentsTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                if (e.isPrimaryButtonDown() && e.getClickCount() == 2){
                    try{
                        String openDocument = "gnome-open ".concat(documentsTable.getSelectionModel().getSelectedItem().getPath()).concat(".doc");
                        java.lang.Runtime.getRuntime().exec(openDocument);
                    }
                    catch(Exception exception){
                        exception.printStackTrace();
                    }
                }
            }
        });
    }
    
    public static void refresh(){
       /*Refresh the Document table view evertime a information abou an user is requested*/ 

        Client client = new Client();
        Document documents = new Document();
    

        allClientDocuments = documents.getAllClientDocuments();
        clientDocumentsTable.setItems(allClientDocuments);

        //holds the column names that will be displayed in the Table view
        HashMap<String,String> columnNames = new HashMap<>();
        columnNames.put("name", "Nombre");
        columnNames.put("type", "Tipo");
        columnNames.put("creationdate", "Fecha de Creacion");
        TableColumn<Document,String> column = null;
       
        //populate the table with the column names and data
        try{
            for (int i=0; i<documents.getDocumentDatabaseTableResultSet().getMetaData().getColumnCount();++i){
                
                //give a column it header name
                if (columnNames.get(documents.getDocumentDatabaseTableResultSet().getMetaData().getColumnName(i+1)) != null){
                    if (!tableViewColumnAlreadyLoaded)
                        column = new TableColumn<Document,String>(columnNames.get(documents.getDocumentDatabaseTableResultSet().getMetaData().getColumnName(i+1)));
                }
                else 
                    continue;
                
                //add data to the columns
                //clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1) will call the method getColumnNameProperty of the client object 
                //where columnName is the name of a javax.beans property for more info about this see:
                //https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/cell/PropertyValueFactory.html 
                column.setCellValueFactory(new PropertyValueFactory<Document,String>(documents.getDocumentDatabaseTableResultSet().getMetaData().getColumnName(i+1)));

                //add the column to the tableview
                clientDocumentsTable.getColumns().add(column);
                //System.out.println(clientDocumentsTable);
            }
            tableViewColumnAlreadyLoaded = true;
        }
        catch(Exception e){
            e.printStackTrace();
        }

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

    @FXML
    private void showNotesManagerScreen(ActionEvent event){
        controller.setScreen(Themis.notesManagerScreen);
        NotesManagerController.refresh();
    }
}
