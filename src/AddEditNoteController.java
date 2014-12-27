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

public class AddEditNoteController implements Initializable, ControlledScreen{
    
    ScreensController controller;
    Note note = new Note();
    static boolean newWasPressed = false;
    static boolean editWasPressed = false;
    
    @FXML
    private TextArea noteTextArea;
    /*public TextField getClientNamesTextField(){return clientNamesTextField;}
    public TextField getClientLastNamesTextField() {return clientLastNamesTextField;}
    public TextField getClientNationalityTextField() {return clientNationalityTextField;}
    public TextField getClientMaritalStatusTextField() {return clientMaritalStatusTextField;}
    public TextField getClientIdPassportTextField() {return clientIdPassportTextField;}
    public TextField getClientJobTextField() {return clientJobTextField;}*/
    public TextArea getNoteTextArea() {return noteTextArea;}

    /*public void setClientNamesTextField(String clientNames){clientNamesTextField.setText(clientNames);}
    public void setClientLastNamesTextField(String clientLastNames) {clientLastNamesTextField.setText(clientLastNames);}
    public void setClientNationalityTextField(String clientNationality) {clientNationalityTextField.setText(clientNationality);}
    public void setClientMaritalStatusTextField(String clientMaritalStatus) {clientMaritalStatusTextField.setText(clientMaritalStatus);}
    public void setClientIdPassportTextField(String clientIdPassport) {clientIdPassportTextField.setText(clientIdPassport);}
    public void setClientJobTextField(String clientJob) {clientJobTextField.setText(clientJob);}*/
    public void setNoteTextAreaText(String contentText) {noteTextArea.setText(contentText);}


    @Override
    public void initialize(URL url, ResourceBundle rb){
        //TODO
    }

    public void setScreenParent(ScreensController screenParent){
        controller = screenParent;
    }

    //setters 
    /*public  void setTitleHeader(String title){
        titleHeader.setText(title);
    }*/

    //event handlers
    @FXML
    private void showNotesManagementScreen(ActionEvent event){
        controller.setScreen(Themis.notesManagerScreen);
    }

    @FXML 
    private void addEditNote (ActionEvent event){
        
        //edit button was pressed in the note management screen
        if (editWasPressed) {
            System.out.println("Estamos Editando");
            //edit note infomation
            note.editNote(NotesManagerController.getSelectedNote().getNoteId(), noteTextArea.getText());
            System.out.println(NotesManagerController.getSelectedNote().getOwnedby()); 
            // create a note obect 
            Note editedNote = new Note(note.getNoteId(NotesManagerController.getSelectedNote().getCreateddate()), noteTextArea.getText(), 
                                        NotesManagerController.getSelectedNote().getCreateddate(), NotesManagerController.getSelectedNote().getOwnedby());
            
            //refresh tableview by updating allNotes observable list
            NotesManagerController.allNotes.set(NotesManagerController.getSelectedNoteIdex(),editedNote);

            controller.setScreen(Themis.notesManagerScreen);
        }
        else {
            System.out.println("Estamos Nuevos");
            
            //add note to database
            note.addNote(noteTextArea.getText());

            //add the new note to observable list, so the table view can be refreshed
            NotesManagerController.allNotes.add(new Note(note.getNoteId(NotesManagerController.getSelectedNote().getCreateddate()), noteTextArea.getText(), 
                                                MainScreenController.getSelectedClient().getClientId()));

            controller.setScreen(Themis.notesManagerScreen);
        } 
    }
}
