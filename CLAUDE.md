# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project status

This repository is **early scaffolding**. `project-scope.md` is the source of truth for the product, roles, and data model — keep it in sync with any schema or scope change. The Spring Boot backend lives in `server/` (Maven module bootstrapped, PostgreSQL datasource + Flyway wired in); no migrations or domain code exist yet. Sections below marked *(planned)* describe the intended structure to build toward.

> **Note:** the backend module directory is `server/`, not `backend/`. Some *(planned)* sections below still say `backend/` — read those as `server/`.

**FraternityOS** (working title; repo dir `trackmycareer`) is a web platform for managing fraternity house operations — members, roles, announcements, a shared calendar, recurring chores, and monthly rent statements — replacing the WhatsApp + spreadsheets workflow.

## Tech Stack

**Backend**
- Spring Boot 3 · Java 21 · Spring Data JPA
- PostgreSQL · Maven build
- Flyway migrations (plain SQL) via `flyway-core` + `flyway-database-postgresql` — Hibernate `ddl-auto` set to `validate`, Flyway owns the schema. Flyway runs on startup; **the app won't boot until an initial migration exists** (`validate` fails against an empty DB).
- Spring Security + JWT for authn/authz (`@PreAuthorize`)
- `@Scheduled` background worker for chore rotation + overdue transitions
- File uploads on local disk behind a `FileStorageService` interface (relative key in `attachment_url`), swappable for S3 post-MVP

**Frontend**
- React + Vite · React Router · TanStack Query
- Tailwind CSS v4 (CSS-variables / `@theme` mode) · Shadcn/UI

## Commands *(planned — replace with real scripts after scaffolding)*

**Backend (Maven, from `server/`)**
- `./mvnw spring-boot:run` — run the API (Flyway migrates on startup)
- `./mvnw test` — full test suite
- `./mvnw test -Dtest=ClassName#method` — single test
- `./mvnw clean package` — build the jar
- New migration: add `server/src/main/resources/db/migration/V<n>__description.sql`

**Frontend (npm, from `frontend/`)**
- `npm run dev` — Vite dev server
- `npm run build` — production build
- `npm run lint` — lint
- `npm test` / `npx vitest run path/to/file.test.tsx` — tests / single test
- Add a Shadcn component: `npx shadcn@latest add <component>`

## Architecture

Two-tier: a Spring Boot REST API (`backend/`) and a separate React SPA (`frontend/`) — no SSR, no shared server. The SPA calls the API over REST with a JWT bearer token.

- **Fixed-role authorization.** Authorization reads a single system role on `HOUSE_MEMBER.role` (`RESIDENT` / `PRESIDENT` / `TREASURER`), enforced with `@PreAuthorize`. `POSITION` (Kitchen Manager, Social Director, …) is a **cosmetic title with no permissions** — never gate access on it.
- **Multi-tenancy.** Every domain row belongs to a `HOUSE`. All queries must be scoped by `house_id`; a member of one house must never read or mutate another house's data. Derive the house from the authenticated principal, not from client input.
- **Scheduled worker (`@Scheduled`).** Generates the next `RESPONSIBILITY_ASSIGNMENT` for `ROTATING` responsibilities (walking `RESPONSIBILITY_MEMBER.rotation_order` via `RESPONSIBILITY.rotation_cursor`) and flips assignments `PENDING → OVERDUE` once `due_date` passes.
- **File storage.** Rent-statement PDFs/images go to local disk via `FileStorageService`; persist a *relative* key in `attachment_url`, never an absolute path.
- **Onboarding.** Creating a house auto-assigns the creator `role = PRESIDENT`, `status = ACTIVE`. Everyone else joins as `status = PENDING` and must be approved by a President before becoming `ACTIVE`.
- **Notifications are out of MVP** — the app is pull-only for v1.

## Project structure *(planned — light DDD)*

The backend uses **light DDD**: package-by-bounded-context, each context split into `domain` (entities, value objects, domain services, repository interfaces), `application` (use-case services, DTOs, orchestration/transactions), `infrastructure` (JPA repository impls, external adapters like storage), and `api` (REST controllers). Domain depends on nothing outward; dependencies point inward.

```
backend/src/main/java/com/fraternityos/
  house/            # House aggregate + membership, roles, positions, onboarding/approval
    domain/
    application/
    infrastructure/
    api/
  announcement/
  calendar/         # events
  responsibility/   # responsibilities, rotation pool, assignments, the @Scheduled rotation worker
  finance/          # monthly statements, payments, FileStorageService
  auth/             # JWT, Spring Security config, login/signup
  shared/           # cross-cutting: base entities, error handling, config, house-scoping utilities
backend/src/main/resources/
  db/migration/     # Flyway V<n>__*.sql
  application.yml

frontend/src/
  api/              # typed API client + TanStack Query hooks (one module per context)
  features/         # feature folders mirroring backend contexts (members, announcements, calendar, chores, finance, auth)
  components/ui/    # Shadcn components
  components/       # shared app components
  routes/           # React Router route definitions
  lib/              # utilities, auth/token handling
```

Keep frontend `features/*` aligned with backend bounded contexts so the two stay navigable together.

## Data model

Full field lists live in `project-scope.md` (`# Data`). Aggregates and relationships:

- **HOUSE** — tenant root. Everything else is scoped to a house.
- **HOUSE_MEMBER** — `house_id`, `email`, `password_hash`, `role` (RESIDENT/PRESIDENT/TREASURER), `status` (PENDING/ACTIVE/ALUMNI). Creator seeded PRESIDENT+ACTIVE; joiners start PENDING.
- **POSITION** / **HOUSE_MEMBER_POSITION** — cosmetic per-house titles; many-to-many with members. No auth meaning.
- **ANNOUNCEMENT** — `author_id`, `is_pinned`.
- **EVENT** — calendar entries with `start_date`/`end_date`, `created_by`.
- **RESPONSIBILITY** — `type` (FIXED/ROTATING), `rotation_days`, `rotation_cursor`.
- **RESPONSIBILITY_MEMBER** — eligible pool + `rotation_order` per rotating responsibility (drives the scheduler).
- **RESPONSIBILITY_ASSIGNMENT** — `status` (PENDING/COMPLETED/OVERDUE), `assigned_date`, `due_date`, `completed_at`.
- **MONTHLY_STATEMENT** — `month`, `year`, `attachment_url` (relative key), `uploaded_by`.
- **PAYMENT** — per member per statement; `amount`, `status` (PENDING/PAID), `paid_at`.

## Unresolved — confirm with the user before deciding

Tracked in `project-scope.md` "Open Questions": payment flow (self-report vs. treasurer-set; possible `OVERDUE`/`marked_by`), whether a member can hold multiple system roles, the no-active-President edge case, timezone handling, and soft- vs hard-delete. Don't silently pick an answer while coding.
