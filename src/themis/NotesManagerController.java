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
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;

public class NotesManagerController implements Initializable, ControlledScreen{
    
    ScreensController controller;
   
    private static boolean tableViewColumnAlreadyLoaded = false;    

    public static ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private static int selectedNoteIndex = 0;
    private static Note SelectedNote;


    @FXML
    private Button AddNoteButton;

    @FXML
    private Button EditNoteButton;

    @FXML
    private TableView<Note> notesTable;

    @FXML
    private TextArea noteContentTextArea;

    public static TableView<Note> clientNotesTable = null; 
    /*
    public void setClientNameLabelText(String clientName){clientNameLabel.setText(clientName);}
    public void setNationalityLabelText(String nationality){nationalityLabel.setText(nationality);}
    public void setMaritalStatusLabelText(String maritalStatus) {maritalStatusLabel.setText(maritalStatus);}
    public void setJobLabelText(String job) {jobLabel.setText(job);}
    public void setIdPassportLabelText(String idPassport) {idPassportLabel.setText(idPassport);}
    public void setAddressLabelText(String address) {addressLabel.setText(address);}*/

    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
        //for some reason is not posible declare a fxml injected control as static (it comes null)
        //so, i declared a static tableview and then i make it point to the same tableview that comes from the fxml
        clientNotesTable = notesTable;

        //shows notes content when user clicks a note
        notesTable.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent e){
                if (e.isPrimaryButtonDown()){
                    try{
                        
                        //format the notes in a way that the text does not go tu much to the right
                        String[] wordsInNote;
                        String formatedNote = "";
                        int wordsPerLine = 0;
                        wordsInNote = notesTable.getSelectionModel().getSelectedItem().getContenttext().split(" ");
                        System.out.println("Despues de llenar el arreglo");
                        for (int i=0; i<wordsInNote.length; i++){
                            if (wordsPerLine == 5){
                                formatedNote += "\n";
                                wordsPerLine = 0;
                            }
                            System.out.println("Agregando a format note");
                            formatedNote += " ".concat(wordsInNote[i]);
                            System.out.println("Antes de incrementar the thing");
                            ++wordsPerLine;
                            System.out.println("Despues de Incrementar the thing");
                        }
                        noteContentTextArea.setText(formatedNote);
                    }
                    catch(Exception exception){
                        exception.printStackTrace();
                    }
                }
            }
        });
    }
    
    public static Note getSelectedNote() {return clientNotesTable.getSelectionModel().getSelectedItem();}
    
    public static void refresh(){
        
        Client client = new Client();
        Note notes = new Note();
    

        allNotes = notes.getAllNotes();

        clientNotesTable.setItems(allNotes);
        
        HashMap<String,String> columnNames = new HashMap<>();
        columnNames.put("contenttext", "Contenido de Nota");
        columnNames.put("createddate", "Fecha de Creacion");
        TableColumn<Note,String> column = null;
                        
        try{
            for (int i=0; i<notes.getNotesDatabaseTableResultSet().getMetaData().getColumnCount();++i){
                //give a column it header name
                if (columnNames.get(notes.getNotesDatabaseTableResultSet().getMetaData().getColumnName(i+1)) != null){
                    if (!tableViewColumnAlreadyLoaded)
                        column = new TableColumn<Note,String>(columnNames.get(notes.getNotesDatabaseTableResultSet().getMetaData().getColumnName(i+1)));
                    else
                        break;
                }
                else 
                    continue;
                
                //add data to the columns
                //clients.getNotesDatabaseTableResultSet().getMetaData().getColumnName(i+1) will call the method getColumnNameProperty of the Note object 
                //where columnName is the name of a javax.beans property for more info about this see:
                //https://docs.oracle.com/javase/8/javafx/api/javafx/scene/control/cell/PropertyValueFactory.html 
                column.setCellValueFactory(new PropertyValueFactory<Note,String>(notes.getNotesDatabaseTableResultSet().getMetaData().getColumnName(i+1)));

                //add the column to the tableview
                clientNotesTable.getColumns().add(column);
            }
            //the Table column have been loaded
            tableViewColumnAlreadyLoaded = true;
            
            //select the first element of the table
            clientNotesTable.getSelectionModel().selectFirst();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        

    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    public static int getSelectedNoteIdex(){return selectedNoteIndex;}
    
    //event handlers
    @FXML
    private void addNote(ActionEvent event){ 
        controller.setScreen(Themis.addEditNotesScreen);
        AddEditNoteController addEditScreen = (AddEditNoteController) controller.getLoader("AddEditScreen").getController();

        //tells if the new button of this scene was pressed
        addEditScreen.editWasPressed = false;
        addEditScreen.newWasPressed = true;

        //clear the text from the addEdit note screen 
        addEditScreen.getNoteTextArea().clear();
        
    }

    @FXML
    private void editNote(ActionEvent event){
        selectedNoteIndex = allNotes.indexOf(clientNotesTable.getSelectionModel().getSelectedItem()); 
    
        controller.setScreen(Themis.addEditNotesScreen);
        AddEditNoteController addEditScreen = (AddEditNoteController) controller.getLoader("AddEditScreen").getController();
        
        // tells if the edit button of this scene was pressed
        addEditScreen.newWasPressed = false;
        addEditScreen.editWasPressed = true;
        
        addEditScreen.setNoteTextAreaText(clientNotesTable.getSelectionModel().getSelectedItem().getContenttext());
       
    }
    @FXML
    private void goToClientDetailsScreen(ActionEvent event){
        controller.setScreen(Themis.clientDetailScreen);
    }
}
