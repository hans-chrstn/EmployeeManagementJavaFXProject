# Group project for Software Development 2026

## Prerequisites

- **Java 21** or higher.
- **MySQL Server** running locally.
- **Environment Variables:** Set `DB_USER` and `DB_PASS` in your environment

## How to Build and Run

### Database Setup

```bash
# Unix
sh ./setup_db.sh

# Windows
powershell.exe -ExecutionPolicy Bypass -File .\setup_db.ps1
```

> **Note**
> If you are manually creating the database, make sure
> the name of the database is EmployeeData3

### Unix / Linux / macOS

1.  **Build the project**:
    ```bash
    ./gradlew build
    ```
2.  **Run the application**:
    ```bash
    ./gradlew run
    ```
3.  **Run tests**:
    ```bash
    ./gradlew test
    ```

### Windows

1.  **Build the project**:
    ```cmd
    gradlew.bat build
    ```
2.  **Run the application**:
    ```cmd
    gradlew.bat run
    ```
3.  **Run tests**:
    ```cmd
    gradlew.bat test
    ```

## Current App Features

- Centralized navigation interface via JavaFX
- Real-time filtering by name, SSN, and employee ID
- Direct record management and updates via synchronized detail forms
- Dynamic configuration of divisions and job titles
- Accurate hire date entry and tracking via integrated date picker
- Salary adjustments across specified percentage and pay ranges via batch processing
- Individual payment record tracking and retrieval
- Data consistency for relational table updates via transactional integrity
- Extensible employee classifications via object-oriented inheritance models
- Decoupled data persistence via repository pattern abstraction
- Secure test execution via isolated H2 database environment (So that test runs don't ruin pre-existing database which was a bug before)
