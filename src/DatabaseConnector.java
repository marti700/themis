package themis;

import java.sql.*;

public class DatabaseConnector{
	
	static Connection connection;
	
	public static void connectToDatabase(){
		
		try{
			connection = DriverManager.getConnection("jdbc:postgresql://localhost:3500/Themis", "postgres","P@ssw0rd");
		}
		catch (Exception e){
			e.printStackTrace();
		}
	}
}