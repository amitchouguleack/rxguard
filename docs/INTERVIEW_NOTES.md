# Interview Notes

One section per phase: what was built, why, trade-offs, likely questions, and a real "what broke" story.

---

## Phase 0: Foundation

### What was built
- A monorepo with a runnable "hello + `/health`" skeleton in each language:
  - rx-gateway: Java 25, Spring Boot 4.1.1
  - pharmacy-system: .NET 10 minimal API
  - phi-shield: Python 3.12, FastAPI
  - interaction-engine: C++20 library with a C ABI
  - web: React + TypeScript
- docker compose profiles, so the 2-core Codespace only runs what the current task needs.
- A dev container that pins every toolchain, and caches Emscripten and Maven in named volumes.
- A requirements baseline (33 IDs), a starter hazard list (10 risks), a test-tagging convention for all five languages, and a CI check that rejects tests tagged with unknown IDs.
- Path-filtered CI with one job per language and a single `ci-ok` gate job for branch protection.
- Four ADRs: Java version, monorepo, free hosting, and out of scope.

### Why it was designed this way
- **Verify, don't assume, the Java version.** The Spring Boot 4.1 docs say it supports Java 17 through 26, and FFM has been final since Java 22. That made Java 25 safe with no preview flags (ADR 0001).
- **Requirements before code.** Tagging tests with requirement IDs from day one means the Phase 13 traceability matrix is generated from real data, not reconstructed after the fact.
- **A single gate job.** Path filtering skips jobs, and branch protection can't require a job that sometimes doesn't run. `ci-ok` aggregates results: skipped counts as a pass, failed or cancelled counts as a fail.
- **Lint enforces the copyright header in every language** (Spotless, `dotnet format` IDE0073, ruff CPY001, a clang-format wrapper script, and a Node script). This keeps the "all rights reserved" rule enforced by tools instead of by memory.

### Trade-offs
- One monorepo means simpler cross-service changes, but CI must stay path-filtered to stay fast.
- Pinning GitHub Actions to commit SHAs is safer against a compromised tag, but harder to read. Dependabot keeps the SHAs current.
- FluentAssertions is pinned to 7.x, the last Apache-2.0 release. 8.x is under a commercial license. Staying license-clean matters more than new assertion features.
- ESLint is pinned to 9 because the accessibility plugin doesn't support 10 yet.

### Likely interview questions
1. **"Why four languages? Isn't that over-engineering?"**
   Each language matches what that role uses in real healthcare IT. Java is common for interface engines and FHIR servers (HAPI). Pharmacy systems are often .NET. NLP and data work are in Python. Performance-critical, deterministic checks are in C++, which also compiles to WASM for the browser. The point is to show integration across vendor boundaries, which is the hard part of healthcare IT. The monorepo and shared contracts keep it manageable for one person.
2. **"How do you prove a requirement is tested?"**
   Every requirement has an ID in `REQUIREMENTS.yml`. Tests carry that ID using each language's native mechanism: JUnit `@Tag`, a pytest marker, an xUnit `Trait`, a GoogleTest name prefix, or the Vitest title. CI already fails if a test references an unknown ID. In Phase 13 a generator joins test results with requirements and risks, and fails the build if any requirement has no passing test.
3. **"How do path-filtered jobs work with required status checks?"**
   A skipped job never reports a status, so requiring it directly would block unrelated PRs. I added an always-running `ci-ok` job that depends on all the others and fails only if one of them failed or was cancelled. Branch protection requires just that one check.

### What broke and how I fixed it
The second web unit test failed with "Found multiple elements with the role link". The first test had passed. The DOM dump showed two app shells, which meant the first render was never unmounted. Testing Library's auto-cleanup hooks into a global `afterEach`, and I had deliberately turned off Vitest globals to keep imports explicit. I added an explicit `cleanup()` in the shared setup file. Every future component test inherits it. See journal entry and issue #4.

---

## Phase 1: Walking skeleton, deployed on day one

### What was built
- **rx-gateway on PostgreSQL:**
  - Flyway manages a `gateway` schema.
  - There are two roles. `gateway_owner` runs migrations. `gateway_app`, used by the running service, has data access only ([ADR 0005](decisions/0005-database-roles.md)).
