// Copyright (c) 2026 Amit Chougule. All rights reserved.

using PharmacySystem.Api;

var builder = WebApplication.CreateBuilder(args);
var app = builder.Build();

app.MapHealthEndpoints();

app.Run();

/// <summary>Entry point, exposed so integration tests can host the app.</summary>
public partial class Program;
