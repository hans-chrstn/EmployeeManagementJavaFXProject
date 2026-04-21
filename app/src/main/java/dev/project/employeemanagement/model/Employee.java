package dev.project.employeemanagement.model;

import java.time.LocalDate;

public abstract class Employee {
  private int empid;
  private String fName;
  private String lName;
  private String email;
  private LocalDate hireDate;
  private double salary;
  private String ssn;
  private JobTitle jobTitle;
  private Division division;

  public Employee() {}

  public Employee(
      int empid,
      String fName,
      String lName,
      String email,
      LocalDate hireDate,
      double salary,
      String ssn,
      JobTitle jobTitle,
      Division division) {
    this.empid = empid;
    this.fName = fName;
    this.lName = lName;
    this.email = email;
    this.hireDate = hireDate;
    this.salary = salary;
    this.ssn = ssn;
    this.jobTitle = jobTitle;
    this.division = division;
  }

  public int getEmpid() {
    return empid;
  }

  public void setEmpid(int empid) {
    this.empid = empid;
  }

  public String getName() {
    return fName + " " + lName;
  }

  public void setName(String name) {
    if (name != null && name.contains(" ")) {
      String[] parts = name.split(" ", 2);
      this.fName = parts[0];
      this.lName = parts[1];
    } else {
      this.fName = name;
      this.lName = "";
    }
  }

  public String getFname() {
    return fName;
  }

  public void setFname(String fName) {
    this.fName = fName;
  }

  public String getLname() {
    return lName;
  }

  public void setLname(String lName) {
    this.lName = lName;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDate getHireDate() {
    return hireDate;
  }

  public void setHireDate(LocalDate hireDate) {
    this.hireDate = hireDate;
  }

  public double getSalary() {
    return salary;
  }

  public void setSalary(double salary) {
    this.salary = salary;
  }

  public String getSsn() {
    return ssn;
  }

  public void setSsn(String ssn) {
    this.ssn = ssn;
  }

  public JobTitle getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(JobTitle jobTitle) {
    this.jobTitle = jobTitle;
  }

  public Division getDivision() {
    return division;
  }

  public void setDivision(Division division) {
    this.division = division;
  }

  @Override
  public String toString() {
    return "Employee [ID=" + empid + ", Name=" + fName + " " + lName + ", Salary=" + salary + "]";
  }

  public abstract double calculateMonthlyPay();
}
