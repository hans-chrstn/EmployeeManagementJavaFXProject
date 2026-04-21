package dev.project.employeemanagement.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {
  private static final String URL = "jdbc:mysql://localhost:3306/EmployeeData3";

  // you need a .env or set your export DB_USER=youruser and DB_PASS=yourpass
  // setup that is
  // connected to your db
  public static Connection getConnection() throws SQLException {
    String user = System.getenv("DB_USER");
    String pass = System.getenv("DB_PASS");

    if (user == null || pass == null) {
      throw new SQLException(
          "Database credentials not found. Please set DB_USER and DB_PASS in your terminal before running.");
    }

    return DriverManager.getConnection(URL, user, pass);
  }
}
