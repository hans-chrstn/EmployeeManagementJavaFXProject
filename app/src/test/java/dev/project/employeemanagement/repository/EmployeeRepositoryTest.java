package dev.project.employeemanagement.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.model.FullTimeEmployee;
import dev.project.employeemanagement.model.Payroll;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EmployeeRepositoryTest {
  private EmployeeRepository repository;

  @BeforeEach
  public void setup() throws SQLException {
    repository = new EmployeeRepository();
    try (Connection conn = DbConfig.getConnection();
        Statement stmt = conn.createStatement()) {
      stmt.execute("DELETE FROM payroll");
      stmt.execute("DELETE FROM employee_division");
      stmt.execute("DELETE FROM employee_job_titles");
      stmt.execute("DELETE FROM employees");
      
      stmt.execute(
          "INSERT INTO employees (empid, Fname, Lname, SSN, Salary, email, HireDate) "
              + "VALUES (1, 'John', 'Doe', '444444444', 50000.00, 'john@example.com', '2020-01-01')");

      stmt.execute(
          "INSERT INTO employees (empid, Fname, Lname, SSN, Salary, email, HireDate) "
              + "VALUES (2, 'Jane', 'Smith', '555555555', 60000.00, 'jane@example.com', '2019-01-01')");
    }
  }

  @Test
  @DisplayName("Test: Valid Name (Partial Match)")
  void testSearchByName() throws SQLException {
    List<Employee> results = repository.searchEmployees("John");
    assertEquals(1, results.size());
    assertEquals("John Doe", results.get(0).getName());
  }

  @Test
  @DisplayName("Test: Valid SSN")
  void testSearchBySsn() throws SQLException {
    List<Employee> results = repository.searchEmployees("555555555");
    assertEquals(1, results.size());
    assertEquals("Jane Smith", results.get(0).getName());
  }

  @Test
  @DisplayName("Test: Valid ID")
  void testSearchById() throws SQLException {
    List<Employee> results = repository.searchEmployees("1");
    assertEquals(1, results.size());
    assertEquals("John Doe", results.get(0).getName());
  }

  @Test
  @DisplayName("Test: Non-existent entry")
  void testSearchNotFound() throws SQLException {
    List<Employee> results = repository.searchEmployees("Ghost");
    assertTrue(results.isEmpty());
  }

  @Test
  @DisplayName("Test: Valid Employee Data Update")
  void testUpdateEmployee() throws SQLException {
    Employee emp = new FullTimeEmployee(1, "John", "Updated", "john@example.com", LocalDate.of(2020, 1, 1), 55000.00, "444444444", null, null);
    repository.updateEmployee(emp);

    List<Employee> results = repository.searchEmployees("Updated");
    assertEquals(1, results.size());
    assertEquals(55000.00, results.get(0).getSalary());
  }

  @Test
  @DisplayName("Test: Add New Employee")
  void testAddEmployee() throws SQLException {
    Employee emp = new FullTimeEmployee(0, "New", "User", "new@example.com", LocalDate.now(), 45000.00, "999999999", null, null);
    repository.addEmployee(emp);
    
    List<Employee> results = repository.searchEmployees("New User");
    assertEquals(1, results.size());
  }

  @Test
  @DisplayName("Test: Delete Employee")
  void testDeleteEmployee() throws SQLException {
    repository.deleteEmployee(1);
    List<Employee> results = repository.searchEmployees("1");
    assertTrue(results.isEmpty());
  }

  @Test
  @DisplayName("Test: Valid Salary Increase with Specific Range")
  void testSalaryIncreaseInRange() throws SQLException {
    repository.updateSalariesInRange(10.0, 40000.0, 55000.00);

    List<Employee> johns = repository.searchEmployees("1");
    List<Employee> janes = repository.searchEmployees("2");

    assertEquals(55000.00, johns.get(0).getSalary(), 0.01);
    assertEquals(60000.00, janes.get(0).getSalary(), 0.01);
  }

  @Test
  @DisplayName("Test: Add and Retrieve Payroll")
  void testPayroll() throws SQLException {
    Payroll p = new Payroll(0, LocalDate.now(), 4000.0, 400.0, 40.0, 160.0, 200.0, 120.0, 80.0, 1);
    repository.addPayroll(p);
    
    List<Payroll> list = repository.getPayrollForEmployee(1);
    assertEquals(1, list.size());
    assertEquals(4000.0, list.get(0).getEarnings());
  }
}
