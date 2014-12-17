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

public class Document{

    private IntegerProperty id;
    private StringProperty name;
    private StringProperty type;
    private StringProperty path;
    private StringProperty creationDate;
    private IntegerProperty owner;
    
    private ObservableList<Document> allDocuments = FXCollections.observableArrayList();
    private Statement stm;
	private	ResultSet rs;
   
    //default constructor
    public Document(){
    }

    public Document (int id, String name, String type, String path, int owner){

        Calendar currentDate = Calendar.getInstance();  //gets the current date;
        SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); //format current date

        this.id = new SimpleIntegerProperty(id);
        this.name = new SimpleStringProperty(name);
        this.type = new SimpleStringProperty(type);
        this.path = new SimpleStringProperty(path);
        this.creationDate = new SimpleStringProperty(formatDate.format(currentDate.getTime()));
        this.owner = new SimpleIntegerProperty(owner);
    }
    
    //getters
    public int getDocumentId(){return id.get();}
    public String getName(){return name.get();}
    public String getType(){return type.get();}
    public String getPath(){return path.get();}
    public String getCreationdate(){return creationDate.get();}
    
    public void insertDocument(String name, String type, String path, int id){
        
        try{
            CallableStatement insertDoc = DatabaseConnector.connection.prepareCall("{call insertdocument(?,?,?,TO_TIMESTAMP(?, 'DD-MM-YYYY HH24:MI:SS'),?)}");

            Calendar currentDate = Calendar.getInstance(); //get current date
            SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

            insertDoc.setString(1,name);
            insertDoc.setString(2, type);
            insertDoc.setString(3,path);
            insertDoc.setString(4,formatDate.format(currentDate.getTime()));
            insertDoc.setInt(5,id);

            //executeQuery
            insertDoc.execute();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    //returns all Documents
	public ObservableList<Document> getAllClientDocuments(){
	/* Returns an obserbable list which contains all documents*/
		try{
			ResultSet queryResult = getDocumentDatabaseTableResultSet();
			
			// add data to observablelist 
			while (queryResult.next()){
				allDocuments.add(new Document(queryResult.getInt("id"), queryResult.getString("name"), queryResult.getString("type"), queryResult.getString("path"), 
									queryResult.getInt("ownedby")));
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return allDocuments;
	}

	public ResultSet getDocumentDatabaseTableResultSet() throws Exception{
		/* Returns a resultset of a SQL select executed against the Documents database table */

		try{
            String queryString = "SELECT id, name, type, path, creationdate, ownedby FROM Documentos WHERE ownedby = ?";
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

    public int getDocumentId(String path){
	    int id = -1;
	    
        try{
			ResultSet rs = null;
			PreparedStatement stm = DatabaseConnector.connection.prepareStatement("SELECT id FROM Documentos WHERE path = ?");
			
            stm.setString(1,path);

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
