# Group project for Software Development 2026

## STRUCTURE

- DATABASE STRUCUTRE LOC: `app/src/main/resources/db/schema.sql`

  > this contains the database strucutre and salary ranges
  > (basically the src of truth of our db)## Prerequisites

- **Java 21** or higher.
- **MySQL Server** running locally.
- **Environment Variables:** Set `DB_USER` and `DB_PASS` in your environment

## How to Build and Run

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

## Database Setup

Execute the schema script:

```bash
# Unix
sudo mysql < app/src/main/resources/db/schema.sql

# Windows (MySQL Command Line Client)
source app/src/main/resources/db/schema.sql
```
