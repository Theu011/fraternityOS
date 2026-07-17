# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

The MVP is **built and working end-to-end**: a Spring Boot API in `server/` and a React SPA in `frontend/`. Flyway migrations `V1` (full schema) and `V2` (join requests) are in place, all MVP bounded contexts are implemented (auth, house/members, announcements, calendar, responsibilities/chores, finance), there is a backend test suite (Mockito unit + Testcontainers integration/repository), and Docker + GitHub Actions CI. The SPA's UI copy is **Portuguese (BR)**. `project-scope.md` is the source of truth for the product, roles, and data model — keep it in sync with any schema or scope change. A few pieces remain *(planned)* and are marked as such below — most notably the `@Scheduled` rotation worker.

> **Note:** the backend module directory is `server/` and the base package is `com.fraternityos.server`.

**FraternityOS** (working title; repo dir `trackmycareer`) is a web platform for managing fraternity house operations — members, roles, announcements, a shared calendar, recurring chores, and monthly rent statements — replacing the WhatsApp + spreadsheets workflow.

## Tech Stack

**Backend**
- Spring Boot 3 · Java 21 · Spring Data JPA
- PostgreSQL · Maven build
- Flyway migrations (plain SQL) via `flyway-core` + `flyway-database-postgresql` — Hibernate `ddl-auto` set to `validate`, Flyway owns the schema. Flyway runs on startup; **the app won't boot until an initial migration exists** (`validate` fails against an empty DB).
- Spring Security + JWT for authn/authz (`@PreAuthorize`)
- `@Scheduled` background worker for chore rotation + overdue transitions *(planned — not yet implemented; today only FIXED chores exist and OVERDUE is derived on read, see below)*
- File uploads on local disk behind a `FileStorageService` interface (relative key in `attachment_url`), swappable for S3 post-MVP
- Tests: JUnit 5 + Mockito (unit) and Testcontainers-backed PostgreSQL (`@SpringBootTest` integration + `@DataJpaTest` repository). `./mvnw test` — **needs Docker running** for the integration/repository tests.
- Containerization + CI: multi-stage `server/Dockerfile`, nginx `frontend/Dockerfile`, full-stack `docker-compose.yml`, GitHub Actions (`.github/workflows/ci.yml`: backend tests, frontend lint+build, image builds).

**Frontend**
- React + Vite · React Router · TanStack Query
- Tailwind CSS v4 (CSS-variables / `@theme` mode) · Shadcn/UI
- UI copy is **Portuguese (BR)**; "house"/"fraternity" is rendered as *"república"* (product name "FraternityOS" stays in English). Seeded `POSITION` catalog names stay in English because authorization maps them by name.

## Commands

**Backend (Maven, from `server/`)**
- `./mvnw spring-boot:run` — run the API (Flyway migrates on startup)
- `./mvnw test` — full test suite (**needs Docker** for the Testcontainers tests)
- `./mvnw test -Dtest=ClassName#method` — single test
- `./mvnw clean package` — build the jar
- New migration: add `server/src/main/resources/db/migration/V<n>__description.sql`

**Frontend (npm, from `frontend/`)**
- `npm run dev` — Vite dev server (proxies `/api` → `http://localhost:8080`)
- `npm run build` — production build (`tsc -b && vite build`)
- `npm run lint` — lint (oxlint)
- Add a Shadcn component: `npx shadcn@latest add <component>`
- *(No frontend test runner is set up yet.)*

**Full stack (Docker, from repo root)**
- `docker compose up --build` — Postgres + backend (:8080) + frontend (:5173, nginx serving the SPA and proxying `/api` → backend). Override `JWT_SECRET` via a `.env` file (see `.env.example`).

## Architecture

Two-tier: a Spring Boot REST API (`backend/`) and a separate React SPA (`frontend/`) — no SSR, no shared server. The SPA calls the API over REST with a JWT bearer token.

