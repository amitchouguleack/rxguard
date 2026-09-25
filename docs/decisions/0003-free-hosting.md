# ADR 0003: $0 hosting: Render + Neon + GitHub Pages + GHCR

- **Status:** Accepted
- **Date:** 2026-09-25

## Context
The project has a hard $0 budget: no paid plans, and no trials that need a credit card. It still needs a public demo URL that recruiters can open. Free-tier limits change, so the limits below were read from each provider's docs on the date shown.

| Provider | Limit (as documented) | Source | Checked |
|---|---|---|---|
| Render free web service | 0.1 CPU / 512 MB RAM; spins down after 15 minutes with no inbound traffic; **750 free instance-hours per month in total** | <https://render.com/docs/free> | 2026-09-25 |
| Render free Postgres | Expires 30 days after creation | <https://render.com/docs/free> | 2026-09-25 |
| Neon free plan | 0.5 GB storage per project, 100 CU-hours per project, scales to zero | <https://neon.com/docs/introduction/plans> | 2026-09-25 |
| GitHub Pages / Actions / GHCR | Free for public repositories | GitHub docs | 2026-09-25 |

## Decision
- **rx-gateway, pharmacy-system, phi-shield** run on Render free web services (Docker images).
- **PostgreSQL** runs on Neon's free plan. Render's free Postgres is not used because it expires after 30 days.
- The **web UI, WASM build, docs and status page** go on GitHub Pages.
- **Container images** go to GHCR. **CI/CD, uptime checks and the reference FHIR server** run in GitHub Actions.
- **No keep-alive pinging.** Three always-on services would need about 3 × 720 = 2,160 instance-hours a month, far above the 750-hour allowance. Services are allowed to sleep instead. The UI shows a "Waking up services…" panel, and the README explains the trade-off.
- Uptime checks run only every 6 hours so they barely use instance-hours.

## Consequences
- Cold starts can take close to a minute, and longer for the JVM, which gets only 0.1 CPU. This is documented, and a demo GIF is provided as a fallback.
- rx-gateway must fit in 512 MB. Its real memory use will be measured once HAPI FHIR, HL7v2 and the authorization server are in (Phase 10). If it is over about 450 MB, a follow-up ADR splits a component into its own service.
- The free hosts have no Business Associate Agreement (BAA), so **real PHI can never be stored on them**. This is one reason the demo uses synthetic data only (see `docs/HIPAA_SAFEGUARDS.md` once written).
- Heavy tooling (Prometheus, Grafana, Jaeger, HAPI FHIR JPA server) runs only in the Codespace and in CI, never on the free hosts.
