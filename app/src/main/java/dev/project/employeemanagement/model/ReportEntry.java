package dev.project.employeemanagement.model;

public class ReportEntry {
    private String category;
    private double totalAmount;

    public ReportEntry(String category, double totalAmount) {
        this.category = category;
        this.totalAmount = totalAmount;
    }

    public String getCategory() { return category; }
    public double getTotalAmount() { return totalAmount; }
}
