package dev.project.employeemanagement;

import dev.project.employeemanagement.model.Division;
import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.model.FullTimeEmployee;
import dev.project.employeemanagement.model.JobTitle;
import dev.project.employeemanagement.model.PayHistoryEntry;
import dev.project.employeemanagement.model.Payroll;
import dev.project.employeemanagement.model.ReportEntry;
import dev.project.employeemanagement.repository.EmployeeRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Application;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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
import javafx.stage.Modality;
import javafx.stage.Stage;

public class App extends Application {
  private final EmployeeRepository repository = new EmployeeRepository();
  private final TableView<Employee> table = new TableView<>();
  private final TextField searchField = new TextField();
  private final Label statusLabel = new Label("System Ready");
  private final BorderPane root = new BorderPane();
  private VBox dbView;

  private final TextField nameField = new TextField();
  private final TextField emailField = new TextField();
  private final DatePicker hireDatePicker = new DatePicker();
  private final TextField ssnField = new TextField();
  private final TextField salaryField = new TextField();
  private final ComboBox<JobTitle> jobTitleCombo = new ComboBox<>();
  private final ComboBox<Division> divisionCombo = new ComboBox<>();
  private Employee selectedEmployee;

  @Override
  public void start(Stage primaryStage) {
    primaryStage.setTitle("Employee Management System - Company Z");

    VBox navRail = new VBox(0);
    navRail.getStyleClass().add("nav-rail");
    navRail.setPrefWidth(240);

    Label brand = new Label("Company Z");
    brand.getStyleClass().add("brand-title");

    Button employeesBtn = new Button("Employee Database");
    employeesBtn.getStyleClass().addAll("nav-button", "nav-button-active");
    employeesBtn.setMaxWidth(Double.MAX_VALUE);

    Button payHistoryBtn = new Button("Pay History Report");
    payHistoryBtn.getStyleClass().add("nav-button");
    payHistoryBtn.setMaxWidth(Double.MAX_VALUE);

    Button payAnalyticsBtn = new Button("Pay Analytics");
    payAnalyticsBtn.getStyleClass().add("nav-button");
    payAnalyticsBtn.setMaxWidth(Double.MAX_VALUE);

    List<Button> navButtons = List.of(employeesBtn, payHistoryBtn, payAnalyticsBtn);

    employeesBtn.setOnAction(e -> {
      root.setCenter(dbView);
      setActiveNav(navButtons, employeesBtn);
    });

    payHistoryBtn.setOnAction(e -> {
      root.setCenter(buildPayHistoryView());
      setActiveNav(navButtons, payHistoryBtn);
    });

    payAnalyticsBtn.setOnAction(e -> {
      root.setCenter(buildCombinedReportView());
      setActiveNav(navButtons, payAnalyticsBtn);
    });

    navRail.getChildren().addAll(brand, employeesBtn, payHistoryBtn, payAnalyticsBtn);

    dbView = createDbView();
    
    HBox statusBar = new HBox(statusLabel);
    statusBar.getStyleClass().add("status-bar");

    root.setLeft(navRail);
    root.setCenter(dbView);
    root.setBottom(statusBar);

    Scene scene = new Scene(root, 1500, 950);
    try {
      scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    } catch (Exception e) {
    }

    primaryStage.setScene(scene);
    primaryStage.show();

    loadComboBoxes();
    handleSearch();
  }

  private VBox createDbView() {
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

    setupTable();

    VBox tableCard = new VBox(table);
    tableCard.getStyleClass().add("card");
    VBox.setVgrow(table, Priority.ALWAYS);

    VBox sidebarDetail = createDetailPane();

    ScrollPane sidebarScroll = new ScrollPane(sidebarDetail);
    sidebarScroll.setFitToWidth(true);
    sidebarScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    sidebarScroll.getStyleClass().add("card");
    sidebarScroll.setPrefWidth(440);

    HBox workspace = new HBox(30, tableCard, sidebarScroll);
    workspace.setPadding(new Insets(35));
    HBox.setHgrow(tableCard, Priority.ALWAYS);

    VBox contentArea = new VBox(header, workspace);
    contentArea.getStyleClass().add("content-area");
    VBox.setVgrow(workspace, Priority.ALWAYS);
    
    return contentArea;
  }

