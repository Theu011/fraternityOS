# Task 1 — Architecture

Don't create endpoints.

Create the structure.

```
backend

auth/

house/

member/

role/

announcement/

calendar/

chore/

finance/

shared/
```

Each module contains

```
controller

service

repository

entity

dto
```

Now create Flyway.

```
V1__initial_schema.sql
```

Create all tables.

No data yet.

---

# Task 2 — Authentication

This is the foundation.

Implement

- JWT
- Login
- Register
- Roles

Roles

```
President

Treasurer

Resident
```

At the end of Task 2

You should be able to

```
POST /login

↓

receive JWT

↓

call secured endpoint
```

Everything else depends on this.

---

# Task 3 — Members + Roles

Build

```
GET members

POST member

PUT member

DELETE member
```

Simple CRUD.

No fancy UI.

This unlocks everything.

---

# Task 4 — Dashboard

Now switch to React.

Build the shell.

Sidebar

```
Dashboard

Members

Calendar

Responsibilities

Finance

Announcements
```

Top navbar

Profile

Notifications

House

Dark mode (optional)

Now the app already looks real.

---

# Task 5 — Announcements

Backend

CRUD

Frontend

Card list

```
Water outage tomorrow

Party Friday

Meeting Sunday
```

President

Create announcement

Resident

View only

Boom.

RBAC demonstrated.

---

# Task 6 — Calendar

Simple month calendar.

Only

```
Title

Date

Description
```

President

Can edit.

Resident

Read only.

Done.

Don't build Google Calendar.

---

# Task 7 — Chores ⭐

This is your differentiator.

Create

Task

Assigned member

Status

```
Pending

Completed

Late
```

Nice Kanban/table UI.

Add

Complete button.

---

# Task 8 — Finance

Treasurer uploads

```
July Rent

PDF

Image
```

Residents

Open statement

Mark paid

Done.

Don't implement payment processing.

---

# Task 9 — Polish

Improve UI.

Use shadcn components.

Cards.

Tables.

Dialogs.

Loading.

Empty states.

This Task has huge ROI.

Translate all texts to portuguese br

---

# Task 10 — Tests

Don't aim for 100%.

Test

```
AuthService

AnnouncementService

ChoreService
```

Integration tests

Login endpoint

Announcements endpoint

One repository test

Enough.

---

# Task 11 — Docker + CI

Docker

GitHub Actions

```
Build

↓

Run tests

↓

Done
```


---

# Task 12 — README

This is surprisingly important.

Include:

## Screenshots

Dashboard

Calendar

Finance

Responsibilities

---

Architecture diagram

```
React

↓

Spring Boot

↓

PostgreSQL
```

---

Features

✅ JWT Authentication

✅ RBAC

✅ Calendar

✅ Chores

✅ Announcements

✅ Finance

---

Tech

React

TypeScript

Spring Boot

PostgreSQL

Docker

GitHub Actions

Flyway

OpenAPI

JUnit

Testcontainers



# Task — Refactor the Data Model for User & House Membership

## Goal

Refactor the current data model to support users existing independently of houses.

A user should be able to:

* Register an account.
* Log in without belonging to a house.
* Create a new house and automatically become its President.
* Request to join an existing house.
* Become a member of a house only after being approved by the President.

This change should happen **before** implementing the remaining features.

---

## Business Rules

### User

A user exists independently of any house.

A newly registered user:

* has an account
* can authenticate
* does **not** belong to any house

---

### House

A house contains many members.

A user can belong to **only one house**.

---

### Membership

Membership represents the relationship between a User and a House.

A membership contains information specific to that house, such as:

* status (ACTIVE or ALUMNI)
* joined date
* graduation date

If a user has no membership, they are not part of any house.

---

### Positions

A membership may have multiple positions.

Examples:

* President
* Treasurer
* Kitchen Manager
* Social Director

Positions are independent from membership status.

Examples:

John

Status:

ACTIVE

Positions:

* President
* Treasurer

Later

Status:

ALUMNI

Positions remain stored as historical information.

---

## Replace the Current Model

Remove the concept of `HOUSE_MEMBER`.

Replace it with:

```
USER
```

```
HOUSE
```

```
MEMBERSHIP
```

```
POSITION
```

```
MEMBERSHIP_POSITION
```

---

## New Database Model

### USER

* id
* name
* email (unique)
* password_hash
* phone
* created_at
* updated_at

---

### HOUSE

* id
* name
* created_at
* updated_at

---

### MEMBERSHIP

