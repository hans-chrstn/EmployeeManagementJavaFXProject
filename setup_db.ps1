$securePass = Read-Host "Enter MySQL root password" -AsSecureString
$rootPass = [System.Net.NetworkCredential]::new("", $securePass).Password

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
CREATE DATABASE IF NOT EXISTS EmployeeData3;
GRANT ALL PRIVILEGES ON EmployeeData3.* TO '$env:DB_USER'@'localhost';
FLUSH PRIVILEGES;
"@

$sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u root -p"$rootPass"
Write-Host "Granted privileges to '$env:DB_USER' on db: EmployeeData3"

# apply schema
Write-Host "Applying schema..."
Get-Content app/src/main/resources/db/schema.sql | & "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe" -u "$env:DB_USER" -p"$env:DB_PASS" EmployeeData3

Write-Host "done"
