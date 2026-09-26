# ADR 0005: Least-privilege database roles per service

- **Status:** Accepted
- **Date:** 2026-09-26

## Context
All services share one PostgreSQL database (Neon's free plan gives one project with 0.5 GB). Sharing a database must not mean sharing access. Later phases also need database-level guarantees: for example, the audit table must be impossible to `UPDATE` or `DELETE` for the running service (Phase 4). That can't be done if the service connects as the schema owner.

## Decision
Each service gets its own schema and **two roles**:

| Role | Used by | Privileges |
|---|---|---|
| `<service>_owner` (e.g. `gateway_owner`) | Flyway migrations at startup | Owns the schema; DDL. `CREATE` on the database only so Flyway can create the schema. |
| `<service>_app` (e.g. `gateway_app`) | The running service's connection pool | `USAGE` on its own schema and `SELECT/INSERT/UPDATE/DELETE` on tables, granted through `ALTER DEFAULT PRIVILEGES` in migration V1. No DDL, no access to other schemas, and no read access to Flyway's history table. |

- The roles are created once by [`deploy/db/bootstrap.sql`](../../deploy/db/bootstrap.sql), run as an admin role. Locally the compose `db` container runs it on first start; on Neon it's run once by hand.
- Spring Boot uses separate credentials for Flyway (`spring.flyway.user`) and for the datasource.
- Grants live in versioned migrations, so a later migration can narrow them. For example, Phase 4 will revoke `UPDATE, DELETE` on the audit table from `gateway_app`.

## Consequences
- SQL injection through the running service can't change the schema or read another service's data.
- `DatabaseLeastPrivilegeTest` runs the real `bootstrap.sql` through `psql` in a Testcontainers Postgres. It then checks that `gateway_app` is denied DDL, denied `DROP`/`ALTER` on owner tables, denied the migration history and denied a foreign schema, while plain DML works. Adding `GRANT CREATE ON SCHEMA gateway TO gateway_app` to V1 makes the test fail. That was checked on purpose.
- The migration credentials are still present in the running container, because Flyway runs at startup. A stricter setup would run migrations as a separate CD step. That is overkill for one free instance and is noted as a production difference.
- Every new service repeats the pattern in `bootstrap.sql` (`pharmacy_*` in Phase 7, `phi_*` in Phase 8).
