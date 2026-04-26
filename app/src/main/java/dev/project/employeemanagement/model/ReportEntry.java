package dev.project.employeemanagement.model;

public class ReportEntry {
  private String label;
  private double total;

  public ReportEntry(String label, double total) {
    this.label = label;
    this.total = total;
  }

  public String getLabel() {
    return label;
  }

  public double getTotal() {
    return total;
  }
}
