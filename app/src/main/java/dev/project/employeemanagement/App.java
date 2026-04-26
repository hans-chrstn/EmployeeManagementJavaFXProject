package dev.project.employeemanagement;

import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.model.PayHistoryEntry;
import dev.project.employeemanagement.model.ReportEntry;
import dev.project.employeemanagement.repository.EmployeeRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class App extends Application {
  private final EmployeeRepository repository = new EmployeeRepository();

  // Employee database view components
  private final TableView<Employee> table = new TableView<>();
  private final TextField searchField = new TextField();
  private final Label statusLabel = new Label("System Ready");
  private final TextField nameField = new TextField();
  private final TextField ssnField = new TextField();
  private final TextField salaryField = new TextField();
  private final TextField jobTitleField = new TextField();
  private final TextField divisionField = new TextField();
  private Employee selectedEmployee;

  // Report view tables
  private final TableView<PayHistoryEntry> payHistoryTable = new TableView<>();
  private final TableView<ReportEntry> jobTitleTable = new TableView<>();
  private final TableView<ReportEntry> divisionTable = new TableView<>();

  private static final String[] MONTHS = {
    "January", "February", "March", "April", "May", "June",
    "July", "August", "September", "October", "November", "December"
  };

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Employee Management System - Company Z");

    setupTable();
    setupPayHistoryTable();
    setupGroupReportTable(jobTitleTable, "Job Title");
    setupGroupReportTable(divisionTable, "Division");

    Button employeesBtn = new Button("Employee Database");
    Button payHistoryBtn = new Button("Pay History Report");
    Button jobTitleBtn = new Button("Pay by Job Title");
    Button divisionBtn = new Button("Pay by Division");
    Button[] allNavBtns = {employeesBtn, payHistoryBtn, jobTitleBtn, divisionBtn};

    VBox navRail = new VBox(0);
    navRail.getStyleClass().add("nav-rail");
    navRail.setPrefWidth(240);
    Label brand = new Label("COMPANY Z");
    brand.getStyleClass().add("brand-title");
    for (Button btn : allNavBtns) {
      btn.getStyleClass().add("nav-button");
      btn.setMaxWidth(Double.MAX_VALUE);
    }
    employeesBtn.getStyleClass().add("nav-button-active");
    navRail.getChildren().addAll(brand, employeesBtn, payHistoryBtn, jobTitleBtn, divisionBtn);

    VBox empView = buildEmployeeView();
    VBox phView = buildPayHistoryView();
    VBox jtView = buildGroupReportView("Total Pay by Job Title", jobTitleTable, true);
    VBox divView = buildGroupReportView("Total Pay by Division", divisionTable, false);

    HBox statusBar = new HBox(statusLabel);
    statusBar.getStyleClass().add("status-bar");

    BorderPane root = new BorderPane();
    root.setLeft(navRail);
    root.setCenter(empView);
    root.setBottom(statusBar);

    employeesBtn.setOnAction(e -> switchView(root, empView, employeesBtn, allNavBtns));
    payHistoryBtn.setOnAction(e -> switchView(root, phView, payHistoryBtn, allNavBtns));
    jobTitleBtn.setOnAction(e -> switchView(root, jtView, jobTitleBtn, allNavBtns));
    divisionBtn.setOnAction(e -> switchView(root, divView, divisionBtn, allNavBtns));

    Scene scene = new Scene(root, 1500, 950);
    try {
      scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    } catch (Exception e) {
      System.out.println("CSS could not be loaded.");
    }

    primaryStage.setScene(scene);
    primaryStage.show();

    jobTitleField.setEditable(false);
    jobTitleField.setPromptText("View only");
    divisionField.setEditable(false);
    divisionField.setPromptText("View only");
    handleSearch();
  }

  private void switchView(BorderPane root, VBox view, Button activeBtn, Button[] allBtns) {
    root.setCenter(view);
    for (Button b : allBtns) b.getStyleClass().remove("nav-button-active");
    activeBtn.getStyleClass().add("nav-button-active");
    updateStatus("Viewing: " + activeBtn.getText());
  }

  // ── Employee Database View ────────────────────────────────────────────────

  private VBox buildEmployeeView() {
    HBox header = new HBox();
    header.getStyleClass().add("header-area");
    header.setAlignment(Pos.CENTER_LEFT);
    Label title = new Label("Employee Database");
    title.getStyleClass().add("header-title");
    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);
    searchField.setPromptText("Search by name, SSN, or ID...");
    searchField.setPrefWidth(350);
    searchField.setOnKeyReleased(e -> handleSearch());
    header.getChildren().addAll(title, spacer, searchField);

    VBox tableCard = new VBox(table);
    tableCard.getStyleClass().add("card");
    VBox.setVgrow(table, Priority.ALWAYS);

    VBox sidebarDetail = createDetailPane();
    sidebarDetail.getStyleClass().add("card");

    HBox workspace = new HBox(30, tableCard, sidebarDetail);
    workspace.setPadding(new Insets(35));
    HBox.setHgrow(tableCard, Priority.ALWAYS);

    VBox view = new VBox(header, workspace);
    view.getStyleClass().add("content-area");
    VBox.setVgrow(workspace, Priority.ALWAYS);
    return view;
  }

  private void setupTable() {
    TableColumn<Employee, Integer> idCol = new TableColumn<>("ID");
    idCol.setCellValueFactory(new PropertyValueFactory<>("empid"));
    idCol.setPrefWidth(70);

    TableColumn<Employee, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<Employee, String> ssnCol = new TableColumn<>("SSN");
    ssnCol.setCellValueFactory(new PropertyValueFactory<>("ssn"));

    TableColumn<Employee, Double> salaryCol = new TableColumn<>("Salary");
    salaryCol.setCellValueFactory(new PropertyValueFactory<>("salary"));
    salaryCol.setCellFactory(
        tc -> new TableCell<>() {
          @Override
          protected void updateItem(Double val, boolean empty) {
            super.updateItem(val, empty);
            if (empty || val == null) setText(null);
            else setText(String.format("$%,.2f", val));
          }
        });

    TableColumn<Employee, String> jobTitleCol = new TableColumn<>("Job Title");
    jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));

    TableColumn<Employee, String> divisionCol = new TableColumn<>("Division");
    divisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));

    table.getColumns().addAll(idCol, nameCol, ssnCol, salaryCol, jobTitleCol, divisionCol);
    table.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        selectedEmployee = newVal;
        nameField.setText(newVal.getName());
        ssnField.setText(newVal.getSsn());
        salaryField.setText(String.valueOf(newVal.getSalary()));
        jobTitleField.setText(newVal.getJobTitle());
        divisionField.setText(newVal.getDivision());
        updateStatus("Selected: " + newVal.getName());
      }
    });
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
  }

  private VBox createDetailPane() {
    VBox container = new VBox(25);
    container.setPadding(new Insets(35));
    container.setPrefWidth(420);

    Label sectionTitle = new Label("UPDATE SELECTED EMPLOYEE");
    sectionTitle.getStyleClass().add("section-header");

    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(18);
    addFormField(grid, "Name", nameField, 0);
    addFormField(grid, "SSN", ssnField, 1);
    addFormField(grid, "Salary", salaryField, 2);
    addFormField(grid, "Job Title (View Only)", jobTitleField, 3);
    addFormField(grid, "Division (View Only)", divisionField, 4);

    Button saveBtn = new Button("Save Changes");
    saveBtn.getStyleClass().add("button-primary");
    saveBtn.setMaxWidth(Double.MAX_VALUE);
    saveBtn.setOnAction(e -> handleUpdate());

    Separator sep = new Separator();
    Label batchTitle = new Label("BATCH SALARY INCREASE");
    batchTitle.getStyleClass().add("section-header");

    TextField pctField = new TextField();
    pctField.setPromptText("% Increase");
    TextField minField = new TextField();
    minField.setPromptText("Min Salary");
    TextField maxField = new TextField();
    maxField.setPromptText("Max Salary");

    HBox batchInput = new HBox(12, pctField, minField, maxField);
    Button batchBtn = new Button("Apply Increase");
    batchBtn.getStyleClass().add("button-success");
    batchBtn.setMaxWidth(Double.MAX_VALUE);
    batchBtn.setOnAction(e -> handleSalaryIncrease(pctField, minField, maxField));

    container.getChildren().addAll(sectionTitle, grid, saveBtn, sep, batchTitle, batchInput, batchBtn);
    return container;
  }

  private void addFormField(GridPane grid, String labelText, TextField field, int row) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    grid.add(label, 0, row);
    grid.add(field, 1, row);
    GridPane.setHgrow(field, Priority.ALWAYS);
  }

  // ── Pay History Report View ───────────────────────────────────────────────

  private VBox buildPayHistoryView() {
    int currentYear = LocalDate.now().getYear();
    int currentMonth = LocalDate.now().getMonthValue();

    HBox header = buildReportHeader("Employee Pay History Report");

    ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(MONTHS));
    monthBox.setValue(MONTHS[currentMonth - 1]);
    TextField yearField = new TextField(String.valueOf(currentYear));
    yearField.setPrefWidth(80);
    Button runBtn = new Button("Run Report");
    runBtn.getStyleClass().add("button-primary");

    HBox controls = new HBox(12, new Label("Month:"), monthBox, new Label("Year:"), yearField, runBtn);
    controls.setPadding(new Insets(20, 35, 0, 35));
    controls.setAlignment(Pos.CENTER_LEFT);

    VBox tableCard = new VBox(payHistoryTable);
    tableCard.getStyleClass().add("card");
    VBox.setVgrow(payHistoryTable, Priority.ALWAYS);

    VBox workspace = new VBox(20, controls, tableCard);
    workspace.setPadding(new Insets(20, 35, 35, 35));
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    runBtn.setOnAction(e -> {
      int month = monthBox.getSelectionModel().getSelectedIndex() + 1;
      int year;
      try {
        year = Integer.parseInt(yearField.getText().trim());
      } catch (NumberFormatException ex) {
        showAlert("Invalid Year", "Please enter a valid 4-digit year.");
        return;
      }
      try {
        List<PayHistoryEntry> rows = repository.getPayHistory(month, year);
        payHistoryTable.setItems(FXCollections.observableArrayList(rows));
        updateStatus(rows.size() + " pay records found for " + MONTHS[month - 1] + " " + year + ".");
      } catch (SQLException ex) {
        showAlert("Database Error", "Failed to load pay history: " + ex.getMessage());
      }
    });

    VBox view = new VBox(header, workspace);
    view.getStyleClass().add("content-area");
    VBox.setVgrow(workspace, Priority.ALWAYS);
    return view;
  }

  private void setupPayHistoryTable() {
    TableColumn<PayHistoryEntry, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<PayHistoryEntry, String> ssnCol = new TableColumn<>("SSN");
    ssnCol.setCellValueFactory(new PropertyValueFactory<>("ssn"));

    TableColumn<PayHistoryEntry, String> jobTitleCol = new TableColumn<>("Job Title");
    jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));

    TableColumn<PayHistoryEntry, String> divisionCol = new TableColumn<>("Division");
    divisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));

    TableColumn<PayHistoryEntry, String> payDateCol = new TableColumn<>("Pay Date");
    payDateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));

    TableColumn<PayHistoryEntry, Double> earningsCol = new TableColumn<>("Earnings");
    earningsCol.setCellValueFactory(new PropertyValueFactory<>("earnings"));
    earningsCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> fedTaxCol = new TableColumn<>("Fed Tax");
    fedTaxCol.setCellValueFactory(new PropertyValueFactory<>("fedTax"));
    fedTaxCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> stateTaxCol = new TableColumn<>("State Tax");
    stateTaxCol.setCellValueFactory(new PropertyValueFactory<>("stateTax"));
    stateTaxCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> retire401kCol = new TableColumn<>("401k");
    retire401kCol.setCellValueFactory(new PropertyValueFactory<>("retire401k"));
    retire401kCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> healthCareCol = new TableColumn<>("Healthcare");
    healthCareCol.setCellValueFactory(new PropertyValueFactory<>("healthCare"));
    healthCareCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> netPayCol = new TableColumn<>("Net Pay");
    netPayCol.setCellValueFactory(new PropertyValueFactory<>("netPay"));
    netPayCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    payHistoryTable.getColumns().addAll(
        nameCol, ssnCol, jobTitleCol, divisionCol, payDateCol,
        earningsCol, fedTaxCol, stateTaxCol, retire401kCol, healthCareCol, netPayCol);
    payHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
  }

  // ── Group Report Views (Job Title & Division) ─────────────────────────────

  private VBox buildGroupReportView(String titleText, TableView<ReportEntry> tbl, boolean isJobTitle) {
    int currentYear = LocalDate.now().getYear();
    int currentMonth = LocalDate.now().getMonthValue();

    HBox header = buildReportHeader(titleText);

    ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(MONTHS));
    monthBox.setValue(MONTHS[currentMonth - 1]);
    TextField yearField = new TextField(String.valueOf(currentYear));
    yearField.setPrefWidth(80);
    Button runBtn = new Button("Run Report");
    runBtn.getStyleClass().add("button-primary");

    HBox controls = new HBox(12, new Label("Month:"), monthBox, new Label("Year:"), yearField, runBtn);
    controls.setPadding(new Insets(20, 35, 0, 35));
    controls.setAlignment(Pos.CENTER_LEFT);

    Label summaryLabel = new Label();
    summaryLabel.getStyleClass().add("section-header");
    summaryLabel.setPadding(new Insets(0, 35, 0, 35));

    VBox tableCard = new VBox(tbl);
    tableCard.getStyleClass().add("card");
    VBox.setVgrow(tbl, Priority.ALWAYS);

    VBox workspace = new VBox(20, controls, tableCard, summaryLabel);
    workspace.setPadding(new Insets(20, 35, 35, 35));
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    runBtn.setOnAction(e -> {
      int month = monthBox.getSelectionModel().getSelectedIndex() + 1;
      int year;
      try {
        year = Integer.parseInt(yearField.getText().trim());
      } catch (NumberFormatException ex) {
        showAlert("Invalid Year", "Please enter a valid 4-digit year.");
        return;
      }
      try {
        List<ReportEntry> rows = isJobTitle
            ? repository.getTotalPayByJobTitle(month, year)
            : repository.getTotalPayByDivision(month, year);
        tbl.setItems(FXCollections.observableArrayList(rows));
        double grandTotal = rows.stream().mapToDouble(ReportEntry::getTotal).sum();
        summaryLabel.setText(String.format(
            "Grand Total: $%,.2f  |  %d group(s)  |  %s %d",
            grandTotal, rows.size(), MONTHS[month - 1], year));
        updateStatus(rows.size() + " groups for " + MONTHS[month - 1] + " " + year + ".");
      } catch (SQLException ex) {
        showAlert("Database Error", "Failed to load report: " + ex.getMessage());
      }
    });

    VBox view = new VBox(header, workspace);
    view.getStyleClass().add("content-area");
    VBox.setVgrow(workspace, Priority.ALWAYS);
    return view;
  }

  private void setupGroupReportTable(TableView<ReportEntry> tbl, String groupLabel) {
    TableColumn<ReportEntry, String> labelCol = new TableColumn<>(groupLabel);
    labelCol.setCellValueFactory(new PropertyValueFactory<>("label"));

    TableColumn<ReportEntry, Double> totalCol = new TableColumn<>("Total Pay");
    totalCol.setCellValueFactory(new PropertyValueFactory<>("total"));
    totalCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean empty) {
        super.updateItem(v, empty);
        setText(empty || v == null ? null : String.format("$%,.2f", v));
      }
    });

    tbl.getColumns().addAll(labelCol, totalCol);
    tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
  }

  // ── Shared Helpers ────────────────────────────────────────────────────────

  private HBox buildReportHeader(String titleText) {
    HBox header = new HBox();
    header.getStyleClass().add("header-area");
    header.setAlignment(Pos.CENTER_LEFT);
    Label title = new Label(titleText);
    title.getStyleClass().add("header-title");
    header.getChildren().add(title);
    return header;
  }

  private void handleSearch() {
    try {
      List<Employee> results = repository.searchEmployees(searchField.getText());
      table.setItems(FXCollections.observableArrayList(results));
      updateStatus(results.size() + " records found.");
    } catch (SQLException e) {
      showAlert("Database Error", "Failed to search employees: " + e.getMessage());
    }
  }

  private void handleUpdate() {
    if (selectedEmployee == null) {
      showAlert("No Selection", "Please select an employee first.");
      return;
    }
    double parsedSalary;
    try {
      parsedSalary = Double.parseDouble(salaryField.getText().trim());
    } catch (NumberFormatException e) {
      showAlert("Error", "Salary must be a valid number.");
      return;
    }
    try {
      selectedEmployee.setName(nameField.getText().trim());
      selectedEmployee.setSsn(ssnField.getText().trim());
      selectedEmployee.setSalary(parsedSalary);
      selectedEmployee.setJobTitle(jobTitleField.getText());
      selectedEmployee.setDivision(divisionField.getText());
      repository.updateEmployee(selectedEmployee);
      handleSearch();
      updateStatus("Employee updated successfully.");
      showAlert("Success", "Employee updated successfully.");
    } catch (Exception e) {
      showAlert("Error", "Check your inputs and try again: " + e.getMessage());
    }
  }

  private void handleSalaryIncrease(TextField p, TextField min, TextField max) {
    String pctInput = p.getText().trim();
    String minInput = min.getText().trim();
    String maxInput = max.getText().trim();

    if (pctInput.isEmpty() || minInput.isEmpty() || maxInput.isEmpty()) {
      showAlert("Missing Input",
          "Enter all three values: percentage increase, minimum salary, and maximum salary.");
      return;
    }

    String cleanedPct = pctInput.replace("%", "").trim();
    String cleanedMin = minInput.replace(",", "").replace("$", "").replace(">", "").trim();
    String cleanedMax = maxInput.replace(",", "").replace("$", "").replace("<", "").trim();

    double pct, minSal, maxSal;
    try {
      pct = Double.parseDouble(cleanedPct);
      minSal = Double.parseDouble(cleanedMin);
      maxSal = Double.parseDouble(cleanedMax);
    } catch (NumberFormatException e) {
      showAlert("Invalid Salary Range Input",
          "Use numbers only. Examples: 3.2, 50000, 100000. You can also type 3.2%, $50,000, or $100,000.");
      return;
    }

    if (pct <= 0) {
      showAlert("Invalid Percentage", "Percentage increase must be greater than 0.");
      return;
    }
    if (minSal < 0 || maxSal < 0) {
      showAlert("Invalid Salary Range", "Minimum and maximum salary must be 0 or greater.");
      return;
    }
    if (minSal >= maxSal) {
      showAlert("Invalid Salary Range",
          "Minimum salary must be less than maximum salary. Example: 58000 and 105000.");
      return;
    }

    try {
      repository.updateSalariesInRange(pct, minSal, maxSal);
      handleSearch();
      updateStatus(String.format(
          "Applied %.2f%% increase for salaries from $%,.2f up to $%,.2f.", pct, minSal, maxSal));
      showAlert("Success", String.format(
          "Applied %.2f%% increase for employees with salaries from $%,.2f up to $%,.2f.",
          pct, minSal, maxSal));
    } catch (Exception e) {
      showAlert("Database Error", "Could not update salaries: " + e.getMessage());
    }
  }

  private void updateStatus(String msg) {
    statusLabel.setText("Status: " + msg);
  }

  private void showAlert(String title, String content) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(content);
    alert.showAndWait();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
