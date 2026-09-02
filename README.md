# MyFarmProduce

A farm-produce ordering and delivery web app built with **Spring Boot (Java 21)** and **Spring Data JPA / PostgreSQL**, using a layered Maven multi-module architecture. Customers browse produce, order and pay online, and track delivery; admins manage the catalog, orders, and users. It also includes a real-time community chat, an AI-style support assistant, and dark mode.

## Features

### Customer
- Browse produce grouped by category, keyword search, product details
- Session-backed cart (add / adjust / remove)
- Checkout with delivery details + flat delivery fee
- Online payment (simulated gateway) with callback **and** webhook confirmation; stock decrements on confirmed payment
- Order confirmation, tracking, history, and one-click reorder (adjusts for current stock/price)
- Profile with photo upload; phone/email are locked and changed via an admin-approved request
- Real-time **community chat** (WebSocket/STOMP)
- **AI help assistant** (free, keyless rule-based; answers order/delivery/payment/availability questions from live data)
- **Dark mode** toggle (persisted)
- Forced password change on first login for admin-created accounts

### Admin (separate account type, role-resolved)
- Inventory CRUD, restock, availability toggle, **product image upload**
- Order management: filter, status updates, cancel, refund (logged)
- User management: create/edit/delete customers, approve/reject profile-change requests
- View support tickets
- Admin profile with photo

## Architecture (layered, Maven multi-module)

| Module | Responsibility |
|---|---|
| `web` | Spring MVC web app (controllers, Thymeleaf views, WebSocket/STOMP chat, security, file storage) |
| `application` | Service interfaces + DTOs (`com.myfarmproduce.application`) |
| `domain` | JPA entities (encapsulated `Product` stock via `reduceStock`/`restock`), enums |
| `infrastructure` | Spring Data repositories, service implementations, Flyway migrations, seeding |
| `common` | Shared constants |

Each module is its own Maven artifact with its own `pom.xml`; dependencies point one way (`web` → `infrastructure`/`application` → `domain` → `common`), mirroring the original layered project-reference graph.

## Tech stack
- Spring Boot 3 (Java 21), Maven multi-module build
- Spring MVC + Thymeleaf, Bootstrap 5.3 (dark mode via `data-bs-theme`)
- Spring Data JPA (Hibernate) + PostgreSQL, Flyway migrations
- Spring Security (custom `AuthenticationProvider` resolving Admin vs Customer by role) + WebSocket/STOMP for chat
- Cookie/session authentication (PBKDF2 password hashing — no OAuth/full Identity provider)

## Getting started

### Prerequisites
- JDK 21
- Maven 3.9+
- PostgreSQL (local install, or via Docker: `docker run --name myfarmproduce-db -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres`)

### Run
```bash
git clone <your-repo-url>
cd MyFarmProduce
mvn -pl web -am spring-boot:run
```
Flyway applies the schema migration and a `CommandLineRunner` seeds data (categories, sample products, admin accounts) on startup. Browse to `http://localhost:8080`.

The datasource defaults to `jdbc:postgresql://localhost:5432/myfarmproduce` / `postgres` / `postgres` (`spring.datasource.*` in `web/src/main/resources/application.properties`, overridable via `SPRING_DATASOURCE_URL`/`_USERNAME`/`_PASSWORD`). You can also set a single `ConnectionStrings__DefaultConnection` env var to a `postgres://user:pass@host:port/db` URI (the same name/format the app used before the Java rewrite) — it's parsed into the JDBC connection at startup and takes priority over the three separate vars.

## Deploying to Render
- Render has no native Java buildpack tuned for multi-module Maven, so the app ships as a Docker web service (see `Dockerfile` at repo root, which builds the whole reactor and runs the `web` module's jar).
- Use Render's managed PostgreSQL for the database; set `ConnectionStrings__DefaultConnection` on the web service to that database's connection string (see `render.yaml` for a ready-to-use Blueprint — it deploys only the web service and expects you to set that by hand, since Render's free tier allows just one free database per account). If you're carrying over the same env var from a previous deploy of this app, no dashboard changes are needed.
- Uploaded images (`uploads/`) are written to local disk, which is **ephemeral** on Render — they're wiped on every deploy/restart unless you attach a paid persistent disk mounted at that path. Fine for demoing the MVP; revisit before real usage.
- Chat works as-is on a single instance (in-memory STOMP broker); add an external broker relay (e.g. RabbitMQ) only if you later scale to multiple instances.

### Default accounts
- **Admin:** `admin@myfarmproduce.local` / `Admin@123`
- **Admin:** `chibuezegeoffrey@gmail.com` / `Admin@123`
- Customers self-register, or an admin creates them with the default password `Password@1234` (changed on first login).

Log in via the single `/account/login` form — admins are resolved by role automatically.

## Notable design decisions
- **Admins are a separate table** from customers and are created only in the backend (seed data); there is no admin sign-up UI.
- **Payment** is behind a `PaymentGateway` abstraction with a dev/simulated implementation; swap in Paystack/Flutterwave later.
- **Email/SMS** use logging stubs (`EmailSender`/`SmsSender`) — no real provider is wired up yet.
- **AI support** uses a free, keyless `SupportAssistant` (rule-based over live data); swap in Claude or another LLM later by changing one Spring bean.

## Tests
```bash
mvn test
```

## Project status
MVP complete (catalog → cart → checkout → payment → fulfillment), plus admin management, profiles, community chat, AI support, and dark mode. Phase-2 items (dedicated rider role, promo codes, reviews, subscriptions, scheduled delivery slots, real payment/email/SMS providers) are not yet implemented.
