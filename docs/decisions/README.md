# Architecture Decision Records

Each ADR uses the format **Context → Decision → Consequences**. ADRs are never edited after acceptance except to mark them *Superseded by ADR-NNNN*.

| # | Title | Status |
|---|---|---|
| [0001](0001-java-version.md) | Java 25 LTS with Spring Boot 4.1 | Accepted |
| [0002](0002-monorepo.md) | Single monorepo for all services | Accepted |
| [0003](0003-free-hosting.md) | $0 hosting: Render + Neon + GitHub Pages + GHCR | Accepted |
| [0004](0004-out-of-scope.md) | Deliberately out of scope | Accepted |
| [0005](0005-database-roles.md) | Least-privilege database roles per service | Accepted |
| [0006](0006-jvm-memory-512mb.md) | JVM settings for Render's 512 MB / 0.1 CPU free instance | Accepted |
| [0007](0007-image-based-deploys.md) | Deploy prebuilt, SHA-tagged images instead of building on Render | Accepted |
