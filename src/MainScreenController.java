package themis;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;  
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.scene.input.MouseEvent;
import javafx.event.*;
//import javafx.scene.control.SelectionModel.*;
//import java.awt.event.MouseEvent;
import screensframework.*;

public class MainScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    private Client clients = new Client();

    private static int selectedClientIndex = 0;
    public static ObservableList<Client> allClients = FXCollections.observableArrayList();

    @FXML
    private TableView<Client> clientsTable;    
    
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        
        allClients = clients.getAllClients();
        
        clientsTable.setItems(allClients);
        
        try{
            for (int i=0; i<clients.getClientDatabaseTableResultSet().getMetaData().getColumnCount();++i){
                //give a column it header name
                TableColumn<Client,String> column = new TableColumn<Client,String>(clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1));
                
                //add data to the columns
                //clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1) will call the method getColumnNameProperty of the client object 
                //where columnName is the name of a javax.beans property for more info about this see:
                //https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/cell/PropertyValueFactory.html 
                column.setCellValueFactory(new PropertyValueFactory<Client,String>(clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1)));

                //add the column to the tableview
                clientsTable.getColumns().add(column);
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }

        //select the first element of the table
        clientsTable.getSelectionModel().selectFirst(); 

        clientsTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                if (e.isPrimaryButtonDown() && e.getClickCount() == 2)
                    System.out.println(allClients.indexOf(clientsTable.getSelectionModel().getSelectedItem()));
                else if (e.isPrimaryButtonDown() && e.getClickCount() == 1)
                    System.out.println("Editar");
            }
        });
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    public static int getSelectedClientIndex(){return selectedClientIndex;}

    @FXML
    private void showNewClientScreen(ActionEvent event){
        controller.setScreen(Themis.newEditClientScreen);
        NewEditClientScreenController addEditScreen = (NewEditClientScreenController) controller.getLoader("NewEditClientScreen").getController();
        addEditScreen.titleHeader.setText("Nuevo Cliente");

        // tells the new screen that the new button of this scene was pressed
        addEditScreen.editWasPressed = false;
        addEditScreen.newWasPressed = true;

        //clears the text from the new client screen text fields
        addEditScreen.getClientNamesTextField().clear();
        addEditScreen.getClientLastNamesTextField().clear();
        addEditScreen.getClientNationalityTextField().clear();
        addEditScreen.getClientMaritalStatusTextField().clear();
        addEditScreen.getClientJobTextField().clear();
        addEditScreen.getClientIdPassportTextField().clear();
        addEditScreen.getClientAddressTextArea().clear();
    }

    @FXML
    private void showEditClientScreen(ActionEvent event){
        
        selectedClientIndex = allClients.indexOf(clientsTable.getSelectionModel().getSelectedItem());
        

        controller.setScreen(Themis.newEditClientScreen);
        NewEditClientScreenController addEditScreen = (NewEditClientScreenController) controller.getLoader("NewEditClientScreen").getController();
        addEditScreen.titleHeader.setText("Editar Cliente");

        // tells the new screen that the edit button of this scene was pressed
        addEditScreen.newWasPressed = false;
        addEditScreen.editWasPressed = true;
        
        //set edit screen text fields with the selected element from the table view 
        addEditScreen.setClientNamesTextField(allClients.get(selectedClientIndex).getNames());
        addEditScreen.setClientLastNamesTextField(allClients.get(selectedClientIndex).getLastnames());
        addEditScreen.setClientNationalityTextField(allClients.get(selectedClientIndex).getNationality());
        addEditScreen.setClientMaritalStatusTextField(allClients.get(selectedClientIndex).getMaritalstatus());
        addEditScreen.setClientJobTextField(allClients.get(selectedClientIndex).getJob());
        addEditScreen.setClientIdPassportTextField(allClients.get(selectedClientIndex).getIdpassport());
        addEditScreen.setClientAddressTextArea(allClients.get(selectedClientIndex).getAddress());
        
    }
}

