CREATE DATABASE IF NOT EXISTS EmployeeData3;
USE EmployeeData3;

DROP TABLE IF EXISTS payroll;
DROP TABLE IF EXISTS employee_division;
DROP TABLE IF EXISTS division;
DROP TABLE IF EXISTS employee_job_titles;
DROP TABLE IF EXISTS job_titles;
DROP TABLE IF EXISTS employees;

CREATE TABLE employees (
  empid INT AUTO_INCREMENT PRIMARY KEY,
  Fname VARCHAR(50) NOT NULL,
  Lname VARCHAR(50) NOT NULL,
  email VARCHAR(100),
  HireDate DATE,
  Salary DECIMAL(12, 2) NOT NULL,
  SSN VARCHAR(9)
);

CREATE TABLE job_titles (
  job_title_id INT AUTO_INCREMENT PRIMARY KEY,
  job_title VARCHAR(100) NOT NULL
);

CREATE TABLE employee_job_titles (
  empid INT,
  job_title_id INT,
  PRIMARY KEY (empid, job_title_id),
  FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
  FOREIGN KEY (job_title_id) REFERENCES job_titles(job_title_id) ON DELETE CASCADE
);

CREATE TABLE division (
  ID INT AUTO_INCREMENT PRIMARY KEY,
  Name VARCHAR(100) NOT NULL,
  city VARCHAR(50),
  addressLine1 VARCHAR(100),
  addressLine2 VARCHAR(100),
  state VARCHAR(50),
  country VARCHAR(50),
  postalCode VARCHAR(20)
);

CREATE TABLE employee_division (
  empid INT,
  div_ID INT,
  PRIMARY KEY (empid, div_ID),
  FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE,
  FOREIGN KEY (div_ID) REFERENCES division(ID) ON DELETE CASCADE
);

CREATE TABLE payroll (
  payID INT AUTO_INCREMENT PRIMARY KEY,
  pay_date DATE NOT NULL,
  earnings DECIMAL(12, 2) NOT NULL,
  fed_tax DECIMAL(12, 2) NOT NULL,
  fed_med DECIMAL(12, 2) NOT NULL,
  fed_ss DECIMAL(12, 2) NOT NULL,
  state_tax DECIMAL(12, 2) NOT NULL,
  retire_401k DECIMAL(12, 2) NOT NULL,
  health_care DECIMAL(12, 2) NOT NULL,
  empid INT,
  FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE
);

INSERT INTO job_titles (job_title) VALUES ('Software Engineer'), ('Project Manager'), ('HR Specialist');

INSERT INTO division (Name, city, addressLine1, state, country, postalCode) 
VALUES ('Engineering', 'San Francisco', '123 Tech Way', 'CA', 'USA', '94105'),
       ('Human Resources', 'New York', '456 People St', 'NY', 'USA', '10001');

INSERT INTO employees (Fname, Lname, email, HireDate, Salary, SSN) 
VALUES ('John', 'Doe', 'john.doe@example.com', '2020-01-15', 85000.00, '123456789'),
       ('Jane', 'Smith', 'jane.smith@example.com', '2019-05-20', 95000.00, '987654321');

INSERT INTO employee_job_titles (empid, job_title_id) VALUES (1, 1), (2, 2);
INSERT INTO employee_division (empid, div_ID) VALUES (1, 1), (2, 2);

INSERT INTO payroll (pay_date, earnings, fed_tax, fed_med, fed_ss, state_tax, retire_401k, health_care, empid)
VALUES ('2023-10-01', 7083.33, 1000.00, 100.00, 400.00, 500.00, 300.00, 200.00, 1),
       ('2023-10-01', 7916.67, 1200.00, 110.00, 450.00, 600.00, 350.00, 200.00, 2);
