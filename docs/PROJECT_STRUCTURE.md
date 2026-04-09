# 📁 Project Structure - Personal Finance Companion

The app's source code is organized into a modular package structure following **Clean Architecture** patterns.

---

## 📦 Package Hierarchy: `com.sandeep.personalfinancecompanion`

Below is a breakdown of the primary packages and their responsibilities.

### 🏠 `presentation` (UI Layer)
Contains everything related to the user interface and state management.
- **`components/`**: Reusable UI widgets (e.g., `CategoryFilter`, `TransactionCard`).
- **`home/`**: Dashboard screen, Weekly Trend chart, and `HomeViewModel`.
- **`transactions/`**: Transaction history, Add/Edit screens, and `TransactionViewModel`.
- **`goal/`**: Financial goal tracking, Contribution logs, and `GoalViewModel`.
- **`insights/`**: Spending analysis pie charts and filtering logic.
- **`profile/`**: User settings, currency selection, and budget management.
- **`navigation/`**: Type-safe navigation graph and route definitions.

### 🏗 `domain` (Business Layer)
The core "rules" of the app. Pure Kotlin code.
- **`model/`**: Central data models (`Transaction`, `Goal`, `Currency`).
- **`repository/`**: Interfaces defining data access contracts.
- **`usecase/`**: Individual business logic operations.

### 💾 `data` (Implementation Layer)
Handles the heavy lifting of data persistence.
- **`local/`**: Room database configuration and DAOs.
- **`entity/`**: Database table definitions (POJOs for Room).
- **`mapper/`**: Static functions to convert between DB Entities and Domain Models.
- **`repository/`**: Implementation of the domain's repository interfaces.

### ⚙ `di` (Dependency Injection)
- **`AppModule.kt`**: Hilt modules that provide database and repository instances.
- **`RepositoryModule.kt`**: Bridges the gap between repository interfaces and implementations.

### ⏰ `worker` (Background Tasks)
Contains WorkManager `Worker` classes for reliable background processing.
- `DailyReminderWorker`: Handles adding-expense nudges.
- `GoalReminderWorker`: Checks for goal milestones and sends alerts.

### 🛠 `util`
Shared utility classes and helper functions.
- `TransactionCategory`: Enum and mapping for expense categories.
- `DateUtils`: Helpers for date formatting and timestamp calculations.

---

## 📜 Root Files

- `README.md`: Project overview and user guide.
- `CONTRIBUTING.md`: Standards for developers.
- `DAILY_RECAP.md`: Record of daily progress and updates.
- `GEMINI.md`: Development instructions for the AI assistant.
- `PLAN.md`: Current development roadmap.
- `docs/`: Technical deep-dive documentation (this folder).
