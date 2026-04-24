package dev.project.employeemanagement.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConfig {
  private static final String MYSQL_URL = "jdbc:mysql://localhost:3306/EmployeeData3";
  private static final String H2_URL = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL";

  public static Connection getConnection() throws SQLException {
    String user = System.getenv("DB_USER");
    String pass = System.getenv("DB_PASS");
    String url = System.getProperty("test.db", "false").equals("true") ? H2_URL : MYSQL_URL;

    if (url.equals(H2_URL)) {
      return DriverManager.getConnection(url, "sa", "");
    }

    if (user == null || pass == null) {
      throw new SQLException(
          "Database credentials not found. Please set DB_USER and DB_PASS in your terminal before running.");
    }

    return DriverManager.getConnection(url, user, pass);
  }
}
