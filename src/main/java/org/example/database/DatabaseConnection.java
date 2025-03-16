package org.example.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String url = "jdbc:postgresql://ep-little-thunder-a2onwd5h-pooler.eu-central-1.aws.neon.tech/dreamdevs?ssl=truesslmode=disable";
    private static final String username = "neondb_owner";
    private static final String password = "npg_vG2HtT7dbizy";

  public  static Connection getConnection(){
        Connection con = null;
        try{
            con = DriverManager.getConnection(url,username,password);
            System.out.println("Database connected successfully");

        }
        catch (SQLException e){
            System.out.println("Database connection error " + e.getMessage());
        }
        return con;
    }

    public static void main(String[] args) {
     getConnection();
    }
}