  private void setActiveNav(List<Button> buttons, Button active) {
    for (Button b : buttons) b.getStyleClass().remove("nav-button-active");
    active.getStyleClass().add("nav-button-active");
  }

  private VBox buildPayHistoryView() {
    HBox header = new HBox();
    header.getStyleClass().add("header-area");
    header.setAlignment(Pos.CENTER_LEFT);
    Label title = new Label("Pay History Report");
    title.getStyleClass().add("header-title");
    header.getChildren().add(title);

    List<String> monthNames = List.of("January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December");
    ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(monthNames));
    monthBox.setValue("January");
    ComboBox<Integer> yearBox = new ComboBox<>(FXCollections.observableArrayList(2023, 2024, 2025, 2026));
    yearBox.setValue(2025);
    Button runBtn = new Button("Generate Report");
    runBtn.getStyleClass().add("button-primary");

    HBox selectors = new HBox(15, new Label("Period:"), monthBox, yearBox, runBtn);
    selectors.setAlignment(Pos.CENTER_LEFT);
    selectors.setPadding(new Insets(20, 35, 20, 35));

    TableView<PayHistoryEntry> payTable = new TableView<>();
    setupPayHistoryTable(payTable);

    VBox tableCard = new VBox(payTable);
    tableCard.getStyleClass().add("card");
    tableCard.setPadding(new Insets(20, 35, 35, 35));
    VBox.setVgrow(payTable, Priority.ALWAYS);

    runBtn.setOnAction(e -> {
      int month = monthBox.getSelectionModel().getSelectedIndex() + 1;
      int year = yearBox.getValue();
      try {
        List<PayHistoryEntry> data = repository.getPayHistory(month, year);
        payTable.setItems(FXCollections.observableArrayList(data));
        updateStatus(data.isEmpty() ? "No payroll records found for this period." : data.size() + " records found.");
      } catch (SQLException ex) {
        showAlert("Report Error", ex.getMessage());
      }
    });

    VBox content = new VBox(selectors, tableCard);
    content.getStyleClass().add("content-area");
    VBox.setVgrow(tableCard, Priority.ALWAYS);

    runBtn.fire();

