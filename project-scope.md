# Project Name

**FraternityOS** (working title)

A web platform that helps fraternity houses manage their daily operations by centralizing responsibilities, communication, finances, and member information.

---

# Problem

Fraternity houses typically rely on multiple disconnected tools—such as WhatsApp, Google Sheets, Splitwise, and verbal communication—to organize daily activities. As the number of residents grows, it becomes increasingly difficult to track responsibilities, coordinate events, manage monthly payments, and communicate effectively.

This scattered workflow often results in missed chores, forgotten payments, duplicated information, and poor visibility into the overall operation of the house.

---

# Solution

FraternityOS provides a centralized platform where fraternity members can manage the day-to-day operations of the house.

The application combines member management, role-based permissions, announcements, shared calendars, recurring responsibilities, and monthly financial statements into a single system. Instead of switching between multiple applications, members can access everything from one dashboard.

The MVP focuses on delivering a simple, intuitive experience that replaces the most common spreadsheets and WhatsApp conversations used to manage a fraternity.

---

# Tech Stack

## Backend
* **Spring Boot 3** · **Java 21**
* **Spring Data JPA** (ORM)
* **PostgreSQL** database
* **Maven** build tool
* **Flyway** for database migrations (plain SQL). Hibernate `ddl-auto` set to `validate` so Flyway owns the schema.
* **Spring Security + JWT** for authentication and role-based authorization (`@PreAuthorize`)
* **`@Scheduled`** background worker for chore rotation generation and chore overdue transitions — *(planned, not yet built; only FIXED chores exist and OVERDUE is derived on read)*
* File uploads stored on **local disk** behind a `FileStorageService` interface (relative key persisted in `attachment_url`), so a move to S3-compatible object storage post-MVP is a single-class change.
* **Testing:** JUnit 5 + Mockito (unit) and **Testcontainers**-backed PostgreSQL (`@SpringBootTest` integration + `@DataJpaTest` repository).
* **Containerization + CI:** Docker (multi-stage backend image; nginx frontend image; full-stack `docker-compose.yml`) and **GitHub Actions** (build, tests, lint, image builds).

## Frontend
* **React** + **Vite**
* **React Router** for routing
* **TanStack Query** (React Query) for data fetching / caching
* **Tailwind CSS v4** (CSS-variables / `@theme` mode)
* **Shadcn/UI** components
* **UI language: Portuguese (BR)** — "house"/"fraternity" is shown as *"república"*; the product name "FraternityOS" stays in English.

---

# Roles & Authorization

Authorization is **derived from the positions** a membership holds (`MEMBERSHIP` → `MEMBERSHIP_POSITION` → `POSITION`). The `President` and `Treasurer` positions are permission-granting; every active membership has a baseline resident capability:

| Effective role | Source position | Capabilities |
|------|--------------|--------------|
| **RESIDENT** (baseline) | any active membership | View announcements, calendar, assigned responsibilities, and monthly rent statement. Mark assigned chores as completed. |
| **PRESIDENT** | `President` position | Everything a Resident can do, plus: manage announcements, manage the house calendar, assign responsibilities, manage members and positions. |
| **TREASURER** | `Treasurer` position | Everything a Resident can do, plus: upload monthly rent statements, track payment status, manage financial information. |

At the security layer the JWT carries the membership's position names; the auth filter maps `President → ROLE_PRESIDENT`, `Treasurer → ROLE_TREASURER`, and any membership → `ROLE_RESIDENT`, so existing `@PreAuthorize("hasRole(...)")` checks are satisfied from positions. Other positions (Kitchen Manager, Social Director, …) are cosmetic and grant no permissions.

*A membership may hold multiple positions (e.g. President **and** Treasurer), so a member can carry more than one permission-granting role at once.*

---

# User Flows

### Onboarding
* A user signs up and either **creates a new house** or **requests to join** an existing one.
* The member who **creates a house is automatically assigned the `PRESIDENT` role**.
* Join requests are created with status `PENDING` and must be **approved by a President** before the member becomes `ACTIVE`.

