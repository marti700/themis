package themis;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.*;
import java.text.SimpleDateFormat;
import java.sql.*;

public class Client{

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

	public Client(String names, String lastNames, String maritalStatus, String idPassport, String address, String nationality, String job){
		
		Calendar currentDate = Calendar.getInstance(); // get the current date
		SimpleDateFormat formatDate = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss"); // to format the current date

		//initialize properties
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

	public String getNames(){
		return names.get();
	}

	public String getLastnames(){
		return lastNames.get();
	} 
	public String getMaritalstatus(){return maritalStatus.get();} 
	public String getIdpassport() {return idPassport.get();} 
	public String getNationality(){return nationality.get();} 
	public String getJob() {return job.get();} 
	public String getAddress(){return address.get();}
	public String getFirstVisit() {return firstVisit.get();} 
	public String getLastVisit(){return lastVisit.get();}

	public StringProperty getNamesProperty(){
		return names;
	}

	public StringProperty getLastnamesProperty(){
		return lastNames;
	} 

	public void insertClient(String names, String lastNames, String nationality, String maritalStatus, String job, String idPassport, String address){
		
		try {
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
		}
		catch (Exception e){
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
*/
	
	//returns all clients
	public ObservableList<Client> getAllClients(){
	/* Returns an obserbable list which contains all clients*/
		try{
			ResultSet queryResult = getClientDatabaseTableResultSet();
			
			// add data to observablelist 
			while (queryResult.next()){
				allClients.add(new Client(queryResult.getString("names"), queryResult.getString("lastnames"), queryResult.getString("maritalstatus"), 
										  queryResult.getString("idpassport"), queryResult.getString("address"), queryResult.getString("nationality"), queryResult.getString("job")));
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
			rs = stm.executeQuery("SELECT names, lastnames, nationality, maritalstatus, job, idpassport, address FROM Clientes;");
		}

		catch(Exception e){
			e.printStackTrace();
		}
		return rs;
	}
}