* id
* user_id (FK → USER)
* house_id (FK → HOUSE)
* status (ACTIVE, ALUMNI)
* room
* joined_at
* graduated_at (nullable)
* created_at
* updated_at

Constraints:

* A user can have at most one membership.
* A house can have many memberships.

---

### POSITION

* id
* name
* description
* created_at

Examples:

* President
* Treasurer
* Kitchen Manager
* Social Director
* Maintenance Director

---

### MEMBERSHIP_POSITION

* membership_id (FK → MEMBERSHIP)
* position_id (FK → POSITION)
* assigned_at

A membership can have multiple positions.

---

## Update Existing Tables

All tables that currently reference `HOUSE_MEMBER` must now reference `MEMBERSHIP`.

Update the following foreign keys:

### ANNOUNCEMENT

Replace

```
author_id → HOUSE_MEMBER
```

with

```
author_membership_id → MEMBERSHIP
```

---

### EVENT

Replace

```
created_by → HOUSE_MEMBER
```

with

```
created_by_membership_id → MEMBERSHIP
```

---

### RESPONSIBILITY_ASSIGNMENT

Replace

```
house_member_id
```

with

```
membership_id
```

---

### MONTHLY_STATEMENT

Replace

```
uploaded_by
```

with

```
uploaded_by_membership_id
```

---

### PAYMENT

Replace

```
house_member_id
```

with

```
membership_id
```

---

## Flyway Migration

Create a new Flyway migration that reflects the new schema.

The migration should:

* Create the new tables.
* Define all primary keys and foreign keys.
* Add appropriate indexes.
* Add a unique constraint to prevent a user from belonging to more than one house.
* Drop or replace obsolete references to `HOUSE_MEMBER`.

---

## Expected Outcome

After this refactor:

* Users can register without belonging to a house.
* Houses contain memberships instead of users directly.
* A membership links one user to one house.
* A membership can have multiple positions.
* Existing business modules (Announcements, Calendar, Responsibilities, Finance, etc.) reference memberships instead of house members.
* The new model is ready for future features such as join requests and house creation without requiring another database redesign.



# Task House — House Onboarding

After a user registers and logs in, they should only be able to access the dashboard if they belong to a house.

If the user has no membership, redirect them to the **House Onboarding** page.

The page should provide two options.

---

## Option 1 — Join an Existing House

Users can search for fraternities that have already been created.

Backend

Implement

```
GET /houses

GET /houses/search?name=

POST /houses/{id}/join-request
```

Requirements

- Search houses by name
- Display basic house information
- Allow the user to submit a join request
- Prevent duplicate join requests
- Prevent users that already belong to a house from submitting requests

Frontend

Create a page containing

```
Search bar

House cards

Join button
```

Each card should display

```
House name

Number of active members

Created date (optional)
```

---

## Option 2 — Create a House

Users can create their own fraternity.

Backend

Implement

```
POST /houses
```

Fields

```
House name
```

When a house is created

- Create the house
- Create the user's membership
- Assign the President position
- Redirect the user to the dashboard

Restrictions

- A user can only create one house.
- A user who already belongs to a house cannot create another.

---

## Success Criteria

A user without a house can:

✅ Search for existing houses

✅ Request to join a house

OR

✅ Create a new house

If a house is created, the creator automatically becomes its President.

If a join request is approved later, the user becomes an ACTIVE member of that house.



# Task X — Members Management

The Members page should allow every authenticated user to view the members of their house.

The available actions depend on the logged-in user's positions.

---

## Resident

Residents have read-only access.

They can:

- View active members
- View alumni
- View member profiles
- View each member's assigned positions
- Search members by name

Residents cannot:

- Edit members
- Assign positions
- Remove positions
- Change member status
- Manage join requests

---

## President

The President manages the house members.

In addition to everything Residents can do, the President can:

- Assign one or more positions to a member
- Remove positions from a member
- Change a member's status
    - ACTIVE
    - ALUMNI
- View pending join requests
- Approve join requests
- Reject join requests

The President cannot:

- Remove themselves as the only President of the house.

At least one President must always exist.

---

## Position Assignment

Positions must **not** be hardcoded.

The frontend should request the available positions from the backend.

Backend

```
GET /positions
```

Returns all available positions stored in the database.

Example response

```json
[
  {
    "id": 1,
    "name": "President"
  },
  {
    "id": 2,
    "name": "Treasurer"
  },
  {
    "id": 3,
    "name": "Kitchen Manager"
  },
  {
    "id": 4,
    "name": "Social Director"
  },
  {
    "id": 5,
    "name": "Maintenance Director"
  }
]
```