### Resident
* View announcements
* View calendar
* View assigned responsibilities
* View monthly rent statement
* Mark assigned chores as completed

### President
* Manage announcements
* Manage the house calendar
* Assign responsibilities
* Manage members and roles
* Approve / reject join requests

### Treasurer
* Upload monthly rent statements
* Track payment status
* Manage financial information

---

# Features

## Authentication
* Self-signup (creates an account only — no house)
* Secure login (email + password); a user can log in without belonging to a house
* JWT authentication
* Position-derived authorization

## Onboarding
* Create a house (creator becomes President) **or** request to join an existing house
* Presidents review pending join requests and approve/reject them

## Member Management
* View active members and alumni; search by name; view profiles and positions
* Assign / remove catalog positions (President, Treasurer, …) — positions loaded from the DB, never hardcoded
* Change member status (ACTIVE ⇄ ALUMNI); "remove" retires a member (soft delete)
* Approve / reject pending join requests
* Guardrail: the house must always keep at least one active President

## Announcements
* Create announcements
* Pin important announcements
* View house news

## Calendar
* Shared house calendar
* Role-restricted event editing
* View upcoming events

## Responsibilities
* Fixed chores (assignee + due date), shown on a Pending / Late / Completed board
* Chore completion tracking; assignee or President may complete
* OVERDUE is **derived on read** once `due_date` passes (no scheduler needed)
* Rotating chores auto-rotated by a scheduled job — *(planned, not yet built)*

## Finance
* Upload monthly rent statement (PDF/Image)
* View payment breakdown
* Track payment status

## Background Jobs (scheduled) — *(planned, not yet built)*
* Generate the next assignment for each `ROTATING` responsibility based on `rotation_days` and the rotation order.
* Persist `PENDING → OVERDUE` transitions once `due_date` passes (today OVERDUE is derived on read instead).

---

# Data

## HOUSE
* id
* name
* created_at
* updated_at

---

## USER  (table `app_user` — `user` is reserved in Postgres)
* id
* name
* email (globally unique)
* password_hash
* phone
* created_at
* updated_at

*A user's account exists independently of any house. A user can register and log in without belonging to a house. `email` is globally unique; login is email + password alone.*

---

## MEMBERSHIP
* id
* user_id (FK → USER)
* house_id (FK → HOUSE)
* status (ACTIVE, ALUMNI)
* room
* joined_at
* graduated_at (nullable)
* created_at
* updated_at

*Links one user to the single house they belong to — a user has **at most one** membership (`UNIQUE (user_id)`). Every house-scoped row references a `MEMBERSHIP`, so a person's house history survives after they go ALUMNI. The house creator is seeded ACTIVE and assigned the `President` position. "Removing" a member is a **soft delete** — status is set to `ALUMNI` (`graduated_at` recorded), the row is retained; there is no hard delete of memberships.*

---

## POSITION

* id
* name (globally unique)
* description
* created_at

A **global catalog**, seeded: President, Treasurer, Kitchen Manager, Social Director, Maintenance Director. `President`/`Treasurer` grant permissions (see Roles & Authorization); the rest are cosmetic.

---

## MEMBERSHIP_POSITION

* membership_id (FK → MEMBERSHIP)
* position_id (FK → POSITION)
* assigned_at

*A membership can hold multiple positions.*

---

## JOIN_REQUEST  (migration `V2`)

* id
* house_id (FK → HOUSE)
* user_id (FK → USER)
* status (PENDING, APPROVED, REJECTED)
* created_at
* decided_at (nullable)

*A house-less user's request to join a house. A partial unique index on `(user_id, house_id) WHERE status = 'PENDING'` prevents duplicate pending requests. On approval a President creates the user's ACTIVE `MEMBERSHIP` and the user's other pending requests are auto-rejected.*

---

## ANNOUNCEMENT

* id
* house_id (FK → HOUSE)
* author_membership_id (FK → MEMBERSHIP)
* title
* content
* is_pinned
* created_at
* updated_at

---

