# ADR 0007: Deploy prebuilt, SHA-tagged images instead of building on Render

- **Status:** Accepted
- **Date:** 2026-09-26

## Context
Render can either build a service from the Git repository itself, or pull a prebuilt image from a registry. The free plan's build environment is limited, and a Render-side build would repeat work CI already did. We also need to know exactly which commit is live, and to roll back quickly.

## Decision
- CD builds the image once in GitHub Actions and pushes it to GHCR with two tags: the full git SHA and `main`.
- The Render service is image-backed (`runtime: image` in [`deploy/render.yaml`](../../deploy/render.yaml)). CD triggers the Render deploy hook with `imgURL=ghcr.io/amitchouguleack/rxguard-rx-gateway:<sha>`, so Render runs exactly the image that was built and tested.
- The image carries its SHA (`RXGUARD_GIT_SHA`), and `/api/version` reports it. `deploy/smoke.sh` waits until the live SHA matches before checking health.
- Deploy targets are switched on with repository variables (`RENDER_DEPLOY_ENABLED`, `PAGES_ENABLED`), so the pipeline can merge before the hosting accounts exist.

## Consequences
- Rollback means calling the deploy hook with an older SHA tag. No rebuild is needed. This is documented in `docs/CI_CD.md` and gets a real test in Phase 13.
- The GHCR package must be public, or Render needs registry credentials. Public is fine: the image contains no secrets and only synthetic data.
- Merges that change only docs still produce a new image and deploy. That's acceptable at the current merge rate. If it becomes noisy, CD can skip the deploy when the gateway paths didn't change.
