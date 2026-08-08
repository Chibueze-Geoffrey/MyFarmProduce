# MyFarmProduce

A farm-produce ordering and delivery web app built with **ASP.NET Core MVC (.NET 10)** and **EF Core / PostgreSQL**, using a layered architecture. Customers browse produce, order and pay online, and track delivery; admins manage the catalog, orders, and users. It also includes a real-time community chat, an AI-style support assistant, and dark mode.

## Features

### Customer
- Browse produce grouped by category, keyword search, product details
- Session-backed cart (add / adjust / remove)
- Checkout with delivery details + flat delivery fee
- Online payment (simulated gateway) with callback **and** webhook confirmation; stock decrements on confirmed payment
- Order confirmation, tracking, history, and one-click reorder (adjusts for current stock/price)
- Profile with photo upload; phone/email are locked and changed via an admin-approved request
- Real-time **community chat** (SignalR)
- **AI help assistant** (free, keyless rule-based; answers order/delivery/payment/availability questions from live data)
- **Dark mode** toggle (persisted)
- Forced password change on first login for admin-created accounts

### Admin (separate account type, role-resolved)
- Inventory CRUD, restock, availability toggle, **product image upload**
- Order management: filter, status updates, cancel, refund (logged)
- User management: create/edit/delete customers, approve/reject profile-change requests
- View support tickets
- Admin profile with photo

## Architecture (layered)

| Project | Responsibility |
|---|---|
| `MyFarmProduce` | ASP.NET Core MVC web app (controllers, Razor views, SignalR hub, file storage) |
| `MyFarmProduce.Application` | Service interfaces + DTOs |
| `MyFarmProduce.Domain` | Entities (encapsulated `Product` stock via `ReduceStock`/`Restock`) |
| `MyFarmProduce.Infrastructure` | EF Core `DbContext`, service implementations, migrations, seeding |
| `MyFarmProduce.Common` | Enums, constants |
| `MyFarmProduce.Tests` | xUnit tests |

## Tech stack
- ASP.NET Core MVC, Razor views, Bootstrap 5.3 (dark mode via `data-bs-theme`)
- EF Core 10 (Code First) + PostgreSQL (via Npgsql)
- SignalR for real-time chat
- Cookie authentication (PBKDF2 password hashing — no full Identity stack)

## Getting started

### Prerequisites
- .NET 10 SDK
- PostgreSQL (local install, or via Docker: `docker run --name myfarmproduce-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres`)

### Run
```bash
git clone <your-repo-url>
cd MyFarmProduce
dotnet run --project MyFarmProduce
```
The app applies EF migrations and seeds data (categories, sample products, admin accounts) on startup. Browse to `http://localhost:5085`.

The connection string is in `MyFarmProduce/appsettings.json` (`ConnectionStrings:DefaultConnection`), defaulting to `Host=localhost;Database=myfarmproduce;Username=postgres;Password=postgres`.

## Deploying to Render
- Render has no native .NET runtime, so the app ships as a Docker web service (see `Dockerfile` at repo root).
- Use Render's managed PostgreSQL for the database; set the `ConnectionStrings__DefaultConnection` env var on the web service to the connection string for that database (see `render.yaml` for a ready-to-use Blueprint — it deploys only the web service and expects you to set that env var by hand, since Render's free tier allows just one free database per account).
- Uploaded images (`wwwroot/uploads`) are written to local disk, which is **ephemeral** on Render — they're wiped on every deploy/restart unless you attach a paid persistent disk mounted at that path. Fine for demoing the MVP; revisit before real usage.
- SignalR chat works as-is on a single instance; add a backplane (e.g. Redis) only if you later scale to multiple instances.

### Default accounts
- **Admin:** `admin@myfarmproduce.local` / `Admin@123`
- **Admin:** `chibuezegeoffrey@gmail.com` / `Admin@123`
- Customers self-register, or an admin creates them with the default password `Password@1234` (changed on first login).

Log in via the single `/Account/Login` form — admins are resolved by role automatically.

## Notable design decisions
- **Admins are a separate table** from customers and are created only in the backend (seed/SQL); there is no admin sign-up UI.
- **Payment** is behind an `IPaymentGateway` abstraction with a dev/simulated implementation; swap in Paystack/Flutterwave later.
- **Email/SMS** use logging stubs (`IEmailSender`/`ISmsSender`) — no real provider is wired up yet.
- **AI support** uses a free, keyless `ISupportAssistant` (rule-based over live data); swap in Claude or another LLM later by changing one DI registration.

## Tests
```bash
dotnet test
```

## Project status
MVP complete (catalog → cart → checkout → payment → fulfillment), plus admin management, profiles, community chat, AI support, and dark mode. Phase-2 items (dedicated rider role, promo codes, reviews, subscriptions, scheduled delivery slots, real payment/email/SMS providers) are not yet implemented.
