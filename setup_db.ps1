if (Test-Path .env) {
  Get-Content .env | ForEach-Object {
    $name, $value = $_.Split('=')
    Set-Item -Path "Env:$name" -Value $value
  }
} else {
  Write-Error "Missing .env file"
  exit
}

# create user and db
# grant and flush priviliges
Write-Host "Creating user and db..."
$sql = @"
DROP USER IF EXISTS '$env:DB_USER'@'localhost';
CREATE USER '$env:DB_USER'@'localhost' IDENTIFIED BY '$env:DB_PASS';
CREATE DATABASE IF NOT EXISTS employee_db;
GRANT ALL PRIVILEGES ON employee_db.* TO '$env:DB_USER'@'localhost';
FLUSH PRIVILEGES;
"@

$sql | mysql -u root -p
Write-Host "Granted privileges to '$env:DB_USER' on db: employee_db"

# apply schema
Write-Host "Appling schema..."
Get-Content app/src/main/resources/db/schema.sql | mysql -u root -p employee_db

Write-Host "done"
