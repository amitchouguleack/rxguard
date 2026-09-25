# ADR 0002: Single monorepo for all services

- **Status:** Accepted
- **Date:** 2026-09-25

## Context
RxGuard has four services in four languages (Java, C++, C#, Python) plus a React web UI. They share contracts: OpenAPI specs, NCPDP XML schemas, FHIR examples, HL7 v2 samples and terminology subsets. Changes to a contract usually touch more than one service at once. There is one engineer, and a portfolio reviewer should be able to see the whole system in one place.

## Decision
Keep everything in one repository:
- `services/<name>` for each backend and the C++ engine
- `web/` for the UI
- `contracts/`, `terminology/`, `deploy/` and `docs/` at the root

CI uses **path filters**, so a change to one service only builds and tests that service. A change to `contracts/` fans out to every consumer.

## Consequences
- One PR can change a contract and all of its producers and consumers together, which avoids version skew between repos.
- One traceability matrix, one issue tracker and one history for the whole system.
- CI has to stay fast with path filtering and per-language caches. Otherwise every commit would build five toolchains.
- Each service still has its own build file, Dockerfile, DB schema and DB user. The monorepo is a source-control choice, not a shared-runtime choice.
