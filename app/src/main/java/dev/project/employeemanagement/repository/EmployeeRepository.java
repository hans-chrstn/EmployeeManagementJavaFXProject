package dev.project.employeemanagement.repository;

import dev.project.employeemanagement.model.Employee;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository {
  public List<Employee> searchEmployees(String query) throws SQLException {
    String sql = "SELECT * FROM employees WHERE empid = ? OR name LIKE ? OR ssn = ?";
    List<Employee> results = new ArrayList<>();

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      int idQuery = -1;
      try {
        idQuery = Integer.parseInt(query);
      } catch (NumberFormatException e) {

      }

      stmt.setInt(1, idQuery);
      stmt.setString(2, "%" + query + "%");
      stmt.setString(3, query);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    }
    return results;
  }

  public void updateEmployee(Employee emp) throws SQLException {
    String sql =
        "UPDATE employees SET name = ?, ssn = ?, salary = ?, job_title = ?, division = ? WHERE"
            + " empid = ?";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, emp.getName());
      stmt.setString(2, emp.getSsn());
      stmt.setDouble(3, emp.getSalary());
      stmt.setString(4, emp.getJobTitle());
      stmt.setString(5, emp.getDivision());
      stmt.setInt(6, emp.getEmpid());

      stmt.executeUpdate();
    }
  }

  public void updateSalariesInRange(double percentage, double minSalary, double maxSalary)
      throws SQLException {
    String sql =
        "UPDATE employees SET salary = salary + (salary * ? / 100) WHERE salary >= ? AND salary <"
            + " ?";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDouble(1, percentage);
      stmt.setDouble(2, minSalary);
      stmt.setDouble(3, maxSalary);

      stmt.executeUpdate();
    }
  }

  public Employee mapRow(ResultSet rs) throws SQLException {
    return new Employee(
        rs.getInt("empid"),
        rs.getString("name"),
        rs.getString("ssn"),
        rs.getDouble("salary"),
        rs.getString("job_title"),
        rs.getString("division"));
  }
}
