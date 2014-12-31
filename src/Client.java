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

public class Client{

	private IntegerProperty id;
	private StringProperty names;
	private StringProperty lastNames;
	private StringProperty maritalStatus;
	private StringProperty idPassport;
	private StringProperty address;
	private StringProperty nationality;
	private StringProperty job;
	private StringProperty firstVisit;
	private StringProperty lastVisit;

	private ObservableList<Client> allClients = FXCollections.observableArrayList();
	private Statement stm;
	private	ResultSet rs;

	//default constructor
	public Client(){
		//this.names = null;
	}

	public Client(int id, String names, String lastNames, String nationality, String maritalStatus, String job, String idPassport, String address ){
		
		Calendar currentDate = Calendar.getInstance(); // get the current date
		SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // to format the current date
		
        //initialize properties
		this.id = new SimpleIntegerProperty(id);
		this.names = new SimpleStringProperty(names);
		this.lastNames = new SimpleStringProperty(lastNames);
		this.maritalStatus = new SimpleStringProperty(maritalStatus);
		this.idPassport = new SimpleStringProperty(idPassport);
		this.nationality = new SimpleStringProperty(nationality);
		this.job = new SimpleStringProperty(job);
		this.address = new SimpleStringProperty(address);
		this.firstVisit = new SimpleStringProperty(formatDate.format(currentDate.getTime()));
		this.lastVisit = new SimpleStringProperty(formatDate.format(currentDate.getTime()));
	}

    public Client(int id, String names, String lastNames, String nationality, String maritalStatus, String job, String idPassport, String address, String firstVisit, 
            String lastVisit ) throws Exception{
		
        //initialize properties
		this.id = new SimpleIntegerProperty(id);
		this.names = new SimpleStringProperty(names);
		this.lastNames = new SimpleStringProperty(lastNames);
		this.maritalStatus = new SimpleStringProperty(maritalStatus);
		this.idPassport = new SimpleStringProperty(idPassport);
		this.nationality = new SimpleStringProperty(nationality);
		this.job = new SimpleStringProperty(job);
		this.address = new SimpleStringProperty(address);
		this.firstVisit = new SimpleStringProperty(firstVisit);
		this.lastVisit = new SimpleStringProperty(lastVisit);
	}


	//getters
	public int getClientId(){return id.get();}
	public String getNames(){return names.get();}
	public String getLastnames(){return lastNames.get();} 
	public String getMaritalstatus(){return maritalStatus.get();} 
	public String getIdpassport() {return idPassport.get();} 
	public String getNationality(){return nationality.get();} 
	public String getJob() {return job.get();} 
	public String getAddress(){return address.get();}
	public String getFirstvisit() {return firstVisit.get();} 
	public String getLastvisit(){return lastVisit.get();}

	//setters
	public void setNames(String names){this.names.set(names);}
	public void setLastnames(String lastNames){this.lastNames.set(lastNames);} 
	public void setMaritalstatus(String maritalStatus){this.maritalStatus.set(maritalStatus);} 
	public void setIdpassport(String idPassport) {this.idPassport.set(idPassport);} 
	public void setNationality(String nationality){this.nationality.set(nationality);} 
	public void setJob(String job) {this.job.set(job);} 
	public void setAddress(String address){this.address.set(address);}
	//public void setFirstvisit() {firstVisit.set();} 
	//public void setLastvisit(){lastVisit.set();}

	
	public void insertClient(String names, String lastNames, String nationality, String maritalStatus, String job, String idPassport, String address) throws Exception{
		
	//	try {
			Calendar currentDate = Calendar.getInstance(); // get the current date
			SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // to format the current date

			//prepare de call to stored procedure 
			CallableStatement stm = DatabaseConnector.connection.prepareCall ("{call insertclient(?,?,?,?,?,?,?,TO_TIMESTAMP(?, 'DD-MM-YYYY HH24:MI:SS'),TO_TIMESTAMP(?, 'DD-MM-YYYY HH24:MI:SS'))}");

			//set values
			stm.setString(1,names);
			stm.setString(2,lastNames);
			stm.setString(3,nationality);
			stm.setString(4,maritalStatus);
			stm.setString(5,job);
			stm.setString(6,idPassport);
			stm.setString(7,address);
			stm.setString(8, formatDate.format(currentDate.getTime()));
			stm.setString(9,formatDate.format(currentDate.getTime()));
			
			//execute query
			stm.execute();
	//	}
	//	catch (Exception e){
			//e.printStackTrace();
            //Dialogs.create()
                    //.owner(stage)
              //      .title("Error Dialog")
                //    .masthead("NULL")
                  //  .message("Ooops, there was an error!")
                   // .showError();
	//	}
	}