- **`/api/version`:** reports the git SHA baked into the image.
- **CORS:** open only for `GET /health` and `/api/version`, and only from the GitHub Pages origin.
- **Image tuned for Render's free plan** (512 MB, 0.1 CPU) and measured under those exact limits ([ADR 0006](decisions/0006-jvm-memory-512mb.md)).
- **CD** ([ADR 0007](decisions/0007-image-based-deploys.md)):
  - builds the image once and tags it with the git SHA in GHCR
  - deploys that exact tag to Render through the deploy hook (`imgURL`)
  - waits for the SHA to go live, then checks health and database health
  - publishes the web UI to GitHub Pages
  - opens an issue if any step fails
- **Web:** a "Waking up services…" panel that polls each service's `/health`.
- **Live:** <https://amitchouguleack.github.io/rxguard/> and <https://rxguard-rx-gateway.onrender.com/api/version>.

### Why it was designed this way
- **Owner and runtime roles are separate** because later phases need guarantees at the database level. For example, Phase 4's audit table must reject `UPDATE`/`DELETE` from the running service, and that's impossible if the service owns the table.
- **Prebuilt, SHA-tagged images** mean the thing that was tested is exactly the thing that runs. Rollback is then just "deploy the previous tag".
- **The smoke test waits for the SHA, not just for `/health`.** Otherwise the old instance could still be answering, and the test would pass against the wrong build.
- **Deploy targets are switched on by repository variables.** That let the pipeline merge before the hosting accounts existed, so the first real run was a switch flip, not a code change.

### Trade-offs
- **C1-only JIT:** cold start about halves (56–61 s vs 120 s at 0.1 CPU), but sustained throughput is lower. That's the right trade for a mostly idle demo; Phase 12 load tests will report both.
- **Flyway at startup:** the migration credentials are present in the running container. A stricter setup runs migrations as a separate CD step, which isn't worth it on one free instance, and the ADR says so.
- **No keep-alive pinging:** three awake services would need about 2,160 instance-hours a month, against 750 free. Visitors see a wake-up panel instead.
- **Every merge redeploys**, including docs-only merges. That's fine at the current merge rate.

### Likely interview questions
1. **"How do you know a deploy actually succeeded?"**
   The image carries its own git SHA, and `/api/version` reports it. After the deploy hook fires, `deploy/smoke.sh` polls until the live SHA equals the one just built. Only then does it check `/health` and actuator health, which includes the database. If any step fails, the pipeline fails and opens a GitHub issue automatically. On the first automatic run, the new build was live 45 s after the smoke test started.
2. **"What does least privilege mean for your database?"**
   Each service has its own schema, an owner role for DDL and a runtime role for DML. The runtime role gets table access through `ALTER DEFAULT PRIVILEGES` in a versioned migration. It can't create or drop tables, can't read another service's schema, and can't even read Flyway's history table. There's a Testcontainers test for each of those, and I confirmed the test fails if I grant `CREATE` by mistake.
3. **"How did you fit a Spring Boot service into 512 MB and 0.1 CPU?"**
   I measured before and after with the container limited to exactly those numbers. With Serial GC, the C1 compiler only, and capped metaspace and code cache, the first `/health` came in 56–61 s using about 121–128 MiB. JVM defaults took 120 s and about 161–182 MiB. That leaves headroom for HAPI FHIR and the authorization server, and the plan says to re-measure and split the service if it goes over about 450 MB.

### What broke and how I fixed it
When I tried to measure the gateway under Render's limits, it died with `Connect timed out` to `db:5432`. My first guess was that 0.1 CPU starved the JVM. A 2-second `pg_isready` from a sibling container also failed, though, and so did `ping` and `nc`, while the host could reach Postgres fine. That moved the problem to the network. The nftables Docker rules were correct, but the Codespace also had legacy iptables rules with a FORWARD policy of DROP that only knew the default `docker0` bridge. Its drop counter (17 packets / 1,044 bytes) matched the traffic exactly. Docker 29 writes nftables rules only, so compose bridges never got a legacy allow rule. The fix was a narrow rule allowing traffic within compose bridges, and the README documents it (issue #13).

Also real: the first Render deploy failed with `password authentication failed for user gateway_owner`. I fixed it by rotating both role passwords in Neon and updating them in Render. I didn't diagnose the exact mismatch, and I say that rather than guess (issue #15).
