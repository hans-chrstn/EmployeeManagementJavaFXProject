package dev.project.employeemanagement.model;

public class Employee {
  private int empid;
  private String name;
  private String ssn;
  private double salary;
  private String jobTitle;
  private String division;

  public Employee() {}

  public Employee(
      int empid, String name, String ssn, double salary, String jobTitle, String division) {
    this.empid = empid;
    this.name = name;
    this.ssn = ssn;
    this.salary = salary;
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
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getJobTitle() {
    return jobTitle;
  }

  public void setJobTitle(String jobTitle) {
    this.jobTitle = jobTitle;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  @Override
  public String toString() {
    return "Employee [ID=]" + empid + ", Name=" + name + ", Salary=" + salary + "]";
  }
}
