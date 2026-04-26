package dev.project.employeemanagement.repository;

import dev.project.employeemanagement.model.Division;
import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.model.JobTitle;
import dev.project.employeemanagement.model.Payroll;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import dev.project.employeemanagement.model.ReportEntry;

public interface IEmployeeRepository {
  List<Employee> searchEmployees(String query) throws SQLException;

  void updateEmployee(Employee emp) throws SQLException;

  void updateSalariesInRange(double percentage, double minSalary, double maxSalary)
      throws SQLException;

  void addEmployee(Employee emp) throws SQLException;

  void deleteEmployee(int empid) throws SQLException;

  List<JobTitle> getAllJobTitles() throws SQLException;

  List<Division> getAllDivisions() throws SQLException;

  void addPayroll(Payroll payroll) throws SQLException;

  List<Payroll> getPayrollForEmployee(int empid) throws SQLException;

  void addDivision(Division division) throws SQLException;

  void addJobTitle(JobTitle jobTitle) throws SQLException;

  void deleteDivision(int id) throws SQLException;

  void deleteJobTitle(int id) throws SQLException;

  List<ReportEntry> getTotalPayByJobTitle(int month, int year) throws SQLException;

  List<ReportEntry> getTotalPayByDivision(int month, int year) throws SQLException;
}
