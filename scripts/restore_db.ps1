param(
  [Parameter(Mandatory=$true)]
  [string]$BackupFile,
  [string]$DbName = "mdmdb",
  [string]$DbUser = "postgres",
  [string]$DbHost = "localhost"
)

if (!(Test-Path $BackupFile)) {
  Write-Error "Backup file not found: $BackupFile"
  exit 1
}

Write-Host "Restoring backup: $BackupFile"
psql -h $DbHost -U $DbUser -d $DbName -f $BackupFile

if ($LASTEXITCODE -ne 0) {
  Write-Error "Restore failed"
  exit 1
}

Write-Host "Restore completed"
