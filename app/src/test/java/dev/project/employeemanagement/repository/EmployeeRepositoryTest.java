package dev.project.employeemanagement.repository;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.project.employeemanagement.model.Employee;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
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
      stmt.execute("DELETE FROM employees");
      stmt.execute(
          "INSERT INTO employees (empid, name, ssn, salary, job_title, division) "
              + "VALUES (1, 'John Doe', '123456789', 50000.00, 'Engineer', 'IT')");

      stmt.execute(
          "INSERT INTO employees (empid, name, ssn, salary, job_title, division) "
              + "VALUES (2, 'Jane Smith', '987654321', 60000.00, 'Manager', 'HR')");
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
    List<Employee> results = repository.searchEmployees("987654321");
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
  @DisplayName("Test: Valid Employee Data")
  void testUpdateEmployee() throws SQLException {
    Employee emp = new Employee(1, "John updated", "123456789", 55000.00, "Senior Eng", "IT");
    repository.updateEmployee(emp);

    List<Employee> results = repository.searchEmployees("John updated");
    assertEquals(1, results.size());
    assertEquals(55000.00, results.get(0).getSalary());
    assertEquals("Senior Eng", results.get(0).getJobTitle());
  }

  @Test
  @DisplayName("Test: Non-existent ID")
  void testUpdateNonExistent() throws SQLException {
    Employee emp = new Employee(999, "No one", "000000000", 10.00, "None", "None");
    assertDoesNotThrow(() -> repository.updateEmployee(emp));
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
  @DisplayName("Test: Not in range Salary Increase")
  void testSalaryIncreaseOutOfRange() throws SQLException {
    repository.updateSalariesInRange(50.0, 100000.0, 200000.0);

    List<Employee> johns = repository.searchEmployees("1");
    assertEquals(50000.00, johns.get(0).getSalary());
  }
}
