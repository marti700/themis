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
import screensframework.*;

public class MainScreenController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    private Client clients = new Client();

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
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    @FXML
    private void showNewClientScreen(ActionEvent event){
        controller.setScreen(Themis.newEditClientScreen);
        NewEditClientScreenController addEditScreen = (NewEditClientScreenController) controller.getLoader("NewEditClientScreen").getController();
        addEditScreen.titleHeader.setText("Nuevo Cliente");
        
    }

    @FXML
    private void showEditClientScreen(ActionEvent event){
        controller.setScreen(Themis.newEditClientScreen);
        NewEditClientScreenController addEditScreen = (NewEditClientScreenController) controller.getLoader("NewEditClientScreen").getController();
        addEditScreen.titleHeader.setText("Editar Cliente");
        
    }
}
