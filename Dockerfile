# Build stage
FROM mcr.microsoft.com/dotnet/sdk:10.0 AS build
WORKDIR /src

COPY NuGet.config .
COPY MyFarmProduce/MyFarmProduce.csproj MyFarmProduce/
COPY MyFarmProduce.Application/MyFarmProduce.Application.csproj MyFarmProduce.Application/
COPY MyFarmProduce.Common/MyFarmProduce.Common.csproj MyFarmProduce.Common/
COPY MyFarmProduce.Domain/MyFarmProduce.Domain.csproj MyFarmProduce.Domain/
COPY MyFarmProduce.Infrastructure/MyFarmProduce.Infrastructure.csproj MyFarmProduce.Infrastructure/
RUN dotnet restore MyFarmProduce/MyFarmProduce.csproj

COPY MyFarmProduce/ MyFarmProduce/
COPY MyFarmProduce.Application/ MyFarmProduce.Application/
COPY MyFarmProduce.Common/ MyFarmProduce.Common/
COPY MyFarmProduce.Domain/ MyFarmProduce.Domain/
COPY MyFarmProduce.Infrastructure/ MyFarmProduce.Infrastructure/

RUN dotnet publish MyFarmProduce/MyFarmProduce.csproj -c Release -o /app/publish --no-restore

# Runtime stage
FROM mcr.microsoft.com/dotnet/aspnet:10.0 AS final
WORKDIR /app
COPY --from=build /app/publish .

ENV ASPNETCORE_ENVIRONMENT=Production
EXPOSE 8080

# Render injects PORT at container start; default to 8080 for local `docker run`.
ENTRYPOINT ["/bin/sh", "-c", "ASPNETCORE_URLS=http://+:${PORT:-8080} exec dotnet MyFarmProduce.dll"]
