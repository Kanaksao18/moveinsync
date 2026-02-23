# Backup and Recovery Strategy

## Data Stores
- Primary: PostgreSQL (`mdmdb`)
- Cache: Redis (non-source-of-truth)

## Backup Plan
- Run periodic PostgreSQL logical backups (`pg_dump`) for schema + data.
- Script provided: `mdm/scripts/backup_db.ps1`
- Keep at least:
  - daily snapshots for 14 days
  - weekly snapshots for 8 weeks
  - monthly snapshots for 6 months

## Recovery Plan
- Restore latest verified backup into standby instance.
- Script provided: `mdm/scripts/restore_db.ps1`
- Validate key tables:
  - `devices`
  - `app_versions`
  - `update_schedules`
  - `device_updates`
  - `audit_logs`
- Repoint application to restored database.

## Integrity Controls
- Audit logs are immutable at entity level (`@PreUpdate` guard).
- Rollout and device-update states are persisted to allow restart-safe resumption.
- Redis failures degrade gracefully to DB source reads.
