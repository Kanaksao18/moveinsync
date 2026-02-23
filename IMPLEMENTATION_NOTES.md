# MDM Implementation Notes

## Authentication and Role Governance
- JWT includes role claims (`ADMIN`, `VIEWER`, `PRODUCT_HEAD`).
- Role-based endpoint authorization enforces:
  - `ADMIN`: schedule/version/compatibility writes.
  - `PRODUCT_HEAD` or `ADMIN`: approval/rejection of pending mandatory schedules.
  - `VIEWER`: read-only dashboards, audit, monitoring.

## Time and Space Cost Considerations
- Rollout selection currently scans repository results and applies filters in memory.
- Complexity for rollout selection is `O(N)` over devices, with `N` = device count.
- Space complexity for selection is `O(K)` where `K` is matched device count.
- Dashboard aggregations are computed via stream grouping and can be moved to SQL aggregation for large-scale optimization.

## Failure Handling and Recovery
- Global exception handling returns structured API errors.
- Cache failures are isolated using custom `CacheErrorHandler`.
- Device update failures are persisted with stage/reason and auto-retry metadata (`nextRetryAt`, `retryCount`).
- Scheduled retry worker runs periodically and retries failed updates based on configurable policy.

## Trade-offs
- Current filtering uses simple in-memory evaluation for faster development and simpler readability.
- This improves maintainability but should be migrated to query-level filters for million-device scale.
- Approval workflow is enforced with status transitions (`PENDING_APPROVAL` -> `SCHEDULED/ACTIVE`).

## Monitoring
- Actuator metrics are exposed and consumed by frontend.
- Dashboard summary API provides:
  - total/active/inactive counts
  - region/version distributions
  - rollout progress and rate metrics

## Caching
- Redis-backed cache for high-read endpoints (latest version).
- TTL-based eviction with explicit cache eviction on mutating flows.

## Exception Strategy
- Domain exceptions (`BadRequest`, `ResourceNotFound`, `Unauthorized`) are mapped to clear responses.
- Unknown exceptions return controlled `500` payload.