    VBox view = new VBox(header, content);
    VBox.setVgrow(content, Priority.ALWAYS);
    return view;
  }

  private void setupPayHistoryTable(TableView<PayHistoryEntry> t) {
    TableColumn<PayHistoryEntry, String> nameCol = new TableColumn<>("Name");
    nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));

    TableColumn<PayHistoryEntry, String> ssnCol = new TableColumn<>("SSN");
    ssnCol.setCellValueFactory(new PropertyValueFactory<>("ssn"));

    TableColumn<PayHistoryEntry, String> jtCol = new TableColumn<>("Job Title");
    jtCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));

    TableColumn<PayHistoryEntry, String> divCol = new TableColumn<>("Division");
    divCol.setCellValueFactory(new PropertyValueFactory<>("division"));

    TableColumn<PayHistoryEntry, String> dateCol = new TableColumn<>("Pay Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));

    TableColumn<PayHistoryEntry, Double> earningsCol = new TableColumn<>("Gross Pay");
    earningsCol.setCellValueFactory(new PropertyValueFactory<>("earnings"));
    earningsCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> fedTaxCol = new TableColumn<>("Fed Tax");
    fedTaxCol.setCellValueFactory(new PropertyValueFactory<>("fedTax"));
    fedTaxCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> fedMedCol = new TableColumn<>("Fed Med");
    fedMedCol.setCellValueFactory(new PropertyValueFactory<>("fedMed"));
    fedMedCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> fedSsCol = new TableColumn<>("Fed SS");
    fedSsCol.setCellValueFactory(new PropertyValueFactory<>("fedSs"));
    fedSsCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> stateTaxCol = new TableColumn<>("State Tax");
    stateTaxCol.setCellValueFactory(new PropertyValueFactory<>("stateTax"));
    stateTaxCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> retireCol = new TableColumn<>("401k");
    retireCol.setCellValueFactory(new PropertyValueFactory<>("retire401k"));
    retireCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> healthCol = new TableColumn<>("Health Care");
    healthCol.setCellValueFactory(new PropertyValueFactory<>("healthCare"));
    healthCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    TableColumn<PayHistoryEntry, Double> netCol = new TableColumn<>("Net Pay");
    netCol.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getNetPay()).asObject());
    netCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    nameCol.setPrefWidth(140);
    ssnCol.setPrefWidth(110);
    jtCol.setPrefWidth(130);
    divCol.setPrefWidth(130);
    dateCol.setPrefWidth(100);
    earningsCol.setPrefWidth(110);
    fedTaxCol.setPrefWidth(90);
    fedMedCol.setPrefWidth(90);
    fedSsCol.setPrefWidth(90);
    stateTaxCol.setPrefWidth(90);
    retireCol.setPrefWidth(80);
    healthCol.setPrefWidth(100);
    netCol.setPrefWidth(110);

    t.getColumns().addAll(nameCol, ssnCol, jtCol, divCol, dateCol, earningsCol,
        fedTaxCol, fedMedCol, fedSsCol, stateTaxCol, retireCol, healthCol, netCol);
  }

  private VBox buildCombinedReportView() {
    HBox header = new HBox();
    header.getStyleClass().add("header-area");
    header.setAlignment(Pos.CENTER_LEFT);
    Label title = new Label("Pay Analytics");
    title.getStyleClass().add("header-title");
    header.getChildren().add(title);

    List<String> monthNames = List.of("January", "February", "March", "April", "May", "June",
        "July", "August", "September", "October", "November", "December");
    ComboBox<String> monthBox = new ComboBox<>(FXCollections.observableArrayList(monthNames));
    monthBox.setValue("January");
    ComboBox<Integer> yearBox = new ComboBox<>(FXCollections.observableArrayList(2023, 2024, 2025, 2026));
    yearBox.setValue(2025);
    Button runBtn = new Button("Generate Report");
    runBtn.getStyleClass().add("button-primary");

    HBox selectors = new HBox(15, new Label("Period:"), monthBox, yearBox, runBtn);
    selectors.setAlignment(Pos.CENTER_LEFT);

    TableView<ReportEntry> jobTable = new TableView<>();
    setupGroupReportTable(jobTable, "Job Title");
    Label jobTotal = new Label();
    jobTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0 0 0;");

    TableView<ReportEntry> divTable = new TableView<>();
    setupGroupReportTable(divTable, "Division");
    Label divTotal = new Label();
    divTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 0 0 0;");

    Label jobCardTitle = new Label("By Job Title");
    jobCardTitle.getStyleClass().add("section-header");
    VBox jobCard = new VBox(12, jobCardTitle, jobTable, jobTotal);
    jobCard.getStyleClass().add("card");
    jobCard.setPadding(new Insets(20));
    HBox.setHgrow(jobCard, Priority.ALWAYS);
    VBox.setVgrow(jobTable, Priority.ALWAYS);

    Label divCardTitle = new Label("By Division");
    divCardTitle.getStyleClass().add("section-header");
    VBox divCard = new VBox(12, divCardTitle, divTable, divTotal);
    divCard.getStyleClass().add("card");
    divCard.setPadding(new Insets(20));
    HBox.setHgrow(divCard, Priority.ALWAYS);
    VBox.setVgrow(divTable, Priority.ALWAYS);

    HBox tables = new HBox(25, jobCard, divCard);
    VBox.setVgrow(tables, Priority.ALWAYS);

    runBtn.setOnAction(e -> {
      int month = monthBox.getSelectionModel().getSelectedIndex() + 1;
      int year = yearBox.getValue();
      try {
        List<ReportEntry> jobData = repository.getTotalPayByJobTitle(month, year);
        jobTable.setItems(FXCollections.observableArrayList(jobData));
        double jobGrand = jobData.stream().mapToDouble(ReportEntry::getTotalAmount).sum();
        jobTotal.setText(jobData.isEmpty() ? "" : String.format("Grand Total:  $%,.2f", jobGrand));

        List<ReportEntry> divData = repository.getTotalPayByDivision(month, year);
        divTable.setItems(FXCollections.observableArrayList(divData));
        double divGrand = divData.stream().mapToDouble(ReportEntry::getTotalAmount).sum();
        divTotal.setText(divData.isEmpty() ? "" : String.format("Grand Total:  $%,.2f", divGrand));

        updateStatus((jobData.size() + divData.size()) == 0 ? "No records found for this period." : "Report generated.");
      } catch (SQLException ex) {
        showAlert("Report Error", ex.getMessage());
      }
    });

    VBox content = new VBox(20, selectors, tables);
    content.getStyleClass().add("content-area");
    content.setPadding(new Insets(30));
    VBox.setVgrow(tables, Priority.ALWAYS);
    VBox.setVgrow(content, Priority.ALWAYS);

    runBtn.fire();

    VBox view = new VBox(header, content);
    VBox.setVgrow(content, Priority.ALWAYS);
    return view;
  }

  private void setupGroupReportTable(TableView<ReportEntry> t, String categoryLabel) {
    TableColumn<ReportEntry, String> catCol = new TableColumn<>(categoryLabel);
    catCol.setCellValueFactory(new PropertyValueFactory<>("category"));

    TableColumn<ReportEntry, Double> totalCol = new TableColumn<>("Total Earnings");
    totalCol.setCellValueFactory(new PropertyValueFactory<>("totalAmount"));
    totalCol.setCellFactory(tc -> new TableCell<>() {
      @Override protected void updateItem(Double v, boolean e) {
        super.updateItem(v, e); setText(e || v == null ? null : String.format("$%,.2f", v));
      }
    });

    t.getColumns().addAll(catCol, totalCol);
    t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
  }

  private void loadComboBoxes() {
    try {
      jobTitleCombo.setItems(FXCollections.observableArrayList(repository.getAllJobTitles()));
      divisionCombo.setItems(FXCollections.observableArrayList(repository.getAllDivisions()));
    } catch (SQLException e) {
      showAlert("Database Error", "Failed to load Job Titles or Divisions: " + e.getMessage());
    }
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
            if (empty || val == null)
              setText(null);
            else
              setText(String.format("$%,.2f", val));
          }
        });

    TableColumn<Employee, JobTitle> jobTitleCol = new TableColumn<>("Job Title");
    jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));

    TableColumn<Employee, Division> divisionCol = new TableColumn<>("Division");
    divisionCol.setCellValueFactory(new PropertyValueFactory<>("division"));

    table.getColumns().addAll(idCol, nameCol, ssnCol, salaryCol, jobTitleCol, divisionCol);

    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null) {
                selectedEmployee = newVal;
                nameField.setText(newVal.getName());
                emailField.setText(newVal.getEmail());
                hireDatePicker.setValue(newVal.getHireDate());
                ssnField.setText(newVal.getSsn());
                salaryField.setText(String.valueOf(newVal.getSalary()));
                
                selectJobTitleInCombo(newVal.getJobTitle());
                selectDivisionInCombo(newVal.getDivision());
                
                updateStatus("Selected: " + newVal.getName());
              }
            });

    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
  }

  private void selectJobTitleInCombo(JobTitle target) {
    if (target == null) {
      jobTitleCombo.getSelectionModel().clearSelection();
      return;
    }
    for (JobTitle item : jobTitleCombo.getItems()) {
      if (item.getId() == target.getId()) {
        jobTitleCombo.getSelectionModel().select(item);
        break;
      }
    }
  }

  private void selectDivisionInCombo(Division target) {
    if (target == null) {
      divisionCombo.getSelectionModel().clearSelection();
      return;
    }
    for (Division item : divisionCombo.getItems()) {
      if (item.getId() == target.getId()) {
        divisionCombo.getSelectionModel().select(item);
        break;
      }
    }
  }

  private VBox createDetailPane() {
    VBox container = new VBox(25);
    container.setPadding(new Insets(35));
    container.setPrefWidth(420);

    Label sectionTitle = new Label("Manage Employee");
    sectionTitle.getStyleClass().add("section-header");

    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(18);

    addFormField(grid, "Name", nameField, 0);
    addFormField(grid, "Email", emailField, 1);
    
    hireDatePicker.setMaxWidth(Double.MAX_VALUE);
    addFormField(grid, "Hire Date", hireDatePicker, 2);
    
    addFormField(grid, "SSN", ssnField, 3);
    addFormField(grid, "Salary", salaryField, 4);
    
    Label jtLabel = new Label("Job Title");
    jtLabel.getStyleClass().add("form-label");
    grid.add(jtLabel, 0, 5);
    jobTitleCombo.setMaxWidth(Double.MAX_VALUE);
    jobTitleCombo.setPromptText("Select Position");
    HBox jtBox = new HBox(5, jobTitleCombo, createSmallButton("+", e -> handleNewJobTitle()), createSmallButton("-", e -> handleDeleteJobTitle()));
    HBox.setHgrow(jobTitleCombo, Priority.ALWAYS);
    grid.add(jtBox, 1, 5);

    Label divLabel = new Label("Division");
    divLabel.getStyleClass().add("form-label");
    grid.add(divLabel, 0, 6);
    divisionCombo.setMaxWidth(Double.MAX_VALUE);
    divisionCombo.setPromptText("Select Department");
    HBox divBox = new HBox(5, divisionCombo, createSmallButton("+", e -> handleNewDivision()), createSmallButton("-", e -> handleDeleteDivision()));
    HBox.setHgrow(divisionCombo, Priority.ALWAYS);
    grid.add(divBox, 1, 6);

    Button saveBtn = new Button("Update Selected");
    saveBtn.getStyleClass().add("button-primary");
    saveBtn.setMaxWidth(Double.MAX_VALUE);
    saveBtn.setOnAction(e -> handleUpdate());

    Button addBtn = new Button("Add as New Employee");
    addBtn.getStyleClass().add("button-success");
    addBtn.setMaxWidth(Double.MAX_VALUE);
    addBtn.setOnAction(e -> handleAdd());

    Button deleteBtn = new Button("Delete Selected");
    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white;");
    deleteBtn.setMaxWidth(Double.MAX_VALUE);
    deleteBtn.setOnAction(e -> handleDelete());

    Button payrollBtn = new Button("View Full Pay Statement");
    payrollBtn.getStyleClass().add("button-outline");
    payrollBtn.setMaxWidth(Double.MAX_VALUE);
    payrollBtn.setOnAction(e -> handleViewPayroll());

    Separator sep = new Separator();
    Label batchTitle = new Label("Batch Salary Increase");
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

    container
        .getChildren()
        .addAll(sectionTitle, grid, saveBtn, addBtn, deleteBtn, payrollBtn, sep, batchTitle, batchInput, batchBtn);
    return container;
  }

  private Button createSmallButton(String text, EventHandler<ActionEvent> handler) {
    Button b = new Button(text);
    b.getStyleClass().add("button-small");
    b.setOnAction(handler);
    return b;
  }

  private void addFormField(GridPane grid, String labelText, Control field, int row) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    grid.add(label, 0, row);
    grid.add(field, 1, row);
    GridPane.setHgrow(field, Priority.ALWAYS);
    if (field instanceof Region) {
        ((Region) field).setMaxWidth(Double.MAX_VALUE);
    }
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
    if (validateInput()) {
        try {
          fillEmployeeData(selectedEmployee);
          repository.updateEmployee(selectedEmployee);
          handleSearch();
          updateStatus("Employee updated successfully.");
        } catch (Exception e) {
          showAlert("Error", "Update failed: " + e.getMessage());
        }
    }
  }

  private void handleAdd() {
    if (validateInput()) {
        try {
            Employee newEmp = new FullTimeEmployee();
            fillEmployeeData(newEmp);
            repository.addEmployee(newEmp);
            handleSearch();
            updateStatus("Employee added successfully.");
        } catch (Exception e) {
            showAlert("Error", "Add failed: " + e.getMessage());
        }
    }
  }

  private void handleDelete() {
    if (selectedEmployee == null) {
        showAlert("No Selection", "Please select an employee to delete.");
        return;
    }
    
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selectedEmployee.getName() + "?", ButtonType.YES, ButtonType.NO);
    confirm.showAndWait().ifPresent(response -> {
        if (response == ButtonType.YES) {
            try {
                repository.deleteEmployee(selectedEmployee.getEmpid());
                handleSearch();
                selectedEmployee = null;
                clearForm();
                updateStatus("Employee deleted.");
            } catch (SQLException e) {
                showAlert("Error", "Delete failed: " + e.getMessage());
            }
        }
    });
  }

  private void handleViewPayroll() {
    if (selectedEmployee == null) {
        showAlert("No Selection", "Please select an employee first.");
        return;
    }

    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setTitle("Employee Financial Overview");

    VBox layout = new VBox(25);
    layout.getStyleClass().add("card");
    layout.setStyle("-fx-background-color: white; -fx-padding: 40;");
    
    Label header = new Label("Employee Pay Statement History");
    header.getStyleClass().add("section-header");
    
    GridPane info = new GridPane();
    info.setHgap(40);
    info.setVgap(10);
    info.add(new Label("Name:"), 0, 0); info.add(new Label(selectedEmployee.getName()), 1, 0);
    info.add(new Label("Email:"), 0, 1); info.add(new Label(selectedEmployee.getEmail()), 1, 1);
    info.add(new Label("Hire Date:"), 0, 2); info.add(new Label(selectedEmployee.getHireDate().toString()), 1, 2);
    info.add(new Label("Current Salary:"), 2, 0); info.add(new Label(String.format("$%,.2f", selectedEmployee.getSalary())), 3, 0);
    info.add(new Label("Job Title:"), 2, 1); info.add(new Label(selectedEmployee.getJobTitle().toString()), 3, 1);
    info.add(new Label("Division:"), 2, 2); info.add(new Label(selectedEmployee.getDivision().toString()), 3, 2);

    TableView<Payroll> payrollTable = new TableView<>();
    TableColumn<Payroll, LocalDate> dateCol = new TableColumn<>("Pay Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
    
    TableColumn<Payroll, Double> earningsCol = new TableColumn<>("Gross Earnings");
    earningsCol.setCellValueFactory(new PropertyValueFactory<>("earnings"));
    earningsCol.setCellFactory(tc -> new TableCell<>() {
        @Override protected void updateItem(Double v, boolean e) {
            super.updateItem(v, e);
            if (e || v == null) setText(null);
            else setText(String.format("$%,.2f", v));
        }
    });

    payrollTable.getColumns().addAll(dateCol, earningsCol);
    payrollTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    try {
        payrollTable.setItems(FXCollections.observableArrayList(repository.getPayrollForEmployee(selectedEmployee.getEmpid())));
    } catch (SQLException e) {
        showAlert("Error", "Failed to load payroll.");
    }

    layout.getChildren().addAll(header, info, new Separator(), new Label("Payment History"), payrollTable);
    
    Scene scene = new Scene(layout, 800, 700);
    try {
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    } catch (Exception e) {}
    
    dialog.setScene(scene);
    dialog.show();
  }

  private void handleNewDivision() {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setResizable(false);
    dialog.setTitle("New Division");

    GridPane grid = new GridPane();
    grid.setPadding(new Insets(15));
    grid.setHgap(10);
    grid.setVgap(10);

    TextField name = new TextField();
    TextField city = new TextField();
    TextField addr1 = new TextField();
    TextField addr2 = new TextField();
    TextField state = new TextField();
    TextField country = new TextField();
    TextField zip = new TextField();

    grid.add(new Label("Name:"), 0, 0); grid.add(name, 1, 0);
    grid.add(new Label("City:"), 0, 1); grid.add(city, 1, 1);
    grid.add(new Label("Address 1:"), 0, 2); grid.add(addr1, 1, 2);
    grid.add(new Label("Address 2:"), 0, 3); grid.add(addr2, 1, 3);
    grid.add(new Label("State:"), 0, 4); grid.add(state, 1, 4);
    grid.add(new Label("Country:"), 0, 5); grid.add(country, 1, 5);
    grid.add(new Label("Zip:"), 0, 6); grid.add(zip, 1, 6);

    Button save = new Button("Save Division");
    save.setMaxWidth(Double.MAX_VALUE);
    save.setOnAction(e -> {
        try {
            Division d = new Division(0, name.getText(), city.getText(), addr1.getText(), addr2.getText(), state.getText(), country.getText(), zip.getText());
            repository.addDivision(d);
            loadComboBoxes();
            dialog.close();
        } catch (SQLException ex) {
            showAlert("Error", "Failed to save.");
        }
    });

    VBox layout = new VBox(15, grid, save);
    layout.setPadding(new Insets(10));
    layout.setAlignment(Pos.CENTER);

    Scene scene = new Scene(layout);
    try {
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    } catch (Exception e) {}
    
    dialog.setScene(scene);
    dialog.sizeToScene();
    dialog.show();
  }

  private void handleNewJobTitle() {
    Stage dialog = new Stage();
    dialog.initModality(Modality.APPLICATION_MODAL);
    dialog.setResizable(false);
    dialog.setTitle("New Job Title");

    TextField titleField = new TextField();
    titleField.setPromptText("Enter title...");
    
    Button save = new Button("Save Title");
    save.setMaxWidth(Double.MAX_VALUE);
    save.setOnAction(e -> {
        try {
            JobTitle jt = new JobTitle(0, titleField.getText());
            repository.addJobTitle(jt);
            loadComboBoxes();
            dialog.close();
        } catch (SQLException ex) {
            showAlert("Error", "Failed to save.");
        }
    });

    VBox layout = new VBox(15, new Label("Job Title:"), titleField, save);
    layout.setPadding(new Insets(20));
    layout.setAlignment(Pos.CENTER);

    Scene scene = new Scene(layout);
    try {
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
    } catch (Exception e) {}

    dialog.setScene(scene);
    dialog.sizeToScene();
    dialog.show();
  }

  private void handleDeleteJobTitle() {
    JobTitle selected = jobTitleCombo.getValue();
    if (selected == null) {
        showAlert("No Selection", "Select a job title from the list to delete.");
        return;
    }
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete Job Title: " + selected.getTitle() + "? This will also remove it from any linked employees.", ButtonType.YES, ButtonType.NO);
    confirm.showAndWait().ifPresent(r -> {
        if (r == ButtonType.YES) {
            try {
                repository.deleteJobTitle(selected.getId());
                loadComboBoxes();
                updateStatus("Job Title deleted.");
            } catch (SQLException e) {
                showAlert("Error", "Could not delete: " + e.getMessage());
            }
        }
    });
  }

  private void handleDeleteDivision() {
    Division selected = divisionCombo.getValue();
    if (selected == null) {
        showAlert("No Selection", "Select a division from the list to delete.");
        return;
    }
    Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete Division: " + selected.getName() + "? This will also remove it from any linked employees.", ButtonType.YES, ButtonType.NO);
    confirm.showAndWait().ifPresent(r -> {
        if (r == ButtonType.YES) {
            try {
                repository.deleteDivision(selected.getId());
                loadComboBoxes();
                updateStatus("Division deleted.");
            } catch (SQLException e) {
                showAlert("Error", "Could not delete: " + e.getMessage());
            }
        }
    });
  }

  private boolean validateInput() {
    try {
        Double.parseDouble(salaryField.getText().trim());
        if (nameField.getText().trim().isEmpty()) throw new Exception("Name required");
        return true;
    } catch (Exception e) {
        showAlert("Validation Error", "Invalid input: " + e.getMessage());
        return false;
    }
  }

  private void fillEmployeeData(Employee emp) {
    emp.setName(nameField.getText().trim());
    emp.setEmail(emailField.getText().trim());
    emp.setHireDate(hireDatePicker.getValue());
    emp.setSsn(ssnField.getText().trim());
    emp.setSalary(Double.parseDouble(salaryField.getText().trim()));
    emp.setJobTitle(jobTitleCombo.getValue());
    emp.setDivision(divisionCombo.getValue());
  }

  private void clearForm() {
    nameField.clear();
    emailField.clear();
    hireDatePicker.setValue(null);
    ssnField.clear();
    salaryField.clear();
    jobTitleCombo.getSelectionModel().clearSelection();
    divisionCombo.getSelectionModel().clearSelection();
  }

  private void handleSalaryIncrease(TextField p, TextField min, TextField max) {
    String pctInput = p.getText().trim();
    String minInput = min.getText().trim();
    String maxInput = max.getText().trim();

    if (pctInput.isEmpty() || minInput.isEmpty() || maxInput.isEmpty()) {
      showAlert("Missing Input", "Enter all three values.");
      return;
    }

    try {
      double pct = Double.parseDouble(pctInput.replace("%", ""));
      double minSal = Double.parseDouble(minInput.replace("$", "").replace(",", ""));
      double maxSal = Double.parseDouble(maxInput.replace("$", "").replace(",", ""));

      repository.updateSalariesInRange(pct, minSal, maxSal);
      handleSearch();
      showAlert("Success", "Salaries updated.");
    } catch (Exception e) {
      showAlert("Invalid Input", "Please enter valid numbers.");
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
