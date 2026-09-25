# Copyright (c) 2026 Amit Chougule. All rights reserved.
"""FastAPI application entry point."""

from fastapi import FastAPI

SERVICE_NAME = "phi-shield"
SYNTHETIC_NOTICE = "SYNTHETIC DATA — NOT FOR CLINICAL USE"

app = FastAPI(title="RxGuard phi-shield", version="0.0.1")


@app.get("/health")
def health() -> dict[str, str]:
    """Liveness endpoint polled by the web UI and smoke tests."""
    return {"status": "UP", "service": SERVICE_NAME}


@app.get("/")
def hello() -> dict[str, str]:
    return {"message": f"Hello from {SERVICE_NAME}", "notice": SYNTHETIC_NOTICE}
