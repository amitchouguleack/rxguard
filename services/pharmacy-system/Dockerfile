# Copyright (c) 2026 Amit Chougule. All rights reserved.
# syntax=docker/dockerfile:1
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src
COPY Directory.Build.props .editorconfig ./
COPY src/PharmacySystem.Api/PharmacySystem.Api.csproj src/PharmacySystem.Api/
RUN dotnet restore src/PharmacySystem.Api/PharmacySystem.Api.csproj
COPY src src
RUN dotnet publish src/PharmacySystem.Api/PharmacySystem.Api.csproj -c Release -o /out --no-restore

FROM mcr.microsoft.com/dotnet/aspnet:10.0
WORKDIR /app
COPY --from=build /out .
ENV ASPNETCORE_HTTP_PORTS=8081
USER $APP_UID
EXPOSE 8081
ENTRYPOINT ["dotnet", "PharmacySystem.Api.dll"]
