# Library Management System

A Spring Boot REST API that manages books, patrons, and the full lending lifecycle across multiple branches. Demonstrates OOP principles, SOLID design, and the Observer, Factory, and Strategy design patterns using only in-memory storage (no database required).

---

## Table of Contents

- [Features](#features)
- [Tech Stack](#tech-stack)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Design Patterns](#design-patterns)
- [SOLID Principles](#solid-principles)
- [Class Diagram](#class-diagram)
- [Project Structure](#project-structure)

---

## Features

**Core**
- Book management — add, update, remove, search by title / author / ISBN
- Patron management — register members, update profiles, track borrowing history
- Lending — checkout (14-day period) and return with full loan history
- Inventory — real-time available / borrowed book tracking

**Extensions**
- Multi-branch support — create branches, filter inventory by branch, transfer books between branches
- Reservation system — reserve borrowed books; patrons are automatically notified (Observer pattern) when a copy is returned
- Recommendation system — three swappable algorithms (Strategy + Factory patterns): genre-based, author-based, and popularity-based

---

## Tech Stack

| Layer | Technology |
|---|---|
| Framework | Spring Boot 3.2 |
| Language | Java 17 |
| Build | Maven |
| Persistence | In-memory (ConcurrentHashMap) |
| Logging | SLF4J + Logback (Spring default) |
| Testing | JUnit 5 + Mockito + AssertJ |
| Utilities | Lombok |

---

## Getting Started

### Prerequisites
- Java 17+
- Maven 3.8+

### Run

```bash
git clone <your-repo-url>
cd library-management-system
mvn spring-boot:run
```

The server starts on `http://localhost:8080`.

On startup, `DataInitializer` seeds **2 branches**, **11 book copies**, and **3 patrons** so every endpoint is immediately explorable.

### Run Tests

```bash
mvn test
```

### Build JAR

```bash
mvn clean package
java -jar target/library-management-system-1.0.0.jar
```

---

## API Reference

### Branches `/api/branches`

| Method | Path | Description |
|---|---|---|
| POST | `/api/branches` | Create a new branch |
| GET | `/api/branches` | List all branches |
| GET | `/api/branches/{branchId}` | Get branch by ID |
| POST | `/api/branches/transfer` | Transfer a book between branches |

### Books `/api/books`

| Method | Path | Description |
|---|---|---|
| POST | `/api/books` | Add a book copy to a branch |
| GET | `/api/books` | List all books |
| GET | `/api/books/{bookId}` | Get book by ID |
| PUT | `/api/books/{bookId}` | Update book metadata |
| DELETE | `/api/books/{bookId}` | Remove a book (not if borrowed) |
| GET | `/api/books/search/title?q=` | Search by title (partial, case-insensitive) |
| GET | `/api/books/search/author?q=` | Search by author |
| GET | `/api/books/search/isbn?q=` | Search by exact ISBN |
| GET | `/api/books/available` | List available copies |
| GET | `/api/books/borrowed` | List borrowed copies |
| GET | `/api/books/branch/{branchId}` | List books at a branch |

### Patrons `/api/patrons`

| Method | Path | Description |
|---|---|---|
| POST | `/api/patrons` | Register a new patron |
| GET | `/api/patrons` | List all patrons |
| GET | `/api/patrons/{patronId}` | Get patron by ID |
| PUT | `/api/patrons/{patronId}` | Update patron profile |
| GET | `/api/patrons/{patronId}/history` | Full loan history |
| GET | `/api/patrons/{patronId}/loans` | Active loans only |

### Lending `/api/loans`

| Method | Path | Description |
|---|---|---|
| POST | `/api/loans/checkout` | Check out a book (by ISBN + branchId) |
| POST | `/api/loans/{loanId}/return` | Return a book |
| GET | `/api/loans/{loanId}` | Get loan details |

### Reservations `/api/reservations`

| Method | Path | Description |
|---|---|---|
| POST | `/api/reservations` | Reserve a currently-borrowed book |
| DELETE | `/api/reservations/{reservationId}/patron/{patronId}` | Cancel a reservation |
| GET | `/api/reservations/patron/{patronId}` | List reservations for a patron |
| GET | `/api/reservations/isbn/{isbn}/pending` | Pending reservations for an ISBN |

### Recommendations & Notifications

| Method | Path | Description |
|---|---|---|
| GET | `/api/patrons/{patronId}/recommendations?strategy=GENRE_BASED&limit=5` | Get book recommendations |
| GET | `/api/recommendations/strategies` | List available strategies |
| GET | `/api/patrons/{patronId}/notifications` | All notifications |
| GET | `/api/patrons/{patronId}/notifications/unread` | Unread notifications |
| PATCH | `/api/notifications/{notificationId}/read` | Mark notification as read |

**Recommendation strategies:** `GENRE_BASED` · `AUTHOR_BASED` · `POPULARITY_BASED`

---

### Example: Full Lending Flow

```bash
# 1. Create a branch
curl -X POST http://localhost:8080/api/branches \
  -H "Content-Type: application/json" \
  -d '{"name":"Central","address":"1 Main St","phone":"555-0100"}'

# 2. Add a book (use branchId from step 1)
curl -X POST http://localhost:8080/api/books \
  -H "Content-Type: application/json" \
  -d '{"isbn":"978-0-7432-7356-5","title":"1984","author":"George Orwell","publicationYear":1949,"genre":"Dystopian","branchId":"<branchId>"}'

# 3. Register a patron
curl -X POST http://localhost:8080/api/patrons \
  -H "Content-Type: application/json" \
  -d '{"name":"Alice","email":"alice@example.com","preferredGenres":["Dystopian"]}'

# 4. Checkout (use patronId and branchId from above)
curl -X POST http://localhost:8080/api/loans/checkout \
  -H "Content-Type: application/json" \
  -d '{"patronId":"<patronId>","isbn":"978-0-7432-7356-5","branchId":"<branchId>"}'

# 5. Return (use loanId from step 4)
curl -X POST http://localhost:8080/api/loans/<loanId>/return
```

---

## Design Patterns

### 1. Observer Pattern
**Files:** `LibraryEvent`, `LibraryEventListener`, `LibraryEventPublisher`, `ReservationNotificationListener`

When a book is returned, `LendingServiceImpl` publishes a `BOOK_RETURNED` event via `LibraryEventPublisher`. `ReservationNotificationListener` is subscribed to this event type and automatically finds all `PENDING` reservations for that ISBN, marks them `NOTIFIED`, and creates in-app `Notification` records for each waiting patron.

This decouples the lending logic from the notification logic — adding new reactions to events (e.g. email dispatch, analytics) requires zero changes to existing code.

### 2. Strategy Pattern
**Files:** `RecommendationStrategy`, `GenreBasedRecommendationStrategy`, `AuthorBasedRecommendationStrategy`, `PopularityBasedRecommendationStrategy`

The recommendation algorithm is selected at runtime by name. All three strategies implement the same `RecommendationStrategy` interface, making them interchangeable. The calling code (`RecommendationServiceImpl`) never references a concrete class.

### 3. Factory Pattern
**Files:** `RecommendationStrategyFactory`

The factory auto-discovers all `RecommendationStrategy` beans via Spring's DI and builds a lookup map keyed by `strategyName()`. Adding a new strategy requires only creating a new `@Component` class — the factory and service need no changes (Open/Closed Principle in action).

---

## SOLID Principles

| Principle | Where applied |
|---|---|
| **Single Responsibility** | Each class has one job: `Book` is a data model, `BookServiceImpl` handles book business logic, `BookRepository` handles storage, `BookController` handles HTTP. |
| **Open/Closed** | `RecommendationStrategyFactory` discovers strategies automatically — adding a new algorithm never modifies existing code. |
| **Liskov Substitution** | Any `RecommendationStrategy` implementation can replace another without breaking `RecommendationServiceImpl`. |
| **Interface Segregation** | Services are split by domain: `BookService`, `PatronService`, `LendingService`, `ReservationService`, `BranchService`, `NotificationService`, `RecommendationService`. Controllers depend only on the interfaces they need. |
| **Dependency Inversion** | All controllers and services depend on interfaces, not concrete implementations. Spring injects the concrete beans. |

---

## Class Diagram

```
┌─────────────────────────────────────────────────────────────────────────┐
│                          DOMAIN MODELS                                   │
└─────────────────────────────────────────────────────────────────────────┘

  ┌──────────────┐     holds many     ┌──────────────┐
  │    Branch    │◄───────────────────│     Book     │
  │──────────────│                    │──────────────│
  │ branchId     │                    │ bookId       │
  │ name         │                    │ isbn         │
  │ address      │                    │ title        │
  │ phone        │                    │ author       │
  │ bookIds[]    │                    │ publicYear   │
  └──────────────┘                    │ genre        │
                                      │ branchId     │
                                      │ status ──────┼──► BookStatus
                                      └──────┬───────┘    (AVAILABLE/
                                             │             BORROWED/
                                             │             RESERVED/
                                             │             TRANSFERRED)
  ┌──────────────┐                    ┌──────┴───────┐
  │    Patron    │                    │     Loan     │
  │──────────────│                    │──────────────│
  │ patronId     │◄───────────────────│ loanId       │
  │ name         │  borrows via loan  │ bookId ──────┼──► Book
  │ email        │                    │ patronId     │
  │ phone        │                    │ branchId     │
  │ memberSince  │                    │ checkoutDate │
  │ borrowHist[] │                    │ dueDate      │
  │ activeLoans[]│                    │ returnDate   │
  │ reservations[]                    │ status ──────┼──► LoanStatus
  │ prefGenres[] │                    └──────────────┘    (ACTIVE/
  └──────────────┘                                        RETURNED/OVERDUE)

  ┌──────────────┐                    ┌──────────────┐
  │ Reservation  │                    │ Notification │
  │──────────────│                    │──────────────│
  │ reservationId│                    │notificationId│
  │ isbn         │                    │ patronId     │
  │ patronId     │                    │ type         │
  │ branchId     │                    │ message      │
  │ status ──────┼──► ReservationStatus│ read        │
  │ reservedAt   │    (PENDING/        │ createdAt   │
  │ notifiedAt   │     NOTIFIED/       └──────────────┘
  └──────────────┘     FULFILLED/
                        CANCELLED)

┌─────────────────────────────────────────────────────────────────────────┐
│                        SERVICE LAYER                                     │
└─────────────────────────────────────────────────────────────────────────┘

  «interface»           «interface»           «interface»
  BookService ◄──── BookServiceImpl     PatronService ◄──── PatronServiceImpl
  LendingService ◄─ LendingServiceImpl  BranchService ◄──── BranchServiceImpl
  ReservationService ◄─ ReservationServiceImpl
  RecommendationService ◄─ RecommendationServiceImpl
  NotificationService ◄─── NotificationServiceImpl

┌─────────────────────────────────────────────────────────────────────────┐
│                    OBSERVER PATTERN                                      │
└─────────────────────────────────────────────────────────────────────────┘

  LendingServiceImpl
        │ publishes
        ▼
  LibraryEventPublisher ──── notifies ───► LibraryEventListener (interface)
        │                                        ▲
        │                                        │ implements
        │                              ReservationNotificationListener
        │                              (subscribes to BOOK_RETURNED)
        │                              → marks reservations NOTIFIED
        │                              → creates Notification records
        │
  LibraryEvent
  (type: BOOK_RETURNED | BOOK_CHECKED_OUT |
         BOOK_ADDED | BOOK_TRANSFERRED |
         RESERVATION_PLACED | LOAN_OVERDUE)

┌─────────────────────────────────────────────────────────────────────────┐
│               STRATEGY + FACTORY PATTERN                                 │
└─────────────────────────────────────────────────────────────────────────┘

  RecommendationServiceImpl
        │ asks factory for strategy by name
        ▼
  RecommendationStrategyFactory
        │ resolves to
        ├──► GenreBasedRecommendationStrategy
        ├──► AuthorBasedRecommendationStrategy
        └──► PopularityBasedRecommendationStrategy
                    │
                    └── all implement RecommendationStrategy «interface»
                        recommend(patron, allBooks, limit) → List<Book>

┌─────────────────────────────────────────────────────────────────────────┐
│                    REST CONTROLLERS                                      │
└─────────────────────────────────────────────────────────────────────────┘

  BookController          → /api/books/**
  PatronController        → /api/patrons/**
  LendingController       → /api/loans/**
  ReservationController   → /api/reservations/**
  BranchController        → /api/branches/**
  RecommendationController→ /api/patrons/{id}/recommendations
                            /api/patrons/{id}/notifications/**
                            /api/recommendations/strategies

┌─────────────────────────────────────────────────────────────────────────┐
│              IN-MEMORY REPOSITORIES (ConcurrentHashMap)                  │
└─────────────────────────────────────────────────────────────────────────┘

  BookRepository · PatronRepository · LoanRepository
  ReservationRepository · BranchRepository · NotificationRepository
```

---

## Project Structure

```
src/
├── main/java/com/library/
│   ├── LibraryApplication.java
│   ├── config/
│   │   └── DataInitializer.java          # Seeds sample data on startup
│   ├── controller/                        # REST endpoints
│   │   ├── BookController.java
│   │   ├── PatronController.java
│   │   ├── LendingController.java
│   │   ├── ReservationController.java
│   │   ├── BranchController.java
│   │   └── RecommendationController.java
│   ├── service/                           # Interfaces
│   │   ├── BookService.java
│   │   ├── PatronService.java
│   │   ├── LendingService.java
│   │   ├── ReservationService.java
│   │   ├── BranchService.java
│   │   ├── RecommendationService.java
│   │   ├── NotificationService.java
│   │   └── impl/                          # Implementations
│   │       ├── BookServiceImpl.java
│   │       ├── PatronServiceImpl.java
│   │       ├── LendingServiceImpl.java
│   │       ├── ReservationServiceImpl.java
│   │       ├── BranchServiceImpl.java
│   │       ├── RecommendationServiceImpl.java
│   │       └── NotificationServiceImpl.java
│   ├── repository/                        # In-memory stores
│   │   ├── BookRepository.java
│   │   ├── PatronRepository.java
│   │   ├── LoanRepository.java
│   │   ├── ReservationRepository.java
│   │   ├── BranchRepository.java
│   │   └── NotificationRepository.java
│   ├── model/                             # Domain entities
│   │   ├── Book.java
│   │   ├── Patron.java
│   │   ├── Loan.java
│   │   ├── Reservation.java
│   │   ├── Branch.java
│   │   └── Notification.java
│   ├── dto/request/                       # Validated request bodies
│   │   ├── CreateBookRequest.java
│   │   ├── UpdateBookRequest.java
│   │   ├── CreatePatronRequest.java
│   │   ├── UpdatePatronRequest.java
│   │   ├── CheckoutRequest.java
│   │   ├── ReservationRequest.java
│   │   ├── TransferBookRequest.java
│   │   └── CreateBranchRequest.java
│   ├── observer/                          # Observer pattern
│   │   ├── LibraryEvent.java
│   │   ├── LibraryEventListener.java
│   │   ├── LibraryEventPublisher.java
│   │   └── ReservationNotificationListener.java
│   ├── strategy/                          # Strategy pattern
│   │   ├── RecommendationStrategy.java
│   │   ├── GenreBasedRecommendationStrategy.java
│   │   ├── AuthorBasedRecommendationStrategy.java
│   │   └── PopularityBasedRecommendationStrategy.java
│   ├── factory/                           # Factory pattern
│   │   └── RecommendationStrategyFactory.java
│   ├── enums/
│   │   ├── BookStatus.java
│   │   ├── LoanStatus.java
│   │   ├── ReservationStatus.java
│   │   └── NotificationType.java
│   └── exception/
│       ├── ResourceNotFoundException.java
│       ├── BusinessException.java
│       └── GlobalExceptionHandler.java
└── test/java/com/library/service/
    ├── BookServiceTest.java
    ├── LendingServiceTest.java
    ├── PatronServiceTest.java
    ├── ReservationServiceTest.java
    └── RecommendationServiceTest.java
```
