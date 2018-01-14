package themis;

import java.util.HashMap;
import javafx.collections.transformation.FilteredList;
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
import javafx.scene.control.TextField;

public class MainScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    private Client clients = new Client();

    private static int selectedClientIndex = 0;
    public static ObservableList<Client> allClients = FXCollections.observableArrayList();
    private static Client currentClient;

    @FXML
    private TableView<Client> clientsTable;    

    @FXML
    private TextField searchTextField;
        
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //TODO
        
        allClients = clients.getAllClients();
        
        clientsTable.setItems(allClients);

        //holds the column names that will be displayed in the Table view
        HashMap<String,String> columnNames = new HashMap<>();
        columnNames.put("names", "Nombres");
        columnNames.put("lastnames","Apellidos");
        columnNames.put("idpassport", "Cedula/Pasaporte");
        columnNames.put("address", "Domicilio");
        TableColumn<Client,String> column = null;

        try{
            for (int i=0; i<clients.getClientDatabaseTableResultSet().getMetaData().getColumnCount();++i){
                //give a column it header name
                if (columnNames.get(clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1)) != null)
                    column = new TableColumn<Client,String>(columnNames.get(clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1)));
                else 
                    continue;
                System.out.println(clients.getClientDatabaseTableResultSet().getMetaData().getColumnName(i+1));                
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

        //opens the client detail screen when a client of the clients table is double clicked
        clientsTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                if (e.isPrimaryButtonDown() && e.getClickCount() == 2){
                    //sets the clientDetailsScreen in the screen     
                    controller.setScreen(Themis.clientDetailScreen);
                    //get the loader used to load the client Detail Screen
                    System.out.println(allClients.indexOf(clientsTable.getSelectionModel().getSelectedItem()));
                    ClientDetailScreenController clientDetailScreen = (ClientDetailScreenController) controller.getLoader("clientDetailScreen").getController();
                    //takes the selected client
                    currentClient = clientsTable.getSelectionModel().getSelectedItem();
                    //
                    //currentSelectedClient = currentClient;

                    //set the client's detail screen label's text 
                    clientDetailScreen.setClientNameLabelText(currentClient.getNames().concat(" ").concat(currentClient.getLastnames()));
                    clientDetailScreen.setNationalityLabelText(currentClient.getNationality());
                    clientDetailScreen.setMaritalStatusLabelText(currentClient.getMaritalstatus());
                    clientDetailScreen.setJobLabelText(currentClient.getJob());
                    clientDetailScreen.setIdPassportLabelText(currentClient.getIdpassport());
                    clientDetailScreen.setAddressLabelText(currentClient.getAddress());

                    ClientDetailScreenController.refresh();
                }

            }
        });
        //Filter the data from the table view by names, last names and id/passport
        FilteredList<Client> filteredData = new FilteredList<>(allClients, p -> true);
            //Set the filter Predicate whenever the filter changes.
            searchTextField.textProperty().addListener((observable, oldValue, newValue) -> {
                filteredData.setPredicate(client -> {
                    // If filter text is empty, display all clients.
                    if (newValue == null || newValue.isEmpty()) {
                        return true;
                    }
                    // Compare first name and last name of every client with filter text.
                    String lowerCaseFilter = newValue.toLowerCase();
                    if (client.getNames().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                        System.out.println(client.getNames().toLowerCase().indexOf(lowerCaseFilter));
                        return true; // Filter matches client names.
                    } 
                    else if (client.getLastnames().toLowerCase().indexOf(lowerCaseFilter) != -1) {
                        return true; // Filter matches client last names.
                    }
                    else if (client.getIdpassport().toLowerCase().indexOf(lowerCaseFilter) != -1){
                        return true; // Filter matches id/passport
                    }
                    return false; // Not a match.
                });
                clientsTable.setItems(filteredData);
        });
     /*---------------------------------------------------*/
    }

    public static Client getSelectedClient(){
        //currentSelectedClient = clientsTable.getSelectionModel().getSelectedItem();
        return currentClient;
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

