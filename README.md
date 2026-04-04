# Personal Finance Companion 💰

Personal Finance Companion is a modern, intuitive, and feature-rich Android application designed to help users take full control of their financial life. Built with the latest Android technologies, it provides a seamless experience for tracking transactions, managing budgets, setting financial goals, and gaining deep insights into spending habits.

## 📲 Download

Stay on top of your finances! Download the latest APK and start managing your money like a pro.

[![Download APK](https://img.shields.io/badge/Download-APK-2E7D32?style=for-the-badge&logo=android&logoColor=white)](https://github.com/dn-sandeeep/Personal-Finance-Companion/releases/latest)

---

## ✨ Key Features

### 🏠 Dashboard (Home)
The heart of the app where you get an instant snapshot of your financial health.
- **Summary Cards:** View your current Balance, total Income, and total Expenses at a glance.
- **Budget Ring:** A visual representation of your monthly spending against your budget.
- **Weekly Trend Chart:** Analyze your spending patterns over the last 7 days with interactive bar charts.

<img width="200" height="600" alt="home screen" src="https://github.com/user-attachments/assets/25e0b20c-03f2-4f36-83d0-d8a2ee5cf344" />

### 📝 Transaction Management
Effortlessly record and categorize every penny.
- **Transaction History:** A comprehensive list of all your activities with powerful filtering.
- **Categories:** Organize your spending into intuitive categories (Food, Transport, Entertainment, etc.).
- **Add Transactions:** Quickly log income or expenses with details like amount, category, date, and description.
- **Edit Transaction:** Swipe right on the transaction that you need to edit.
- **Delete Transaction:** Swipe left on the transaction that you need to delete.

<img width="200" height="600" alt="Trandsaction history" src="https://github.com/user-attachments/assets/0b5a5dff-2892-4b7d-bca1-c1426ca96fda" />

<img width="200" height="600" alt="add transaction" src="https://github.com/user-attachments/assets/25d5c1d3-409d-451c-9343-2c49547249d3" />

<img width="200" height="600" alt="Edit trandsaction" src="https://github.com/user-attachments/assets/0a0c203f-2368-4f74-b313-97b47944eee8" />

<img width="200" height="600" alt="Delete trandsaction" src="https://github.com/user-attachments/assets/08aaa065-0c79-4198-b307-046a5f067477" />


### 🎯 Financial Goals
Save for what matters most.
- **Goal Tracking:** Create specific financial goals (e.g., "New Car", "Dream Home").
- **Contribution Log:** Record contributions towards your goals and watch your progress bar grow.
- **Target Dates:** Stay motivated with clear deadlines for each goal.
- **Prioritize Goal:** Prioritize your goal that is important to you.
- **No Spend Challenge:** User can choose the days for the challenge to avoid non-essential spends (e.g., entertainment, food)

<img width="200" height="600" alt="Goal" src="https://github.com/user-attachments/assets/1df24c88-40ab-45bf-8110-ce8ab30827fa" />

<img width="200" height="600" alt="Goal-2" src="https://github.com/user-attachments/assets/c3ffd0fd-12d5-4020-a252-da86d90decdf" />


### 📊 Advanced Insights
Data-driven decisions for better savings.
- **Pie Charts:** Visualize your spending distribution across different categories.
- **Category Analysis:** Identify where most of your money goes and find opportunities to save.
- **Dynamic Filtering:** View insights for different time periods (Weekly, Monthly, Yearly).

<img width="200" height="600" alt="Insights" src="https://github.com/user-attachments/assets/125d53ba-648e-4656-9f5e-399ae7222cfe" />


### 🔔 Smart Notifications
Stay informed without effort.
- **Daily Reminders:** Never forget to log your daily expenses.
- **Goal Alerts:** Get notified when you reach milestones or when a target date is approaching.
- **Budget Setup:** Smart "Set Monthly Budget" prompt on Home screen for new users.

### 💎 Premium UI Experience
The app ensures a professional feel even when data is missing or loading:
- **Loading States:** Centered progress indicators for all data-fetching operations.
- **Empty States:** Beautifully designed placeholders for empty lists (Transactions, Goals, Insights) using a unified `EmptyState` component.
- **Error Handling:** Robust error views with retry options to handle unexpected data issues gracefully.

---

## 🚀 How to Use the App

### 1. Setting Up Your Profile
When you first open the app, head to the **Profile** section to select your preferred **Currency**. This will ensure all your financial data is displayed in a way that makes sense to you.

<img width="200" height="600" alt="Settings" src="https://github.com/user-attachments/assets/ff02e4e2-cb0f-4d74-b422-b5810d319001" />
<img width="200" height="600" alt="Currency" src="https://github.com/user-attachments/assets/c44c7d23-5338-4a3c-b089-64004561abde" />



### 2. Logging Your First Transaction
- Tap the **"+" (Add)** button on the Transaction screen.
- Select the **Transaction Type** (Income or Expense).
- Enter the **Amount** and choose a **Category**.
- (Optional) Add a description and adjust the date.
- Hit **Save**. Your balance and dashboard charts will update instantly!

### 3. Creating a Financial Goal
- Go to the **Goals** tab.
- Tap **"Add New Goal"**.
- Set a **Name**, **Target Amount**, and **Target Date**.
- Once saved, you can tap on the goal anytime to **Add Contributions**. Watch the progress ring fill up as you get closer to your target.

### 4. Analyzing Your Spending
- Navigate to the **Insights** tab.
- Use the **Pie Chart** to see which categories consume the most of your budget.
- Switch between different timeframes to understand how your habits change over time.

---

## 🛠 Tech Stack & Architecture

This project follows the **Modern Android Development (MAD)** standards and **Clean Architecture** principles.

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose) (100% Declarative UI)
- **Architecture:** MVVM (Model-View-ViewModel) with Clean Architecture (Domain, Data, Presentation layers).
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Local Database:** [Room](https://developer.android.com/training/data-storage/room)
- **Asynchronous Tasks:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [Flow](https://kotlinlang.org/docs/flow.html)
- **Background Work:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager) (for reminders)
- **Preferences:** [DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (for user settings)
- **Navigation:** [Compose Navigation](https://developer.android.com/jackpack/compose/navigation)
- **State Management:** Unified `UiState` (Sealed Interface) pattern for robust Loading/Success/Error handling.

---

## 🏗 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/dn-sandeeep/Personal-Finance-Companion.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync Project with Gradle Files.
4. Run the app on an Emulator or Physical Device (API 24+).

---

Developed by Sandeep.
