package dev.project.employeemanagement;

import dev.project.employeemanagement.model.Division;
import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.model.FullTimeEmployee;
import dev.project.employeemanagement.model.JobTitle;
import dev.project.employeemanagement.model.Payroll;
import dev.project.employeemanagement.repository.EmployeeRepository;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import javafx.application.Application;
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

    Label brand = new Label("COMPANY Z");
    brand.getStyleClass().add("brand-title");

    Button employeesBtn = new Button("Employee Database");
    employeesBtn.getStyleClass().addAll("nav-button", "nav-button-active");
    employeesBtn.setMaxWidth(Double.MAX_VALUE);

    navRail.getChildren().addAll(brand, employeesBtn);

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
    sidebarDetail.getStyleClass().add("card");

    HBox workspace = new HBox(30, tableCard, sidebarDetail);
    workspace.setPadding(new Insets(35));
    HBox.setHgrow(tableCard, Priority.ALWAYS);

    VBox contentArea = new VBox(header, workspace);
    contentArea.getStyleClass().add("content-area");
    VBox.setVgrow(workspace, Priority.ALWAYS);

    HBox statusBar = new HBox(statusLabel);
    statusBar.getStyleClass().add("status-bar");

    BorderPane root = new BorderPane();
    root.setLeft(navRail);
    root.setCenter(contentArea);
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

    TableColumn<Employee, String> emailCol = new TableColumn<>("Email");
    emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));

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

    table.getColumns().addAll(idCol, nameCol, emailCol, salaryCol, jobTitleCol);

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

    Label sectionTitle = new Label("MANAGE EMPLOYEE");
    sectionTitle.getStyleClass().add("section-header");

    GridPane grid = new GridPane();
    grid.setHgap(20);
    grid.setVgap(18);

    addFormField(grid, "Name", nameField, 0);
    addFormField(grid, "Email", emailField, 1);
    addFormField(grid, "Hire Date", hireDatePicker, 2);
    addFormField(grid, "SSN", ssnField, 3);
    addFormField(grid, "Salary", salaryField, 4);
    
    Label jtLabel = new Label("Job Title");
    jtLabel.getStyleClass().add("form-label");
    grid.add(jtLabel, 0, 5);
    HBox jtBox = new HBox(5, jobTitleCombo, createSmallButton("+", e -> handleNewJobTitle()), createSmallButton("-", e -> handleDeleteJobTitle()));
    jobTitleCombo.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(jobTitleCombo, Priority.ALWAYS);
    grid.add(jtBox, 1, 5);

    Label divLabel = new Label("Division");
    divLabel.getStyleClass().add("form-label");
    grid.add(divLabel, 0, 6);
    HBox divBox = new HBox(5, divisionCombo, createSmallButton("+", e -> handleNewDivision()), createSmallButton("-", e -> handleDeleteDivision()));
    divisionCombo.setMaxWidth(Double.MAX_VALUE);
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

    Button payrollBtn = new Button("View Payroll History");
    payrollBtn.setMaxWidth(Double.MAX_VALUE);
    payrollBtn.setOnAction(e -> handleViewPayroll());

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

    container
        .getChildren()
        .addAll(sectionTitle, grid, saveBtn, addBtn, deleteBtn, payrollBtn, sep, batchTitle, batchInput, batchBtn);
    return container;
  }

  private Button createSmallButton(String text, EventHandler<ActionEvent> handler) {
    Button b = new Button(text);
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
    dialog.setTitle("Payroll History - " + selectedEmployee.getName());

    TableView<Payroll> payrollTable = new TableView<>();
    TableColumn<Payroll, LocalDate> dateCol = new TableColumn<>("Date");
    dateCol.setCellValueFactory(new PropertyValueFactory<>("payDate"));
    
    TableColumn<Payroll, Double> earningsCol = new TableColumn<>("Earnings");
    earningsCol.setCellValueFactory(new PropertyValueFactory<>("earnings"));

    payrollTable.getColumns().addAll(dateCol, earningsCol);

    try {
        payrollTable.setItems(FXCollections.observableArrayList(repository.getPayrollForEmployee(selectedEmployee.getEmpid())));
    } catch (SQLException e) {
        showAlert("Error", "Failed to load payroll.");
    }

    VBox layout = new VBox(10, new Label("Payroll records for ID: " + selectedEmployee.getEmpid()), payrollTable);
    layout.setPadding(new Insets(20));
    dialog.setScene(new Scene(layout, 400, 400));
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
