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