	public void editClient(String names, String lastNames, String nationality, String maritalStatus, String job, String idPassport, String address, int id){
		try{
			//prepare call to stored procedure
			CallableStatement editClient = DatabaseConnector.connection.prepareCall("{call editclient(?,?,?,?,?,?,?,?)}");

			//set values
			editClient.setString(1,names);
			editClient.setString(2,lastNames);
			editClient.setString(3,nationality);
			editClient.setString(4,maritalStatus);
			editClient.setString(5,job);
			editClient.setString(6, idPassport);
			editClient.setString(7,address);
			editClient.setInt(8, id);

			//execute statement
			editClient.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*public StringProperty getMaritalstatusProperty(){return maritalStatus;} 
	public StringProperty getIdpassportProperty(){return idPassport;} 
	public StringProperty getNationalityProperty() {return nationality;} 
	//public StringProperty getJobProperty(){return job;} 
	public StringProperty getAddressProperty() {return address;}
	//public StringProperty getFirstVisitProperty() {return firstVisit;} 
	//public StringProperty getLastVisitProperty() {return lastVisit;}
	public StringProperty getNamesProperty(){return names;}
	public StringProperty getLastnamesProperty(){return lastNames;} 

*/
	
	//returns all clients
	public ObservableList<Client> getAllClients(){
	/* Returns an obserbable list which contains all clients*/
		try{
			ResultSet queryResult = getClientDatabaseTableResultSet();
			
			// add data to observablelist 
			while (queryResult.next()){
				allClients.add(new Client(queryResult.getInt("id"), queryResult.getString("names"), queryResult.getString("lastnames"), queryResult.getString("nationality"), 
										queryResult.getString("maritalstatus"), queryResult.getString("job"), queryResult.getString("idpassport"), queryResult.getString("address"),
                                        queryResult.getString("firstvisit"), queryResult.getString("lastvisit")));
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return allClients;
	}

	public ResultSet getClientDatabaseTableResultSet() throws Exception{
		/* Returns a resultset of a SQL select executed against the Clientes database table */

		try{
			stm = DatabaseConnector.connection.createStatement();
			rs = stm.executeQuery("SELECT id, names, lastnames, nationality, maritalstatus, job, idpassport, address, firstvisit, lastvisit FROM Clientes;");
		}

		catch(Exception e){
			e.printStackTrace();
		}
		return rs;
	}

	//gets a clients id
	public int getClientId(String idPassport){
	    int id = -1;
	    
        try{
			ResultSet rs = null;
			PreparedStatement stm = DatabaseConnector.connection.prepareStatement("SELECT id FROM Clientes WHERE idpassport = ?");
			
            stm.setString(1,idPassport);

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

    public String getClientName(String idPassport){
        String names = "";
        String lastNames = "";
        String clientFullName = "";        
        
        try{
            ResultSet rs = null;
            PreparedStatement stm = DatabaseConnector.connection.prepareStatement("SELECT names, lastnames FROM Clientes WHERE idpassport = ?");

            stm.setString(1, idPassport);

            rs = stm.executeQuery();

            while(rs.next()){
               names = rs.getString("names");
               lastNames = rs.getString("lastnames");
            }

            clientFullName = names + " " + lastNames;

            return clientFullName;
        }
        catch (Exception e){
            e.printStackTrace();
        }

        return " ";
    }
}
