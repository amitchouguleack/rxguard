# RxGuard build progress

**Phase 2 of 14 — 16% done (17 of 109 steps)**

**What's next:** Phase 2, step 1: entities, Flyway migrations, and the synthetic seed loader.

Each step is ticked the moment it's finished, with a link to the PR that delivered it. The steps come from the build plan. Phases 0–14 are built in order, each on its own branch and PR with green CI.

| Phase | Name | Steps done |
|---|---|---|
| 0 | Foundation | 10 / 10 ✅ |
| 1 | Walking skeleton, deployed on day one | 7 / 7 ✅ |
| 2 | Core domain, validation, and medical terminologies | 0 / 8 |
| 3 | Identity: OAuth2, OIDC, SMART on FHIR, EPCS step-up | 0 / 7 |
| 4 | PHI vault, end-to-end encryption, audit, privacy rights | 0 / 10 |
| 5 | Prescription workflow, risk scoring, clinical decision support | 0 / 6 |
| 6 | C++ interaction engine (native + WASM + Java FFM) | 0 / 8 |
| 7 | C# pharmacy-system | 0 / 7 |
| 8 | Python phi-shield | 0 / 7 |
| 9 | HL7 v2 interface | 0 / 7 |
| 10 | HL7 FHIR R4 API | 0 / 6 |
| 11 | Clinical UI/UX + accessibility | 0 / 7 |
| 12 | Observability, monitoring, resilience, and game days | 0 / 8 |
| 13 | Traceable CI/CD + hardening | 0 / 5 |
| 14 | Docs + polish | 0 / 6 |

---

