package themis;

import javafx.beans.property.StringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.sql.*;

public class Client{

	private StringProperty name;
	private StringProperty secondName;
	private StringProperty firstLastname;
	private StringProperty secondLastName;
	private StringProperty civilState;
	private StringProperty idPassport;
	private StringProperty address;
	private StringProperty nationality;
	private StringProperty job;
	//private StringProperty firstVisit;
	//private StringProperty lastVisit;

	private ObservableList<Client> allClients = FXCollections.observableArrayList();
	private Statement stm;
	private	ResultSet rs;

	
	public Client(){
		this.name = null;
		this.secondLastName = null;

	}
	public Client(String name, String secondName, String firstLastname, String secondLastName, String civilState, 
					String idPassport, String address, String nationality, String job /*, String firstVisit, String lastVisit*/){
		
		this.name = new SimpleStringProperty(name);
		this.secondName = new SimpleStringProperty(secondName);
		this.firstLastname = new SimpleStringProperty(firstLastname);
		this.secondLastName = new SimpleStringProperty(secondLastName);
		this.civilState = new SimpleStringProperty(civilState);
		this.idPassport = new SimpleStringProperty(idPassport);
		this.nationality = new SimpleStringProperty(nationality);
		this.job = new SimpleStringProperty(job);
		this.address = new SimpleStringProperty(address);
		//this.firstVisit = new SimpleStringProperty(firstVisit);
		//this.lastVisit = new SimpleStringProperty(lastVisit);
	}

	public String getName(){
		return name.get();
	}

	public String getSecondName(){
		return secondName.get();
	} 
	public String getFirstLastname(){ return firstLastname.get();} 
	public String getsecondastName(){return secondLastName.get();} 
	public String getCivilState(){return civilState.get();} 
	public String getIdPassport() {return idPassport.get();} 
	public String getNationality(){return nationality.get();} 
	public String getJob() {return job.get();} 
	public String getAddress(){return address.get();}
	//public String getFirstVisit() {firstVisit.get();} 
	//public String getLastVisit(){return lastVisit.get();}

	public StringProperty getNameProperty(){
		return name;
	}

	public StringProperty getSecondNameProperty(){
		return secondName;
	} 
	
	
	public StringProperty getfirst_last_nameProperty(){return  firstLastname;} 
	public StringProperty getsecond_last_nameProperty(){return secondLastName;} 
	public StringProperty getCivil_stateProperty(){return civilState;} 
	public StringProperty getCedula_pasaporteProperty(){return idPassport;} 
	public StringProperty getNacionalidadProperty() {return nationality;} 
	public StringProperty getJobProperty(){return job;} 
	public StringProperty getAddressProperty() {return address;}
	//public StringProperty getFirstVisitProperty() {return firstVisit;} 
	//public StringProperty getLastVisitProperty() {return lastVisit;}

	
	//returns all clients
	public ObservableList<Client> getAllClients(){
		
		try{
			ResultSet queryResult = getClientDatabaseTableResultSet();

			while (queryResult.next()){
				allClients.add(new Client(queryResult.getString("Name"), queryResult.getString("second_name"), queryResult.getString("first_last_name"), 
											queryResult.getString("second_last_name"), queryResult.getString("civil_state"), queryResult.getString("cedula_pasaporte"), 
											queryResult.getString("address"), queryResult.getString("nacionalidad"), queryResult.getString("job")));
			}
		}
		catch (Exception e){
			e.printStackTrace();
		}

		return allClients;
	}

	public ResultSet getClientDatabaseTableResultSet() throws Exception{
		try{
			this.stm = DatabaseConnector.connection.createStatement();
			this.rs = stm.executeQuery("SELECT name, second_name, first_last_name, second_last_name, nacionalidad, civil_state, job, cedula_pasaporte, address FROM clients;");
		}

		catch(Exception e){
			e.printStackTrace();
		}
		return this.rs;
	}
}