When assigning a position:

- The President selects one or more positions from the list returned by the API.
- The backend creates the corresponding records in `MEMBERSHIP_POSITION`.
- A member may have multiple positions simultaneously.
- Assigning an already assigned position should have no effect or return a validation error.

---

## Backend

Implement

```
GET /members

GET /members/{id}

GET /members/alumni

PATCH /members/{id}/status

GET /positions

POST /members/{id}/positions

DELETE /members/{id}/positions/{positionId}
```

Join Requests

```
GET /join-requests

POST /join-requests/{id}/approve

POST /join-requests/{id}/reject
```

---

## Frontend

Create a Members page with two tabs.

```
Members

Alumni
```

Each row should display

```
Name

Email

Room

Status

Positions
```

If the logged-in user is a President, display an **Actions** menu.

Actions

```
Assign Position

Remove Position

Change Status

View Profile
```

When **Assign Position** is clicked:

- Fetch the available positions from `GET /positions`.
- Display them in a multi-select dialog.
- Submit the selected positions to the backend.

Hide all management actions for non-Presidents.

---

## Success Criteria

Residents can:

✅ View members

✅ View alumni

✅ Search members

Presidents can:

✅ Assign one or more positions

✅ Remove positions

✅ Change member status

✅ Manage join requests

The list of available positions is always loaded from the database, never hardcoded in the frontend.


# Task X — Senior Engineering Review

Act as a Senior Software Engineer performing a production-quality code review.

Your goal is to evaluate the project architecture and implementation as if this were a Pull Request before merging into the main branch.

Do **not** rewrite the project unnecessarily.

Prefer incremental improvements over large refactors.

Only recommend changes that provide a clear benefit.

---

## Review Areas

Review the project against the following principles.

### Architecture

Evaluate whether the project follows a clean modular architecture.

Check:

- Separation of concerns
- Package organization
- Module boundaries
- Dependency direction
- Cohesion
- Coupling

---

### SOLID

Evaluate every module against the SOLID principles.

Specifically check:

- Single Responsibility Principle
- Open/Closed Principle
- Liskov Substitution Principle
- Interface Segregation Principle
- Dependency Inversion Principle

Identify violations and explain why.

---

### Domain Driven Design (DDD Lite)

This project intentionally follows a lightweight DDD approach.

Review:

- Aggregate boundaries
- Entity responsibilities
- Value Objects (where appropriate)
- Domain Services
- Application Services
- Repositories
- Ubiquitous Language

Ensure business rules live inside the domain instead of controllers.

---

### Design Patterns

Check whether common patterns are being applied appropriately.

Examples:

- Strategy
- Factory
- Builder
- Mapper
- Repository
- Service Layer

Do not introduce patterns unless they solve an actual problem.

Avoid overengineering.

---

### Spring Boot Best Practices

Review:

- Dependency Injection
- Constructor injection
- Transaction boundaries
- Validation
- Exception handling
- Configuration
- Security
- DTO usage
- Entity exposure
- Repository usage

---

### REST API

Review endpoints for:

- Naming
- HTTP verbs
- Status codes
- Request validation
- Response consistency
- Error handling

---

### Database

Review:

- Table design
- Relationships
- Constraints
- Cascade behavior
- Indexes
- Naming conventions
- Flyway migrations

---

### Security

Review:

- JWT implementation
- Password hashing
- Authorization
- Authentication flow
- Sensitive data exposure

---

### React

Review:

- Component organization
- Reusability
- State management
- Custom hooks
- API layer
- Routing
- Folder structure

---

### Testing

Review:

- Unit tests
- Integration tests
- Test readability
- Coverage of business rules

---

### Code Quality

Look for:

- Large classes
- Large methods
- Duplicate code
- Magic numbers
- Poor naming
- Dead code
- Code smells

---

## Output Format

Produce a report with the following sections.

### ✅ Strengths

Things that are well designed.

---

### ⚠️ Issues

For each issue include:

- Severity
    - Critical
    - High
    - Medium
    - Low

- Explanation

- Why it matters

- Recommended improvement

---

### 🏗 Architectural Suggestions

Suggest improvements that would make the project easier to maintain in the future.

Do not recommend unnecessary abstractions.

---

### 🚫 Overengineering

Identify places where the project is becoming more complicated than necessary.

Recommend simpler alternatives where appropriate.

---

### 📊 Overall Assessment

Provide scores (1–10) for:

