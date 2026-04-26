package dev.project.employeemanagement.model;

public class PayHistoryEntry {
  private int empid;
  private String name;
  private String ssn;
  private String jobTitle;
  private String division;
  private String payDate;
  private double earnings;
  private double fedTax;
  private double fedMed;
  private double fedSs;
  private double stateTax;
  private double retire401k;
  private double healthCare;

  public PayHistoryEntry(
      int empid, String name, String ssn, String jobTitle, String division,
      String payDate, double earnings, double fedTax, double fedMed, double fedSs,
      double stateTax, double retire401k, double healthCare) {
    this.empid = empid;
    this.name = name;
    this.ssn = ssn;
    this.jobTitle = jobTitle;
    this.division = division;
    this.payDate = payDate;
    this.earnings = earnings;
    this.fedTax = fedTax;
    this.fedMed = fedMed;
    this.fedSs = fedSs;
    this.stateTax = stateTax;
    this.retire401k = retire401k;
    this.healthCare = healthCare;
  }

  public int getEmpid() { return empid; }
  public String getName() { return name; }
  public String getSsn() { return ssn; }
  public String getJobTitle() { return jobTitle; }
  public String getDivision() { return division; }
  public String getPayDate() { return payDate; }
  public double getEarnings() { return earnings; }
  public double getFedTax() { return fedTax; }
  public double getFedMed() { return fedMed; }
  public double getFedSs() { return fedSs; }
  public double getStateTax() { return stateTax; }
  public double getRetire401k() { return retire401k; }
  public double getHealthCare() { return healthCare; }

  public double getNetPay() {
    return earnings - fedTax - fedMed - fedSs - stateTax - retire401k - healthCare;
  }
}
