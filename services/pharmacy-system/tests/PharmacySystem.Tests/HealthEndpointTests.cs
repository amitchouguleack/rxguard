// Copyright (c) 2026 Amit Chougule. All rights reserved.

using System.Net;
using System.Net.Http.Json;
using FluentAssertions;
using Microsoft.AspNetCore.Mvc.Testing;

namespace PharmacySystem.Tests;

public sealed class HealthEndpointTests(WebApplicationFactory<Program> factory)
    : IClassFixture<WebApplicationFactory<Program>>
{
    private sealed record HealthResponse(string Status, string Service);

    private sealed record HelloResponse(string Message, string Notice);

    [Fact]
    [Trait("Req", "REQ-OPS-001")]
    public async Task Health_ReportsUpWithServiceName()
    {
        using var client = factory.CreateClient();

        var response = await client.GetAsync(new Uri("/health", UriKind.Relative));

        response.StatusCode.Should().Be(HttpStatusCode.OK);
        var body = await response.Content.ReadFromJsonAsync<HealthResponse>();
        body.Should().Be(new HealthResponse("UP", "pharmacy-system"));
    }

    [Fact]
    [Trait("Req", "REQ-UI-001")]
    public async Task Root_CarriesSyntheticDataNotice()
    {
        using var client = factory.CreateClient();

        var body = await client.GetFromJsonAsync<HelloResponse>(new Uri("/", UriKind.Relative));

        body!.Notice.Should().Be("SYNTHETIC DATA — NOT FOR CLINICAL USE");
    }
}
