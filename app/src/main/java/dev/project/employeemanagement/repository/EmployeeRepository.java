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
    String normalizedQuery = query == null ? "" : query.trim();
    String normalizedSsn = normalizedQuery.replaceAll("[^0-9]", "");

    String sql = "SELECT e.empid, CONCAT(e.Fname, ' ', e.Lname) AS name, e.SSN, e.Salary, " +
        "jt.job_title, d.Name AS division " +
        "FROM employees e " +
        "LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid " +
        "LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id " +
        "LEFT JOIN employee_division ed ON e.empid = ed.empid " +
        "LEFT JOIN division d ON ed.div_ID = d.ID " +
        "WHERE ? = '' " +
        "OR CONCAT(e.Fname, ' ', e.Lname) LIKE ? " +
        "OR e.Fname LIKE ? " +
        "OR e.Lname LIKE ? " +
        "OR REPLACE(e.SSN, '-', '') LIKE ? " +
        "OR CAST(e.empid AS CHAR) LIKE ? " +
        "ORDER BY e.empid";

    List<Employee> results = new ArrayList<>();
    String likeQuery = "%" + normalizedQuery + "%";
    String likeSsnQuery = "%" + normalizedSsn + "%";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, normalizedQuery);
      stmt.setString(2, likeQuery);
      stmt.setString(3, likeQuery);
      stmt.setString(4, likeQuery);
      stmt.setString(5, likeSsnQuery);
      stmt.setString(6, likeQuery);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    }
    return results;
  }

  public void updateEmployee(Employee emp) throws SQLException {
    String sql = "UPDATE employees SET Fname = ?, Lname = ?, SSN = ?, Salary = ? WHERE empid = ?";

    String[] parts = emp.getName().split(" ", 2);
    String fName = parts.length > 0 ? parts[0] : "";
    String lName = parts.length > 1 ? parts[1] : "";
    String normalizedSsn = emp.getSsn() == null ? "" : emp.getSsn().replaceAll("[^0-9]", "");

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, fName);
      stmt.setString(2, lName);
      stmt.setString(3, normalizedSsn);
      stmt.setDouble(4, emp.getSalary());
      stmt.setInt(5, emp.getEmpid());

      stmt.executeUpdate();
    }
  }

  public void updateSalariesInRange(double percentage, double minSalary, double maxSalary)
      throws SQLException {
    String sql = "UPDATE employees SET Salary = Salary + (Salary * ? / 100) WHERE Salary >= ? AND Salary < ?";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDouble(1, percentage);
      stmt.setDouble(2, minSalary);
      stmt.setDouble(3, maxSalary);

      stmt.executeUpdate();
    }
  }

  public void addEmployee(Employee emp) throws SQLException {
    String sql = "INSERT INTO employees (Fname, Lname, SSN, Salary) VALUES (?, ?, ?, ?)";

    String[] parts = emp.getName().split(" ", 2);
    String fName = parts.length > 0 ? parts[0] : "";
    String lName = parts.length > 1 ? parts[1] : "";
    String normalizedSsn = emp.getSsn() == null ? "" : emp.getSsn().replaceAll("[^0-9]", "");

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, fName);
      stmt.setString(2, lName);
      stmt.setString(3, normalizedSsn);
      stmt.setDouble(4, emp.getSalary());

      stmt.executeUpdate();
    }
  }

  public void deleteEmployee(int empid) throws SQLException {
    String deletePayrollSql = "DELETE FROM payroll WHERE empid = ?";
    String deleteJobTitlesSql = "DELETE FROM employee_job_titles WHERE empid = ?";
    String deleteDivisionSql = "DELETE FROM employee_division WHERE empid = ?";
    String deleteEmployeeSql = "DELETE FROM employees WHERE empid = ?";

    try (Connection conn = DbConfig.getConnection()) {
      conn.setAutoCommit(false);

      try (PreparedStatement deletePayrollStmt = conn.prepareStatement(deletePayrollSql);
          PreparedStatement deleteJobTitlesStmt = conn.prepareStatement(deleteJobTitlesSql);
          PreparedStatement deleteDivisionStmt = conn.prepareStatement(deleteDivisionSql);
          PreparedStatement deleteEmployeeStmt = conn.prepareStatement(deleteEmployeeSql)) {

        deletePayrollStmt.setInt(1, empid);
        deletePayrollStmt.executeUpdate();

        deleteJobTitlesStmt.setInt(1, empid);
        deleteJobTitlesStmt.executeUpdate();

        deleteDivisionStmt.setInt(1, empid);
        deleteDivisionStmt.executeUpdate();

        deleteEmployeeStmt.setInt(1, empid);
        deleteEmployeeStmt.executeUpdate();

        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  public Employee mapRow(ResultSet rs) throws SQLException {
    return new Employee(
        rs.getInt("empid"),
        rs.getString("name"),
        rs.getString("SSN"),
        rs.getDouble("Salary"),
        rs.getString("job_title"),
        rs.getString("division"));
  }
}
