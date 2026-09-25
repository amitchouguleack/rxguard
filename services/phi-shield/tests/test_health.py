# Copyright (c) 2026 Amit Chougule. All rights reserved.
import pytest
from fastapi.testclient import TestClient

from phi_shield.main import app

client = TestClient(app)


@pytest.mark.req("REQ-OPS-001")
def test_health_reports_up_with_service_name():
    response = client.get("/health")

    assert response.status_code == 200
    assert response.json() == {"status": "UP", "service": "phi-shield"}


@pytest.mark.req("REQ-UI-001")
def test_root_carries_synthetic_data_notice():
    assert client.get("/").json()["notice"] == "SYNTHETIC DATA — NOT FOR CLINICAL USE"