## Phase 0: Foundation ✅
- [x] 0. Authorship setup: local exclude rules, commit-msg hook, identity check ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 1. Dev container: JDK 25, .NET LTS, Python 3.12, Node LTS, clang/libFuzzer, Emscripten, docker-in-docker ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 2. Java 25 + Spring Boot compatibility verified; ADRs 0001–0004 ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 3. Monorepo skeleton with hello + /health per service, docker compose profiles ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 4. GitHub setup: issue/PR templates, CODEOWNERS, dependabot, labels, branch protection ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 5. `REQUIREMENTS.yml` starter set ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 6. Test-tagging convention ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 7. `ci.yml`: path-filtered build + lint + test per service, with caches ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 8. Engineering journal ([#5](https://github.com/amitchouguleack/rxguard/pull/5))
- [x] 9. README skeleton with badges and Codespaces instructions ([#5](https://github.com/amitchouguleack/rxguard/pull/5))

## Phase 1: Walking skeleton, deployed on day one ✅
- [x] 1. rx-gateway on Postgres with Flyway, `gateway` schema, least-privilege DB roles ([#14](https://github.com/amitchouguleack/rxguard/pull/14))
- [x] 2. Neon + Render setup and GitHub secrets (done by hand; first-deploy auth failure logged in [#16](https://github.com/amitchouguleack/rxguard/pull/16))
- [x] 3. `deploy/render.yaml` and a multi-stage Dockerfile tuned for 512 MB ([#14](https://github.com/amitchouguleack/rxguard/pull/14))
- [x] 4. `cd.yml`: GHCR image by SHA → Render deploy hook → smoke test → issue on failure ([#14](https://github.com/amitchouguleack/rxguard/pull/14), first live run on [#16](https://github.com/amitchouguleack/rxguard/pull/16))
- [x] 5. React skeleton on GitHub Pages with the "Waking up services…" panel ([#14](https://github.com/amitchouguleack/rxguard/pull/14))
- [x] 6. Pipeline documented in `docs/CI_CD.md` with a Mermaid diagram ([#14](https://github.com/amitchouguleack/rxguard/pull/14))
- [x] 7. Public URL live: <https://amitchouguleack.github.io/rxguard/> ([#16](https://github.com/amitchouguleack/rxguard/pull/16), [#17](https://github.com/amitchouguleack/rxguard/pull/17))

## Phase 2: Core domain, validation, and medical terminologies
- [ ] 1. Entities, Flyway migrations, dev seed loader (30 patients, 6 prescribers, 3 pharmacies, ~40 medications, allergies, diagnoses, labs)
- [ ] 2. License-safe terminology subsets (RxNorm, ICD-10-CM, LOINC, SNOMED CT IDs), ConceptMaps, `docs/TERMINOLOGY.md`
- [ ] 3. `@ValidNpi` (Luhn with 80840 prefix)
- [ ] 4. `@ValidDea` (checksum)
- [ ] 5. `@ValidIcd10Cm`, `@ValidLoinc` (check digit), RxNorm lookup against the subset
- [ ] 6. Schedule rules (CII no refills + DEA, CIII–CV max 5 refills, daysSupply 1–90, indication required)
- [ ] 7. RFC 7807 errors and OpenAPI docs exported to `contracts/`
- [ ] 8. Tests: validators, jqwik property tests, Testcontainers repositories

## Phase 3: Identity: OAuth2, OIDC, SMART on FHIR, EPCS step-up
- [ ] 1. Spring Authorization Server: OIDC + SMART discovery, JWKS, auth code + PKCE, refresh rotation, client credentials
- [ ] 2. Login UI with demo users per role, BCrypt, login rate limiting (Bucket4j)
- [ ] 3. SMART scopes → Spring Security authorities; patient launch context in the token
- [ ] 4. EPCS step-up: TOTP enrollment, fresh TOTP (5-minute step-up claim) to sign CII–CV
- [ ] 5. Service accounts for pharmacy-system and phi-shield
- [ ] 6. Security tests for every endpoint (role/scope, no/expired token, other patient → 404, missing PKCE, reused/expired TOTP)
- [ ] 7. `docs/THREAT_MODEL.md` (STRIDE per service and data flow)

## Phase 4: PHI vault, end-to-end encryption, audit, privacy rights
- [ ] 1. AES-256-GCM field encryption with keyId, key-rotation CLI, rotation test
- [ ] 2. Blind-index search (last name + DOB, MRN)
- [ ] 3. JWE for NCPDP payloads on top of HMAC; tampered/wrong-key/replay tests
- [ ] 4. Hash-chained append-only audit log via AOP, `GET /api/audit/verify`, no UPDATE/DELETE for the runtime role
- [ ] 5. Break-glass endpoint
- [ ] 6. Accounting of disclosures endpoint
- [ ] 7. Part 11-style e-signatures (manifestation, record-hash binding, immutability, versioning)
- [ ] 8. GDPR/CCPA features: consent, data-subject requests with legal hold, portable export
- [ ] 9. PHI log masking + test that seeded names never appear in logs
- [ ] 10. `HIPAA_SAFEGUARDS.md`, `PRIVACY_REGULATIONS.md`, `PART11_ESIGNATURES.md`

## Phase 5: Prescription workflow, risk scoring, clinical decision support
- [ ] 1. Endpoints: create, sign (e-signature + EPCS step-up), transmit, cancel, get, list, risk result
- [ ] 2. `PrescriptionStateMachine` with tests for every transition
- [ ] 3. Risk engine (Strategy pattern, one class per rule, reasons, never auto-blocks, ADR)
- [ ] 4. Lab-aware CDS: renal dose warnings from latest LOINC eGFR, stale-lab advisory
- [ ] 5. Alert-fatigue design: interruptive vs passive, override reasons, override rates, ADR
- [ ] 6. "Patient story" integration test; PIT mutation score recorded

## Phase 6: C++ interaction engine (native + WASM + Java FFM)
- [ ] 1. C++20 library: DDI, allergy, duplication, renal rules, batch check, deterministic
- [ ] 2. Illustrative interaction table (~60 pairs) in an efficient structure
- [ ] 3. `extern "C"` API with defensive input handling
- [ ] 4. Tests: GoogleTest, RapidCheck, libFuzzer target, ASan/UBSan, nightly fuzz
- [ ] 5. Google Benchmark results in `docs/PERFORMANCE.md`
- [ ] 6. Java binding via FFM, called on transmit; Dockerfile C++ build stage
- [ ] 7. WASM build via Emscripten for in-browser warnings
- [ ] 8. ADR: why C++ with two runtimes

## Phase 7: C# pharmacy-system
- [ ] 1. Minimal API with `pharmacy` schema and DB user, EF Core migrations
- [ ] 2. `POST /ncpdp/newrx`: JWE decrypt, HMAC/timestamp/nonce checks, XML schema validation, ack
- [ ] 3. Dispense queue (OIDC via rx-gateway): list, view, dispense, deny; RxFill callback + FHIR MedicationDispense
- [ ] 4. rx-gateway outbox (retry, backoff + jitter, dead-letter) and idempotent callback handler
- [ ] 5. HTTP resilience handler (timeout, retry, circuit breaker)
- [ ] 6. xUnit tests: crypto, queue, FsCheck round trip, FHIR validation, Testcontainers
- [ ] 7. Added to compose, render.yaml, CI, CD + smoke tests

## Phase 8: Python phi-shield
- [ ] 1. `POST /v1/deidentify` with custom Presidio recognizers
- [ ] 2. `POST /v1/export/deidentified` (Safe Harbor, consent-only)
- [ ] 3. `POST /v1/terminology/normalize` with real accuracy reported
- [ ] 4. `POST /v1/anomalies/scan` with explainable outlier detection
- [ ] 5. PHI evaluation set with real precision/recall
- [ ] 6. Client-credentials auth, no request-body logging, log test
- [ ] 7. pytest + Hypothesis + ruff + 85% coverage; memory measured; compose/render/CI/CD

## Phase 9: HL7 v2 interface
- [ ] 1. HAPI HL7v2 inbound: ADT^A04/A08, ORU^R01, ACK AA/AE/AR
- [ ] 2. Transport: MLLP listener + HL7-over-HTTP endpoint
- [ ] 3. v2 → FHIR mapping (Patient, Observation)
- [ ] 4. Defensive handling: malformed input, unknown codes → work queue, idempotent control IDs, encrypted raw storage
- [ ] 5. Tests: samples, jqwik parser fuzz, MLLP integration
- [ ] 6. HL7 v3 / CDA section (optional C-CDA export)
- [ ] 7. `tools/ehr-sim` hospital EHR simulator

## Phase 10: HL7 FHIR R4 API
- [ ] 1. `/fhir` read/search/create for the listed resources, `$everything`, `$translate`
- [ ] 2. SMART scopes on every interaction; `OperationOutcome` errors
- [ ] 3. Validation against base R4 and US Core
- [ ] 4. Server-agnostic `FhirStore` + HAPI FHIR JPA round trip in CI; managed-services ADR
- [ ] 5. `docs/FHIR_MAPPING.md`
- [ ] 6. FHIR contract tests

## Phase 11: Clinical UI/UX + accessibility
- [ ] 1. Screens: login, prescriber, pharmacist, auditor, patient, PHI Shield, HL7 console, FHIR explorer, architecture
- [ ] 2. Error-preventing UX (Tall Man, dose notation, explicit units, confirmation summary, patient banner)
- [ ] 3. WCAG 2.2 AA; axe-core in Playwright CI
- [ ] 4. Clinician-burden metric (real clicks/keystrokes, before/after)
- [ ] 5. Synthetic-data banner and "Waking up services…" panel on every page
- [ ] 6. Playwright golden path against the full stack in CI
- [ ] 7. Vitest + Testing Library unit tests

## Phase 12: Observability, monitoring, resilience, and game days
- [ ] 1. Correlation ID + `traceparent` via OpenTelemetry, structured JSON logs
- [ ] 2. Health vs readiness endpoints
- [ ] 3. Metrics + Grafana dashboards (observability compose profile)
- [ ] 4. Prometheus alert rules
- [ ] 5. Uptime workflow + status page
- [ ] 6. `docs/DEBUGGING_PLAYBOOK.md`
- [ ] 7. Six game days with blameless postmortems
- [ ] 8. k6 load tests, one real bottleneck fixed (before/after)

## Phase 13: Traceable CI/CD + hardening
- [ ] 1. Pipelines: ci, e2e, quality, security, interop, cd, release, uptime
- [ ] 2. Traceability matrix generator that fails on untested requirements
- [ ] 3. Rollback documented and tested for real
- [ ] 4. Required status checks on `main`: ci, e2e, security, traceability
- [ ] 5. README badges

## Phase 14: Docs + polish
- [ ] 1. Final README in the planned order
- [ ] 2. `docs/CLINICAL_WORKFLOWS.md`
- [ ] 3. `docs/ARCHITECTURE.md` (system, sequence, ER, deployment diagrams)
- [ ] 4. Final pass: formatters, zero warnings, dead code, link check in CI
- [ ] 5. Fresh Codespace runs `docker compose --profile full up` from the README
- [ ] 6. Recruiter-facing drafts (About box, social image, profile README, LinkedIn, resume bullets)
