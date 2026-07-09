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
* **`@Scheduled`** background worker for chore rotation generation and payment/chore overdue transitions
* File uploads stored on **local disk** behind a `FileStorageService` interface (relative key persisted in `attachment_url`), so a move to S3-compatible object storage post-MVP is a single-class change.

## Frontend
* **React** + **Vite**
* **React Router** for routing
* **TanStack Query** (React Query) for data fetching / caching
* **Tailwind CSS v4** (CSS-variables / `@theme` mode)
* **Shadcn/UI** components

---

# Roles & Authorization

Authorization is driven by a **fixed system role** stored on each member (`HOUSE_MEMBER.role`):

| Role | Capabilities |
|------|--------------|
| **RESIDENT** | View announcements, calendar, assigned responsibilities, and monthly rent statement. Mark assigned chores as completed. |
| **PRESIDENT** | Everything a Resident can do, plus: manage announcements, manage the house calendar, assign responsibilities, manage members and roles, approve join requests. |
| **TREASURER** | Everything a Resident can do, plus: upload monthly rent statements, track payment status, manage financial information. |

**`POSITION` is cosmetic only** — titles such as Kitchen Manager or Social Director that appear on profiles but grant **no** permissions. All access-control decisions read `HOUSE_MEMBER.role`.

*Open question (MVP): whether a single member may hold multiple system roles (e.g. President **and** Treasurer). Current model assumes one role per member.*

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
* Self-signup with President approval
* Secure login
* JWT authentication
* Role-based authorization

## Member Management
* View house members
* Assign predefined roles (system) and cosmetic positions
* Approve pending join requests
* Resident profiles

## Announcements
* Create announcements
* Pin important announcements
* View house news

## Calendar
* Shared house calendar
* Role-restricted event editing
* View upcoming events

## Responsibilities
* Fixed responsibilities
* Rotating chores (auto-rotated by scheduled job)
* Chore completion tracking

## Finance
* Upload monthly rent statement (PDF/Image)
* View payment breakdown
* Track payment status

## Background Jobs (scheduled)
* Generate the next assignment for each `ROTATING` responsibility based on `rotation_days` and the rotation order.
* Flip chore assignments from `PENDING` to `OVERDUE` once `due_date` passes.

---

# Data

## HOUSE
* id
* name
* created_at
* updated_at

---

## HOUSE_MEMBER
* id
* house_id (FK → HOUSE)
* name
* email
* password_hash
* phone
* room
* role (RESIDENT, PRESIDENT, TREASURER)
* status (PENDING, ACTIVE, ALUMNI)
* joined_at
* graduated_at (nullable)
* created_at
* updated_at

*`status = PENDING` means the join request awaits President approval. The house creator is seeded with `role = PRESIDENT`, `status = ACTIVE`.*

---

## POSITION

* id
* house_id (FK → HOUSE)
* name
* description
* created_at

Examples (cosmetic titles only — no permissions):

* Kitchen Manager
* Social Director
* Maintenance Director

*Scoped per house so each house can define its own titles.*

---

## HOUSE_MEMBER_POSITION

* house_member_id (FK → HOUSE_MEMBER)
* position_id (FK → POSITION)
* assigned_at

*A house member can have multiple positions.*

---

## ANNOUNCEMENT

* id
* house_id (FK → HOUSE)
* author_id (FK → HOUSE_MEMBER)
* title
* content
* is_pinned
* created_at
* updated_at

---

## EVENT

* id
* house_id (FK → HOUSE)
* created_by (FK → HOUSE_MEMBER)
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
* house_member_id (FK → HOUSE_MEMBER)
* rotation_order (int) — position of this member in the rotation

*Defines the eligible pool and ordering for `ROTATING` responsibilities. The scheduled job walks this list to pick the next assignee.*

---

## RESPONSIBILITY_ASSIGNMENT

* id
* responsibility_id (FK → RESPONSIBILITY)
* house_member_id (FK → HOUSE_MEMBER)
* status (PENDING, COMPLETED, OVERDUE)
* assigned_date
* due_date
* completed_at (nullable)

---

## MONTHLY_STATEMENT

* id
* house_id (FK → HOUSE)
* uploaded_by (FK → HOUSE_MEMBER)
* month
* year
* notes
* attachment_url (relative storage key)
* created_at

---

## PAYMENT

* id
* monthly_statement_id (FK → MONTHLY_STATEMENT)
* house_member_id (FK → HOUSE_MEMBER)
* amount
* status (PENDING, PAID)
* paid_at (nullable)
* created_at

*Open question: payment flow — does the resident self-report payment (treasurer confirms) or does the treasurer set status directly? May also need an `OVERDUE` status and a `marked_by` field. See Open Questions.*

---

# Post-MVP / Known Risks

* **Notifications are out of MVP.** The app is pull-only for v1 (users must open it to see updates). This is the primary adoption risk — the product competes with the habit of checking WhatsApp. Email / push / in-app notifications are the top v2 candidate.
* **File storage** is local disk for MVP; migrate to S3-compatible object storage for production.
* **JWT refresh strategy** — MVP may ship a single short-lived token; plan refresh tokens for v2.

---

# Open Questions

* **Payment flow:** self-report + treasurer confirmation vs. treasurer sets directly. Add `OVERDUE` status and a `marked_by` field?
* **Multi-role members:** can one member be both President and Treasurer?
* **No active President edge case:** if all Presidents graduate/leave, who approves join requests and manages the house?
* **Timezone handling** for event times and chore due dates.
* **Soft-delete vs. hard-delete** for announcements, events, and responsibilities.
