package dev.project.employeemanagement;

import dev.project.employeemanagement.model.Employee;
import dev.project.employeemanagement.repository.EmployeeRepository;
import java.sql.SQLException;
import java.util.List;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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
  private final TableView<Employee> table = new TableView<>();
  private final TextField searchField = new TextField();
  private final Label statusLabel = new Label("System Ready");

  private final TextField nameField = new TextField();
  private final TextField ssnField = new TextField();
  private final TextField salaryField = new TextField();
  private final TextField jobTitleField = new TextField();
  private final TextField divisionField = new TextField();
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
      System.out.println("CSS could not be loaded.");
    }

    primaryStage.setScene(scene);
    primaryStage.show();

    jobTitleField.setEditable(false);
    jobTitleField.setPromptText("View only");
    handleSearch();
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

    TableColumn<Employee, String> jobTitleCol = new TableColumn<>("Job Title");
    jobTitleCol.setCellValueFactory(new PropertyValueFactory<>("jobTitle"));

    table.getColumns().addAll(idCol, nameCol, ssnCol, salaryCol, jobTitleCol);

    table
        .getSelectionModel()
        .selectedItemProperty()
        .addListener(
            (obs, oldVal, newVal) -> {
              if (newVal != null) {
                selectedEmployee = newVal;
                nameField.setText(newVal.getName());
                ssnField.setText(newVal.getSsn());
                salaryField.setText(String.valueOf(newVal.getSalary()));
                jobTitleField.setText(newVal.getJobTitle());
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
    addFormField(grid, "Job Title", jobTitleField, 3);

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

    container
        .getChildren()
        .addAll(sectionTitle, grid, saveBtn, sep, batchTitle, batchInput, batchBtn);
    return container;
  }

  private void addFormField(GridPane grid, String labelText, TextField field, int row) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    grid.add(label, 0, row);
    grid.add(field, 1, row);
    GridPane.setHgrow(field, Priority.ALWAYS);
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
    try {
      double pct = Double.parseDouble(p.getText());
      double minSal = Double.parseDouble(min.getText());
      double maxSal = Double.parseDouble(max.getText());

      repository.updateSalariesInRange(pct, minSal, maxSal);
      handleSearch();
      updateStatus("Salaries updated successfully.");
      showAlert("Success", "Salaries updated successfully.");
    } catch (Exception e) {
      showAlert("Input Error", "Please enter valid numeric values for the salary range.");
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