- Architecture
- Code Quality
- Maintainability
- Scalability
- Security
- Testability
- Readability
- REST API Design

Finish with an overall evaluation explaining whether the project demonstrates production-ready software engineering practices suitable for a portfolio targeting Software Engineer or Data Engineer positions.


# Task X — Prepare the Project for Production

Act as a Senior DevOps Engineer.

Prepare the project for production deployment.

Do **not** deploy anything yet.

Your goal is to make the application production-ready while keeping the current development experience intact.

---

## Backend

Review the Spring Boot project.

Create or update the production configuration.

Create

```
application-prod.yml
```

Requirements

- Read all sensitive values from environment variables
- Keep local development unchanged
- Enable Flyway
- Configure production logging
- Use `ddl-auto=validate`
- Ensure no secrets are committed

Use environment variables for

```
SPRING_DATASOURCE_URL

SPRING_DATASOURCE_USERNAME

SPRING_DATASOURCE_PASSWORD

JWT_SECRET
```

---

## Frontend

Prepare the React application for production.

Requirements

- Use environment variables
- Replace hardcoded API URLs
- Configure

```
VITE_API_URL
```

Ensure the application works in both development and production.

---

## Docker

Review the Docker configuration.

Create or improve

```
Dockerfile

docker-compose.yml

.dockerignore
```

Optimize image size where possible.

---

## Environment Variables

Create

```
.env.example
```

Include every required variable without exposing secrets.

Document every variable.

---

## GitHub Actions

Review the CI workflow.

Ensure every push to `main`

```
Build

↓

Run Tests

↓

Package Application
```

Deployment should **not** happen yet.

---

## CORS

Prepare production CORS configuration.

Requirements

- Allow configuration through environment variables
- Do not allow all origins in production
- Keep local development working

---

## Flyway

Review database migrations.

Ensure migrations execute automatically in production.

---

## README

Update the README with

- Environment variables
- Local setup
- Production setup
- Docker usage
- CI pipeline

---

## Deliverables

When finished, provide

- Summary of changes
- Files created
- Files modified
- Remaining deployment steps

Do not deploy anything yet.

# Task X — Production Deployment Assistant

Act as a Senior DevOps Engineer.

Help me deploy this application to production.

We will deploy incrementally.

**Never perform multiple deployment steps at once.**

After each completed step, stop and wait for my confirmation before continuing.

Assume I have already created and connected accounts for:

- GitHub
- Neon
- Render
- Vercel

---

## Target Architecture

```
React (Vercel)

↓

Spring Boot (Render)

↓

PostgreSQL (Neon)
```

---

## Deployment Process

Guide me through the following steps one at a time.

### Step 1

Create the Neon PostgreSQL database.

Help me

- Create the project
- Configure the database
- Obtain the connection information
- Explain which values are secrets
- Explain where each value will be used

Stop and wait for confirmation.

---

### Step 2

Configure Render.

Help me

- Create the Web Service
- Connect the GitHub repository
- Configure Java
- Configure build/start commands
- Configure environment variables

Do **not** assume any values.

Tell me exactly where to obtain each value.

Stop and wait for confirmation.

---

### Step 3

Deploy the backend.

Verify

- Build succeeds
- Flyway executes
- Application starts
- Health endpoint works
- Swagger/OpenAPI is accessible

If something fails, help debug it before moving on.

Stop and wait for confirmation.

---

### Step 4

Deploy the React application to Vercel.

Help me

- Import the repository
- Configure environment variables
- Configure the production API URL

Verify the deployment.

Stop and wait for confirmation.

---

### Step 5

Configure CORS.

Allow only the deployed frontend domain.

Verify authentication works.

Stop and wait for confirmation.

---

### Step 6

Perform production verification.

Test

- Registration
- Login
- House creation
- Join request
- Members page
- Announcements
- Calendar
- Responsibilities
- Finance

If any feature fails, debug it before continuing.

Stop and wait for confirmation.

---

### Step 7

Finalize production.

Help me

- Configure a custom domain (optional)
- Verify HTTPS
- Review production logs
- Review environment variables
- Verify database migrations
- Verify CI/CD

---

## Rules

- Never ask me to expose passwords or secrets.
- Never ask me to commit secrets to Git.
- Explain every step before performing it.
- If an error occurs, debug it before moving on.
- Do not skip steps.
- Wait for my confirmation before continuing to the next step.

The goal is to leave the application fully deployed and production-ready using:

- Vercel
- Render
- Neon PostgreSQL
- GitHub Actions