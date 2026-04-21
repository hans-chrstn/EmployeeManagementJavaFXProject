package dev.project.employeemanagement.model;

import java.time.LocalDate;

public class FullTimeEmployee extends Employee {
  public FullTimeEmployee() {
    super();
  }

  public FullTimeEmployee(
      int empid,
      String fName,
      String lName,
      String email,
      LocalDate hireDate,
      double salary,
      String ssn,
      JobTitle jobTitle,
      Division division) {
    super(empid, fName, lName, email, hireDate, salary, ssn, jobTitle, division);
  }

  @Override
  public double calculateMonthlyPay() {
    return getSalary() / 12.0;
  }
}
