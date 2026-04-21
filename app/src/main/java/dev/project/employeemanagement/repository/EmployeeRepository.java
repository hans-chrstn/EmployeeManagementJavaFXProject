package dev.project.employeemanagement.repository;

import dev.project.employeemanagement.model.Division;
import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.model.FullTimeEmployee;
import dev.project.employeemanagement.model.JobTitle;
import dev.project.employeemanagement.model.Payroll;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class EmployeeRepository implements IEmployeeRepository {

  @Override
  public List<Employee> searchEmployees(String query) throws SQLException {
    String normalizedQuery = query == null ? "" : query.trim();
    String normalizedSsn = normalizedQuery.replaceAll("[^0-9]", "");

    String sql =
        "SELECT e.empid, e.Fname, e.Lname, e.email, e.HireDate, e.Salary, e.SSN, "
            + "jt.job_title_id, jt.job_title, "
            + "d.ID AS division_id, d.Name AS division_name, d.city, d.addressLine1, d.addressLine2, d.state, d.country, d.postalCode "
            + "FROM employees e "
            + "LEFT JOIN employee_job_titles ejt ON e.empid = ejt.empid "
            + "LEFT JOIN job_titles jt ON ejt.job_title_id = jt.job_title_id "
            + "LEFT JOIN employee_division ed ON e.empid = ed.empid "
            + "LEFT JOIN division d ON ed.div_ID = d.ID "
            + "WHERE ? = '' "
            + "OR CONCAT(e.Fname, ' ', e.Lname) LIKE ? "
            + "OR e.Fname LIKE ? "
            + "OR e.Lname LIKE ? "
            + "OR (? <> '' AND REPLACE(e.SSN, '-', '') LIKE ?) "
            + "OR CAST(e.empid AS CHAR) = ? "
            + "ORDER BY e.empid";

    List<Employee> results = new ArrayList<>();
    String likeQuery = "%" + normalizedQuery + "%";
    String likeSsnQuery = "%" + normalizedSsn + "%";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {

      stmt.setString(1, normalizedQuery);
      stmt.setString(2, likeQuery);
      stmt.setString(3, likeQuery);
      stmt.setString(4, likeQuery);
      stmt.setString(5, normalizedSsn);
      stmt.setString(6, likeSsnQuery);
      stmt.setString(7, normalizedQuery);

      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(mapRow(rs));
        }
      }
    }
    return results;
  }

  @Override
  public void updateEmployee(Employee emp) throws SQLException {
    String sql = "UPDATE employees SET Fname = ?, Lname = ?, email = ?, HireDate = ?, Salary = ?, SSN = ? WHERE empid = ?";

    try (Connection conn = DbConfig.getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, emp.getFname());
        stmt.setString(2, emp.getLname());
        stmt.setString(3, emp.getEmail());
        stmt.setDate(4, emp.getHireDate() != null ? Date.valueOf(emp.getHireDate()) : null);
        stmt.setDouble(5, emp.getSalary());
        stmt.setString(6, emp.getSsn());
        stmt.setInt(7, emp.getEmpid());
        stmt.executeUpdate();

        if (emp.getJobTitle() != null) {
          updateJobTitleAssociation(conn, emp.getEmpid(), emp.getJobTitle().getId());
        }

        if (emp.getDivision() != null) {
          updateDivisionAssociation(conn, emp.getEmpid(), emp.getDivision().getId());
        }

        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  private void updateJobTitleAssociation(Connection conn, int empid, int jobTitleId) throws SQLException {
    String deleteSql = "DELETE FROM employee_job_titles WHERE empid = ?";
    String insertSql = "INSERT INTO employee_job_titles (empid, job_title_id) VALUES (?, ?)";
    
    try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
      delStmt.setInt(1, empid);
      delStmt.executeUpdate();
    }
    try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
      insStmt.setInt(1, empid);
      insStmt.setInt(2, jobTitleId);
      insStmt.executeUpdate();
    }
  }

  private void updateDivisionAssociation(Connection conn, int empid, int divId) throws SQLException {
    String deleteSql = "DELETE FROM employee_division WHERE empid = ?";
    String insertSql = "INSERT INTO employee_division (empid, div_ID) VALUES (?, ?)";
    
    try (PreparedStatement delStmt = conn.prepareStatement(deleteSql)) {
      delStmt.setInt(1, empid);
      delStmt.executeUpdate();
    }
    try (PreparedStatement insStmt = conn.prepareStatement(insertSql)) {
      insStmt.setInt(1, empid);
      insStmt.setInt(2, divId);
      insStmt.executeUpdate();
    }
  }

  @Override
  public void updateSalariesInRange(double percentage, double minSalary, double maxSalary)
      throws SQLException {
    String sql =
        "UPDATE employees SET Salary = Salary + (Salary * ? / 100) WHERE Salary >= ? AND Salary <"
            + " ?";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDouble(1, percentage);
      stmt.setDouble(2, minSalary);
      stmt.setDouble(3, maxSalary);

      stmt.executeUpdate();
    }
  }

  @Override
  public void addEmployee(Employee emp) throws SQLException {
    String sql = "INSERT INTO employees (Fname, Lname, email, HireDate, Salary, SSN) VALUES (?, ?, ?, ?, ?, ?)";

    try (Connection conn = DbConfig.getConnection()) {
      conn.setAutoCommit(false);
      try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
        stmt.setString(1, emp.getFname());
        stmt.setString(2, emp.getLname());
        stmt.setString(3, emp.getEmail());
        stmt.setDate(4, emp.getHireDate() != null ? Date.valueOf(emp.getHireDate()) : null);
        stmt.setDouble(5, emp.getSalary());
        stmt.setString(6, emp.getSsn());
        stmt.executeUpdate();

        try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
          if (generatedKeys.next()) {
            int empid = generatedKeys.getInt(1);
            emp.setEmpid(empid);
            
            if (emp.getJobTitle() != null) {
              updateJobTitleAssociation(conn, empid, emp.getJobTitle().getId());
            }
            if (emp.getDivision() != null) {
              updateDivisionAssociation(conn, empid, emp.getDivision().getId());
            }
          }
        }
        conn.commit();
      } catch (SQLException e) {
        conn.rollback();
        throw e;
      } finally {
        conn.setAutoCommit(true);
      }
    }
  }

  @Override
  public void deleteEmployee(int empid) throws SQLException {
    String sql = "DELETE FROM employees WHERE empid = ?";

    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, empid);
      stmt.executeUpdate();
    }
  }

  @Override
  public List<JobTitle> getAllJobTitles() throws SQLException {
    String sql = "SELECT * FROM job_titles";
    List<JobTitle> titles = new ArrayList<>();
    try (Connection conn = DbConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        titles.add(new JobTitle(rs.getInt("job_title_id"), rs.getString("job_title")));
      }
    }
    return titles;
  }

  @Override
  public List<Division> getAllDivisions() throws SQLException {
    String sql = "SELECT * FROM division";
    List<Division> divisions = new ArrayList<>();
    try (Connection conn = DbConfig.getConnection();
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery(sql)) {
      while (rs.next()) {
        divisions.add(new Division(
            rs.getInt("ID"),
            rs.getString("Name"),
            rs.getString("city"),
            rs.getString("addressLine1"),
            rs.getString("addressLine2"),
            rs.getString("state"),
            rs.getString("country"),
            rs.getString("postalCode")
        ));
      }
    }
    return divisions;
  }

  @Override
  public void addPayroll(Payroll p) throws SQLException {
    String sql = "INSERT INTO payroll (pay_date, earnings, fed_tax, fed_med, fed_ss, state_tax, retire_401k, health_care, empid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(p.getPayDate()));
      stmt.setDouble(2, p.getEarnings());
      stmt.setDouble(3, p.getFedTax());
      stmt.setDouble(4, p.getFedMed());
      stmt.setDouble(5, p.getFedSs());
      stmt.setDouble(6, p.getStateTax());
      stmt.setDouble(7, p.getRetire401k());
      stmt.setDouble(8, p.getHealthCare());
      stmt.setInt(9, p.getEmpid());
      stmt.executeUpdate();
    }
  }

  @Override
  public List<Payroll> getPayrollForEmployee(int empid) throws SQLException {
    String sql = "SELECT * FROM payroll WHERE empid = ? ORDER BY pay_date DESC";
    List<Payroll> list = new ArrayList<>();
    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, empid);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          list.add(new Payroll(
              rs.getInt("payID"),
              rs.getDate("pay_date").toLocalDate(),
              rs.getDouble("earnings"),
              rs.getDouble("fed_tax"),
              rs.getDouble("fed_med"),
              rs.getDouble("fed_ss"),
              rs.getDouble("state_tax"),
              rs.getDouble("retire_401k"),
              rs.getDouble("health_care"),
              rs.getInt("empid")
          ));
        }
      }
    }
    return list;
  }

  @Override
  public void addDivision(Division d) throws SQLException {
    String sql = "INSERT INTO division (Name, city, addressLine1, addressLine2, state, country, postalCode) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, d.getName());
      stmt.setString(2, d.getCity());
      stmt.setString(3, d.getAddressLine1());
      stmt.setString(4, d.getAddressLine2());
      stmt.setString(5, d.getState());
      stmt.setString(6, d.getCountry());
      stmt.setString(7, d.getPostalCode());
      stmt.executeUpdate();
    }
  }

  @Override
  public void addJobTitle(JobTitle jt) throws SQLException {
    String sql = "INSERT INTO job_titles (job_title) VALUES (?)";
    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, jt.getTitle());
      stmt.executeUpdate();
    }
  }

  @Override
  public void deleteDivision(int id) throws SQLException {
    String sql = "DELETE FROM division WHERE ID = ?";
    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
    }
  }

  @Override
  public void deleteJobTitle(int id) throws SQLException {
    String sql = "DELETE FROM job_titles WHERE job_title_id = ?";
    try (Connection conn = DbConfig.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setInt(1, id);
      stmt.executeUpdate();
    }
  }

  public Employee mapRow(ResultSet rs) throws SQLException {
    JobTitle jobTitle = null;
    int jobTitleId = rs.getInt("job_title_id");
    if (!rs.wasNull()) {
      jobTitle = new JobTitle(jobTitleId, rs.getString("job_title"));
    }

    Division division = null;
    int divId = rs.getInt("division_id");
    if (!rs.wasNull()) {
      division = new Division(
          divId,
          rs.getString("division_name"),
          rs.getString("city"),
          rs.getString("addressLine1"),
          rs.getString("addressLine2"),
          rs.getString("state"),
          rs.getString("country"),
          rs.getString("postalCode")
      );
    }

    return new FullTimeEmployee(
        rs.getInt("empid"),
        rs.getString("Fname"),
        rs.getString("Lname"),
        rs.getString("email"),
        rs.getDate("HireDate") != null ? rs.getDate("HireDate").toLocalDate() : null,
        rs.getDouble("Salary"),
        rs.getString("SSN"),
        jobTitle,
        division
    );
  }
}
