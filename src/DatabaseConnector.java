package themis;

import java.sql.*;

public class DatabaseConnector{
	
	public static Connection connection;
	
	public static void connectToDatabase(){
		
		try{
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:5432/Themis", "postgres","P@ssw0rd");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}

	public static Connection getDatabaseConnection(){
		return connection;
	}
}
