# ADR 0004: Deliberately out of scope

- **Status:** Accepted
- **Date:** 2026-09-25

## Context
Healthcare IT covers far more than prescribing. Adding unrelated standards would make the project look broader but less credible, and some options would break the $0 budget. Being explicit about what is left out, and why, is part of the design.

## Decision
The following are **not built**. Each one is explained in the docs instead.

| Topic | Why it is not in RxGuard | What RxGuard does instead |
|---|---|---|
| **DICOM and PACS** (medical imaging) | Prescribing workflows don't handle images, so adding them would be artificial. | `docs/CLINICAL_WORKFLOWS.md` explains where PACS sits in the ecosystem. It could be a separate future project (for example Orthanc + OHIF viewer, both free). |
| **Embedded C/C++ and RTOS** (physical devices) | Needs real hardware and a device context. RxGuard is software-only. | The C++ interaction engine uses the same discipline: deterministic logic, no undefined behavior, sanitizers, bounded inputs, fuzzing and property tests. |
| **AWS HealthLake, Google Cloud Healthcare API, Azure Health Data Services** | They cost money, and real use needs a BAA. | A server-agnostic FHIR layer tested against the free HAPI FHIR JPA server, plus an ADR (Phase 10) mapping it to each managed service. |
| **Splunk and Datadog** | Paid. | Prometheus, Grafana, Jaeger and OpenTelemetry (free), with dashboards and alert rules committed as code. |
| **HL7 v3 messaging** | Largely superseded in the US by v2 and FHIR. | Explained in `docs/HL7V2_INTERFACE.md`, with an optional minimal C-CDA export. |

## Consequences
- The scope stays focused on the prescribing lifecycle: registration → labs → prescribing → pharmacy → dispense → audit.
- Reviewers can see these were conscious decisions, not gaps that were missed.
- The README repeats this table in short form.