- **Position-derived authorization.** There is no `role` column. Authorization derives from the positions a `MEMBERSHIP` holds: the JWT carries position names and `JwtAuthenticationFilter` maps `President → ROLE_PRESIDENT`, `Treasurer → ROLE_TREASURER`, and any membership → `ROLE_RESIDENT` (see `auth/security/PositionAuthorities`). Endpoints stay gated with `@PreAuthorize("hasRole(...)")`. Other positions (Kitchen Manager, Social Director, …) are cosmetic and grant nothing.
- **Multi-tenancy.** Every domain row belongs to a `HOUSE` and references a `MEMBERSHIP`. All queries must be scoped by `house_id`; a member of one house must never read or mutate another house's data. Derive the house/membership from the authenticated principal (`AuthenticatedMember`), not from client input.
- **Scheduled worker (`@Scheduled`).** *(planned — not yet built.)* Intended to generate the next `RESPONSIBILITY_ASSIGNMENT` for `ROTATING` responsibilities (walking `RESPONSIBILITY_MEMBER.rotation_order` via `RESPONSIBILITY.rotation_cursor`) and flip assignments `PENDING → OVERDUE` once `due_date` passes. **Current state:** no `@Scheduled` bean exists; only FIXED chores are implemented (`ChoreService`) and OVERDUE is *derived on read* (`ChoreService.effectiveStatus`), never persisted. The `ROTATING` type, `rotation_days`, `rotation_cursor`, and `responsibility_member` are unused scaffolding for this future work.
- **File storage.** Rent-statement PDFs/images go to local disk via `FileStorageService`; persist a *relative* key in `attachment_url`, never an absolute path. `LocalDiskFileStorageService` confines keys to the storage root (path-traversal guard) and allowlists PDF/PNG/JPEG.
- **Onboarding.** Registering creates a `USER` **only** — no house. A house-less user can log in (null membership/house, no positions) and is routed to onboarding, where they either **create a house** (seeds an `ACTIVE` `MEMBERSHIP` with the `President` position and returns a fresh token) or **request to join** an existing house (`JOIN_REQUEST`, status `PENDING`). A President approves the request, which creates the requester's `ACTIVE` membership and auto-rejects their other pending requests. A partial unique index prevents duplicate pending requests.
- **Member lifecycle.** `MemberService`/`HouseService` manage members: assign/remove catalog positions, change status (`ACTIVE`/`ALUMNI`), approve/reject join requests. Two invariants: (1) the house must always keep **at least one active President** — retiring the last one, or removing `President` from them, returns **409**; (2) **"removing" a member is a soft delete** — `DELETE /members/{id}` retires them to `ALUMNI` (history retained), it does not hard-delete, because other rows reference the membership with no cascade.
- **Notifications are out of MVP** — the app is pull-only for v1.

## Coding conventions

Write code following **SOLID** principles. The light-DDD layering already pushes you toward them — keep it that way:

- **Single Responsibility.** One reason to change per class. Controllers only handle HTTP (parse/validate request, call a use-case service, map the response); business logic lives in `application` services; persistence lives in `infrastructure`. Don't put queries or business rules in controllers, or HTTP concerns in the domain.
- **Open/Closed.** Extend behavior by adding types, not by editing existing ones. Prefer polymorphism/strategy over growing `if`/`switch` on an enum (e.g. per-`RESPONSIBILITY.type` rotation logic behind an interface rather than a branch that every new type must touch).
- **Liskov Substitution.** Any implementation of an interface must be safely usable through that interface — no surprising exceptions or weakened guarantees. A subtype must honor the base contract.
- **Interface Segregation.** Keep interfaces small and role-specific. Split repositories/services by use-case rather than one fat interface clients only partly use; a consumer shouldn't depend on methods it never calls.
- **Dependency Inversion.** Depend on abstractions, not concretions. `domain`/`application` code depends on interfaces (e.g. `FileStorageService`, repository interfaces); concrete adapters (local-disk storage, JPA repos) live in `infrastructure` and are injected. This keeps the S3 swap and DB details out of the domain — dependencies point inward.

Favor constructor injection (not field injection) so dependencies are explicit and classes stay testable. Prefer composition over inheritance. Don't over-engineer: apply these to earn flexibility where the design needs it, not by adding interfaces and indirection for their own sake.

## Project structure (light DDD)