## EVENT

* id
* house_id (FK → HOUSE)
* created_by_membership_id (FK → MEMBERSHIP)
* title
* description
* location
* start_date
* end_date
* created_at
* updated_at

---

## RESPONSIBILITY

* id
* house_id (FK → HOUSE)
* title
* description
* type (FIXED, ROTATING)
* rotation_days (nullable)
* rotation_cursor (nullable) — index/pointer to the last-assigned member in the rotation pool
* active
* created_at
* updated_at

---

## RESPONSIBILITY_MEMBER

* responsibility_id (FK → RESPONSIBILITY)
* membership_id (FK → MEMBERSHIP)
* rotation_order (int) — position of this member in the rotation

*Defines the eligible pool and ordering for `ROTATING` responsibilities. The scheduled job walks this list to pick the next assignee.*

---

## RESPONSIBILITY_ASSIGNMENT

* id
* responsibility_id (FK → RESPONSIBILITY)
* membership_id (FK → MEMBERSHIP)
* status (PENDING, COMPLETED, OVERDUE)
* assigned_date
* due_date
* completed_at (nullable)

---

## MONTHLY_STATEMENT

* id
* house_id (FK → HOUSE)
* uploaded_by_membership_id (FK → MEMBERSHIP)
* month
* year
* notes
* attachment_url (relative storage key)
* created_at

---

## PAYMENT

* id
* monthly_statement_id (FK → MONTHLY_STATEMENT)
* membership_id (FK → MEMBERSHIP)
* amount
* status (PENDING, PAID)
* paid_at (nullable)
* created_at

*Open question: payment flow — does the resident self-report payment (treasurer confirms) or does the treasurer set status directly? May also need an `OVERDUE` status and a `marked_by` field. See Open Questions.*

---

# Post-MVP / Known Risks

* **Rotating chores + scheduled worker are not built yet.** Only FIXED chores exist; `type = ROTATING`, `rotation_days`, `rotation_cursor`, and `RESPONSIBILITY_MEMBER` are schema scaffolding for the planned `@Scheduled` rotation job. OVERDUE is currently derived on read rather than persisted by a job.
* **Notifications are out of MVP.** The app is pull-only for v1 (users must open it to see updates). This is the primary adoption risk — the product competes with the habit of checking WhatsApp. Email / push / in-app notifications are the top v2 candidate.
* **File storage** is local disk for MVP; migrate to S3-compatible object storage for production.
* **JWT** — single short-lived token (HS256, no refresh, no server-side revocation; logout is client-side). The signing secret comes from `JWT_SECRET`; a strong unique value **must** be set in production (the app warns when the built-in dev secret is used). Refresh tokens are a v2 candidate.

---

# Resolved Decisions

* **Email uniqueness — RESOLVED:** email is globally unique; login uses email + password. Enforced by a global `UNIQUE (email)` constraint in migration `V1` (`uq_app_user_email`).
* **Onboarding split — RESOLVED:** registering creates a `USER` account only (no house). The house is established afterward via onboarding — create a house (→ founding President) or request to join (→ President approval → ACTIVE membership).
* **Member removal — RESOLVED:** "removing" a member is a **soft delete** (retire to `ALUMNI`), never a hard delete, and it enforces the "at least one active President" invariant. (Other entities — announcements, events, responsibilities, statements — still hard-delete; see Open Questions.)
* **Multi-position members — RESOLVED:** a membership may hold multiple positions simultaneously (e.g. President and Treasurer), so a member can carry more than one permission-granting role.

---

# Open Questions

* **Payment flow:** self-report + treasurer confirmation vs. treasurer sets directly. Add `OVERDUE` status and a `marked_by` field?
* **No active President edge case:** if all Presidents graduate/leave, who approves join requests and manages the house? (The invariant blocks retiring the *last* active President, but not other ways a house could end up leaderless.)
* **Timezone handling** for event times and chore due dates.
* **Soft-delete vs. hard-delete** for announcements, events, responsibilities, and statements (memberships are already soft-deleted).
