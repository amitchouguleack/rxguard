# Risk Analysis (ISO 14971-style)

> Development process modeled on IEC 62304 / 21 CFR Part 11 practices — not a regulated device and not validated.

**Status:** starter hazard list (Phase 0). Causes, severity and probability estimates, verification tests and residual risk are filled in as each control is built. Every control links to a requirement in [`REQUIREMENTS.yml`](REQUIREMENTS.yml).

| ID | Hazard | Potential harm | Planned controls (requirements) |
|---|---|---|---|
| RISK-001 | Missed drug interaction, allergy, or renal dose issue | Adverse drug event | REQ-CDS-001, REQ-CDS-002, REQ-CDS-003, REQ-INT-001 |
| RISK-002 | Wrong patient selected or matched | Medication given to the wrong person | REQ-UI-005, REQ-AUTH-003, REQ-INT-001, REQ-UI-002 |
| RISK-003 | Wrong dose, unit, or look-alike drug | Overdose or underdose | REQ-UI-003, REQ-UI-004, REQ-RX-005, REQ-CDS-003 |
| RISK-004 | PHI disclosed to an unauthorized party | Privacy breach | REQ-PHI-001 to 005, REQ-SEC-001, REQ-SEC-002, REQ-AUTH-002, REQ-AUTH-003, REQ-AUTH-005 |
| RISK-005 | Controlled-substance misuse not surfaced | Overdose, diversion | REQ-RX-003, REQ-RX-004, REQ-CDS-004, REQ-AUTH-004 |
| RISK-006 | Unauthorized or impersonated prescriber | Fraudulent prescriptions | REQ-RX-001, REQ-RX-002, REQ-AUTH-001, REQ-AUTH-004 |
| RISK-007 | Prescription lost, duplicated, or altered in transit | Missed therapy or double dispensing | REQ-RX-006, REQ-SEC-002, REQ-SEC-003, REQ-INT-002, REQ-INT-003 |
| RISK-008 | Audit trail altered or deleted | Undetected misuse | REQ-PHI-002 |
| RISK-009 | Alert fatigue causes a serious alert to be ignored | Adverse drug event | REQ-CDS-004, REQ-CDS-005 |
| RISK-010 | Demo mistaken for a clinical tool | Decisions made on synthetic data | REQ-UI-001 |