The backend uses **light DDD**: package-by-bounded-context, each context split into `domain` (entities, value objects, domain services, repository interfaces), `application` (use-case services, DTOs, orchestration/transactions), `infrastructure` (JPA repository impls, external adapters like storage), and `api` (REST controllers). Domain depends on nothing outward; dependencies point inward.

```
server/src/main/java/com/fraternityos/server/
  house/            # House, membership, positions, onboarding, join requests, member mgmt
    domain/
    application/
    infrastructure/
    api/
  announcement/
  calendar/         # events
  responsibility/   # responsibilities + assignments + ChoreService (rotation pool/worker planned)
  finance/          # monthly statements, payments, FileStorageService
  auth/             # JWT, Spring Security config, login/signup, security principal
  shared/           # cross-cutting: error handling (GlobalExceptionHandler), config
server/src/main/resources/
  db/migration/     # Flyway V<n>__*.sql (V1 schema, V2 join_request)
  application.yml
server/src/test/java/...        # Mockito unit tests, Testcontainers ITs, support/AbstractIntegrationTest

frontend/src/
  features/         # feature folders mirroring backend contexts (auth, house, members,
                    #   announcements, calendar, chores + responsibilities, finance);
                    #   each has its own api.ts (typed client + TanStack Query hooks)
  components/ui/    # Shadcn components
  components/       # shared app components + layout
  routes/           # React Router nav definitions
  lib/              # api client, auth/token handling, theme, utils
```

Keep frontend `features/*` aligned with backend bounded contexts so the two stay navigable together.

## Data model

Full field lists live in `project-scope.md` (`# Data`). Aggregates and relationships:

- **USER** (table `app_user`) — identity: `name`, `email` (globally unique), `password_hash`, `phone`. Exists independently of any house.
- **HOUSE** — tenant root. Everything else is scoped to a house.
- **MEMBERSHIP** — links one `USER` to one `HOUSE` (`UNIQUE (user_id)` = at most one house per user); `status` (ACTIVE/ALUMNI), `room`, `joined_at`, `graduated_at`. The tenancy anchor every domain row references.
- **POSITION** / **MEMBERSHIP_POSITION** — `POSITION` is a **global** seeded catalog (President, Treasurer, Kitchen Manager, Social Director, Maintenance Director); assigned to memberships many-to-many. President/Treasurer grant permissions (see authorization above); the rest are cosmetic.
- **JOIN_REQUEST** (migration `V2`) — a house-less user's request to join a house: `house_id`, `user_id`, `status` (PENDING/APPROVED/REJECTED), `created_at`, `decided_at`. A partial unique index on `(user_id, house_id) WHERE status = 'PENDING'` prevents duplicate pending requests.
- **ANNOUNCEMENT** — `author_membership_id`, `is_pinned`.
- **EVENT** — calendar entries with `start_date`/`end_date`, `created_by_membership_id`.
- **RESPONSIBILITY** — `type` (FIXED/ROTATING), `rotation_days`, `rotation_cursor`.
- **RESPONSIBILITY_MEMBER** — eligible pool (`membership_id`) + `rotation_order` per rotating responsibility (drives the scheduler).
- **RESPONSIBILITY_ASSIGNMENT** — `membership_id`, `status` (PENDING/COMPLETED/OVERDUE), `assigned_date`, `due_date`, `completed_at`.
- **MONTHLY_STATEMENT** — `month`, `year`, `attachment_url` (relative key), `uploaded_by_membership_id`.
- **PAYMENT** — per membership per statement; `amount`, `status` (PENDING/PAID), `paid_at`.

## Unresolved — confirm with the user before deciding

Tracked in `project-scope.md` "Open Questions": payment flow (self-report vs. treasurer-set; possible `OVERDUE`/`marked_by`), whether a member can hold multiple system roles, the no-active-President edge case, and timezone handling. Don't silently pick an answer while coding.

*(Resolved this project: member "removal" is a soft delete — retire to `ALUMNI` — enforcing the last-active-President invariant; register creates an account only, house comes from onboarding. Still open: soft- vs hard-delete for the other entities — announcements, events, responsibilities, statements — which currently hard-delete.)*
