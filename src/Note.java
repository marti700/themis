package themis; 

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.*;
import javafx.scene.control.TableView;

public class Note{

    private IntegerProperty id;
    private StringProperty contentText;
    private StringProperty creationDate;
    private IntegerProperty ownedBy;    

    private ObservableList<Note> allNotes = FXCollections.observableArrayList();
    private Statement stm;
	private	ResultSet rs;
   
    //default constructor
    public Note(){
    }

    public Note (int id, String contentText, int ownedBy){

        Calendar currentDate = Calendar.getInstance();  //gets the current date;
        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); //format current date

        this.id = new SimpleIntegerProperty(id);
        this.contentText = new SimpleStringProperty(contentText);
        this.creationDate = new SimpleStringProperty(formatDate.format(currentDate.getTime()));
        this.ownedBy = new SimpleIntegerProperty(ownedBy);
    }

    public Note (int id, String contentText, String creationDate, int ownedBy){

        this.id = new SimpleIntegerProperty(id);
        this.contentText = new SimpleStringProperty(contentText);
        this.creationDate = new SimpleStringProperty(creationDate);
        this.ownedBy = new SimpleIntegerProperty(ownedBy);
    }
    
    //getters
    public int getNoteId(){return id.get();}
    public String getContenttext(){return contentText.get();}
    public String getCreateddate(){return creationDate.get();}
    public int getOwnedby(){return ownedBy.get();}     
    public void addNote(String noteContent){
        
        try{
            CallableStatement addNote = DatabaseConnector.connection.prepareCall("{call addnote(?,TO_TIMESTAMP(?, 'DD-MM-YYYY HH24:MI:SS'),?)}");

            Calendar currentDate = Calendar.getInstance(); //get current date
            SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            addNote.setString(1,noteContent);
            addNote.setString(2,formatDate.format(currentDate.getTime()));
            addNote.setInt(3,MainScreenController.getSelectedClient().getClientId());

            //execute Query
            addNote.execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public void editNote(int id, String contentText){
        try{
            CallableStatement editNote = DatabaseConnector.connection.prepareCall("{call editnote(?,?)}");

            editNote.setInt(1, id);
            editNote.setString(2, contentText);

            //execute Query
            editNote.execute();
            System.out.println("Supustamente ya se edito");
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }
    //returns all Documents
	public ObservableList<Note> getAllNotes(){
	/* Returns an obserbable list which contains all documents*/
		try{
			ResultSet queryResult = getNotesDatabaseTableResultSet();
			
			// add data to observablelist 
			while (queryResult.next()){
				allNotes.add(new Note(queryResult.getInt("id"), queryResult.getString("contenttext"), queryResult.getString("createdDate"),queryResult.getInt("ownedby")));
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return allNotes;
	}

	public ResultSet getNotesDatabaseTableResultSet() throws Exception{
		/* Returns a resultset of a SQL select executed against the Documents database table */

		try{
            String queryString = "SELECT id, contentText, createdDate, ownedby FROM Notas WHERE ownedby = ?";
			PreparedStatement stm = DatabaseConnector.connection.prepareStatement(queryString);
            //stm = DatabaseConnector.connection.createStatement();
            stm.setInt(1, MainScreenController.getSelectedClient().getClientId());
			rs = stm.executeQuery();
		}

		catch(Exception e){
			e.printStackTrace();
		}
		return rs;
	}

    public int getNoteId(String creationDate){
	    int id = -1;
        try{
			ResultSet rs = null;
			PreparedStatement stm = DatabaseConnector.connection.prepareStatement("SELECT id FROM Notas WHERE createdDate = TO_TIMESTAMP(?, 'DD-MM-YYYY HH24:MI:SS')");
            stm.setString(1, creationDate);
            rs = stm.executeQuery();
            while (rs.next()){
              id = rs.getInt("id");
            }
            return id;
		}

		catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}

}
