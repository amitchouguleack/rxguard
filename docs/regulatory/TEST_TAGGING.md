# Test-tagging convention

Every test that verifies a requirement names its ID from [`REQUIREMENTS.yml`](REQUIREMENTS.yml). CI rejects a tag that does not match a known ID (`tools/traceability/validate_requirements.py`). In Phase 13 the same tags feed the generated traceability matrix.

| Language | How to tag | Example |
|---|---|---|
| Java (JUnit 5) | `@Tag("REQ-…")` | `@Tag("REQ-OPS-001")` |
| Python (pytest) | `@pytest.mark.req("REQ-…")` | `@pytest.mark.req("REQ-OPS-001")` |
| C# (xUnit) | `[Trait("Req", "REQ-…")]` | `[Trait("Req", "REQ-OPS-001")]` |
| C++ (GoogleTest) | Test name starts with `REQ_…` (dashes become underscores) | `TEST(Engine, REQ_OPS_001_VersionIsReported)` |
| TypeScript (Vitest / Playwright) | `[REQ-…]` in the test title | `it('[REQ-UI-001] shows the banner', …)` |

Rules:
- One test may verify several requirements. Add one tag per requirement.
- Tag the test that actually proves the behavior, not a helper or setup test.
- A requirement with `verification: Test` counts as verified only when a tagged test passes in CI.
