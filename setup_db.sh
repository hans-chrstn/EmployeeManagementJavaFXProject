#!/usr/bin/env bash


if [ -f .env ]; then
  export $(grep -v '^#' .env | xargs)
else
  echo "Missing .env file"
  exit 1
fi

# create user, db
# grant priviliges then flush
echo "Creating user and db..."
sudo mysql <<EOF
DROP USER IF EXISTS '$DB_USER'@'localhost';
CREATE USER '$DB_USER'@'localhost' IDENTIFIED BY '$DB_PASS';
CREATE DATABASE IF NOT EXISTS EmployeeData3;
GRANT ALL PRIVILEGES ON EmployeeData3.* TO '$DB_USER'@'localhost';
FLUSH PRIVILEGES;
EOF
echo "Granted privileges to '$DB_USER' on db: EmployeeData3"

# apply schema
echo "Appling schema..."
sudo mysql EmployeeData3 < app/src/main/resources/db/schema.sql

echo "done"
