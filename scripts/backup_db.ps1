param(
  [string]$DbName = "mdmdb",
  [string]$DbUser = "postgres",
  [string]$DbHost = "localhost",
  [string]$OutputDir = ".\\backups"
)

if (!(Test-Path $OutputDir)) {
  New-Item -ItemType Directory -Path $OutputDir | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$file = Join-Path $OutputDir "mdm_backup_$timestamp.sql"

Write-Host "Creating backup: $file"
pg_dump -h $DbHost -U $DbUser -d $DbName -F p -f $file

if ($LASTEXITCODE -ne 0) {
  Write-Error "Backup failed"
  exit 1
}

Write-Host "Backup completed"
