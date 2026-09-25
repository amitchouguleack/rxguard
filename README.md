# RxGuard

[![ci](https://github.com/amitchouguleack/rxguard/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/amitchouguleack/rxguard/actions/workflows/ci.yml)
[![License: All rights reserved](https://img.shields.io/badge/license-All%20rights%20reserved-red)](LICENSE)

End-to-end e-prescribing simulation: HL7 v2 → FHIR R4, SMART on FHIR, NCPDP SCRIPT-inspired messaging, EPCS, and PHI safeguards, built as four services in four languages (Java · C++ · C# · Python) plus a React UI.

**Solo project — designed, built, tested, deployed, and documented end to end by Amit Chougule.**

> ⚠️ **Not for cloning or reuse.** © Amit Chougule. All rights reserved. This repo is public so recruiters and hiring managers can review my work. It is not open source. You may view the code and run the demo to evaluate me as a candidate, but you may not copy, clone for reuse, fork for your own projects, or redistribute any part of it without my written permission. See [LICENSE](LICENSE).

> **SYNTHETIC DATA — NOT FOR CLINICAL USE.** Every patient, prescriber, and pharmacy is generated.

## Status

Built phase by phase. Only finished features are listed.

| Phase | Scope | Status |
|---|---|---|
| 0 | Foundation: dev container, service skeletons, CI, requirements baseline | ✅ |
| 1 | Walking skeleton deployed ($0 hosting) | ⏳ |

## Architecture (planned)

| Service | Language | Role |
|---|---|---|
| `services/rx-gateway` | Java 25 + Spring Boot 4.1 | Prescriber side: validation, workflow, CDS, PHI vault, audit, SMART on FHIR, FHIR R4 API, HL7 v2 |
| `services/interaction-engine` | C++20 | Deterministic interaction, allergy, and renal-dose checks. Native library (Java FFM) + WebAssembly |
| `services/pharmacy-system` | C# / .NET 10 | Pharmacy system: receives prescriptions, dispense queue, fill status |
| `services/phi-shield` | Python 3.12 + FastAPI | De-identification, terminology normalization, access-anomaly detection |
| `web` | React + TypeScript | Clinical UI, runs as a SMART app |

Design decisions are recorded as [ADRs](docs/decisions/README.md), including what is [deliberately out of scope](docs/decisions/0004-out-of-scope.md).

## How to run in Codespaces (for evaluation)

1. On the repo page, click **Code → Codespaces → Create codespace on main**. The first build installs every toolchain (JDK 25, .NET 10, Python 3.12, Node LTS, clang, Emscripten) and takes several minutes.
2. Start only what you want to look at (the Codespace has 2 cores):
   ```bash
   docker compose --profile phi up        # phi-shield on :8000
   docker compose --profile full up       # db + all three services
   ```
3. Check health: `curl localhost:8080/health`, `curl localhost:8081/health`, `curl localhost:8000/health`.
4. Web UI: `cd web && npm run dev`, then open the forwarded port 5173.

Run each service's tests:

```bash
(cd services/rx-gateway && ./mvnw verify)
(cd services/pharmacy-system && dotnet test PharmacySystem.slnx)
(cd services/phi-shield && .venv/bin/pytest)
(cd services/interaction-engine && cmake -S . -B build -G Ninja -DRXG_SANITIZE=ON && cmake --build build && ctest --test-dir build)
(cd web && npm test)
```

Codespaces stop after **30 minutes idle** by default, so nothing keeps running (or using free hours) when you walk away.

## Engineering process

- Every requirement has an ID in [`REQUIREMENTS.yml`](docs/regulatory/REQUIREMENTS.yml), and tests are tagged with the IDs they verify ([convention](docs/regulatory/TEST_TAGGING.md)).
- Every phase has an issue, a branch, a PR, and green CI before merge. CI is described in [CI_CD.md](docs/CI_CD.md).
- Real problems and how they were debugged: [ENGINEERING_JOURNAL.md](docs/ENGINEERING_JOURNAL.md).
