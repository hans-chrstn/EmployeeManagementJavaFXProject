-- EmployeeData3 full schema
-- Run this in MySQL Workbench / Beaver to create all tables.

CREATE DATABASE IF NOT EXISTS EmployeeData3;
USE EmployeeData3;

CREATE TABLE IF NOT EXISTS employees (
  empid   INT           AUTO_INCREMENT PRIMARY KEY,
  Fname   VARCHAR(50)   NOT NULL,
  Lname   VARCHAR(50)   NOT NULL,
  SSN     VARCHAR(9)    UNIQUE,
  Salary  DECIMAL(12,2) NOT NULL,
  email   VARCHAR(100),
  HireDate DATE
);

CREATE TABLE IF NOT EXISTS job_titles (
  job_title_id INT         AUTO_INCREMENT PRIMARY KEY,
  job_title    VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS employee_job_titles (
  empid        INT NOT NULL,
  job_title_id INT NOT NULL,
  PRIMARY KEY (empid, job_title_id),
  FOREIGN KEY (empid)        REFERENCES employees(empid)   ON DELETE CASCADE,
  FOREIGN KEY (job_title_id) REFERENCES job_titles(job_title_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS division (
  ID           INT          AUTO_INCREMENT PRIMARY KEY,
  Name         VARCHAR(100) NOT NULL,
  city         VARCHAR(50),
  addressLine1 VARCHAR(100),
  addressLine2 VARCHAR(100),
  state        VARCHAR(50),
  country      VARCHAR(50),
  postalCode   VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS employee_division (
  empid  INT NOT NULL,
  div_ID INT NOT NULL,
  PRIMARY KEY (empid, div_ID),
  FOREIGN KEY (empid)  REFERENCES employees(empid) ON DELETE CASCADE,
  FOREIGN KEY (div_ID) REFERENCES division(ID)     ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS payroll (
  payID       INT           AUTO_INCREMENT PRIMARY KEY,
  pay_date    DATE          NOT NULL,
  earnings    DECIMAL(12,2) NOT NULL,
  fed_tax     DECIMAL(12,2) NOT NULL,
  fed_med     DECIMAL(12,2) NOT NULL,
  fed_ss      DECIMAL(12,2) NOT NULL,
  state_tax   DECIMAL(12,2) NOT NULL,
  retire_401k DECIMAL(12,2) NOT NULL,
  health_care DECIMAL(12,2) NOT NULL,
  empid       INT,
  FOREIGN KEY (empid) REFERENCES employees(empid) ON DELETE CASCADE
);
