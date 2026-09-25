// Copyright (c) 2026 Amit Chougule. All rights reserved.

namespace PharmacySystem.Api;

/// <summary>Liveness endpoint polled by the web UI and smoke tests.</summary>
public static class HealthEndpoints
{
    public const string ServiceName = "pharmacy-system";
    public const string SyntheticNotice = "SYNTHETIC DATA — NOT FOR CLINICAL USE";

    public static IEndpointRouteBuilder MapHealthEndpoints(this IEndpointRouteBuilder app)
    {
        app.MapGet("/health", () => Results.Ok(new { status = "UP", service = ServiceName }));
        app.MapGet("/", () => Results.Ok(new { message = $"Hello from {ServiceName}", notice = SyntheticNotice }));
        return app;
    }
}
