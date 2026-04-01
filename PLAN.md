# Personal Finance Companion - Development Plan

This document serves as the reference plan for building the Personal Finance Companion app.

## 1. Architecture & Tech Stack
*   **UI Framework:** Jetpack Compose (Modern, declarative UI).
*   **Architecture:** MVVM (Model-View-ViewModel).
*   **Data Source:** Mock API Service with Ktor `MockEngine` (In-memory simulation of a REST backend).
*   **Navigation:** Jetpack Navigation Compose.
*   **Dependency Injection:** Hilt for managing ViewModels and Repositories.
*   **Coroutines & Flow:** For asynchronous operations and reactive UI updates.

## 2. Data Models
*   **Transaction**:
    *   `id` (String)
    *   `amount` (Double)
    *   `type` (Enum: INCOME, EXPENSE)
    *   `category` (Enum: Food, Transport, Salary, etc.)
    *   `date` (Long - Unix Timestamp)
    *   `notes` (String)

## 3. Core Features & Screens

### A. Home Dashboard (`HomeScreen`)
*   **Quick Summary Cards:** Current Balance, Total Income, Total Expenses.
*   **Visual Element:** Circular Progress (Budget Tracker) and Weekly Trend Chart.
*   **Recent Activity:** Navigation to full transaction history.
*   **Action Buttons:** Quick access to add Income or Expense.

### B. Transaction Tracking (`TransactionListScreen` & `AddEditTransactionScreen`)
*   **Transaction History:** Grouped by date (Today, Yesterday, etc.).
*   **Filtering/Searching:** Search by notes/category and filter by Type (Income/Expense).
*   **Add/Edit Form:** Form with Amount, Type, Category, and Notes.
*   **Swipe to Delete:** Easy deletion with "Undo" snackbar support.

### C. Insights Screen (`InsightsScreen`)
*   **Spending by Category:** Visual breakdown using stylized proportion bars.
*   **Data Aggregation:** Logic to calculate totals per category from the transaction list.

## 4. UI/UX Considerations
*   **Navigation:** Bottom Navigation Bar (Home, Transactions, Insights).
*   **State Management:** `StateFlow` for UI states (Loading, Success, Error).
*   **Theming:** Teal-based Material 3 theme with custom brand colors.

## 5. Implementation Phases
*   **Phase 1: API & Data Layer (Done)**
    *   Setup `FakeMockEngine` and `TransactionApiService`.
    *   Implement `TransactionRepository`.
*   **Phase 2: Core UI & Navigation (Done)**
    *   Implement Main Activity with Bottom Nav and FAB.
    *   Build Home, Transaction List, and Add/Edit screens.
*   **Phase 3: Logic Refinement (Current)**
    *   Fix currency inconsistencies.
    *   Connect missing navigation callbacks in Home screen.
    *   Improve Category Breakdown logic to use all transactions.
*   **Phase 4: Insights & Polish**
    *   Implement real data for the Weekly Trend chart.
    *   Final UI polish and error handling.
