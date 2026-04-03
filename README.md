# Personal Finance Companion 💰

Personal Finance Companion is a modern, intuitive, and feature-rich Android application designed to help users take full control of their financial life. Built with the latest Android technologies, it provides a seamless experience for tracking transactions, managing budgets, setting financial goals, and gaining deep insights into spending habits.

## ✨ Key Features

### 🏠 Dashboard (Home)
The heart of the app where you get an instant snapshot of your financial health.
- **Summary Cards:** View your current Balance, total Income, and total Expenses at a glance.
- **Budget Ring:** A visual representation of your monthly spending against your budget.
- **Weekly Trend Chart:** Analyze your spending patterns over the last 7 days with interactive bar charts.

![Dashboard Screenshot](https://via.placeholder.com/300x600?text=Dashboard+Home)

### 📝 Transaction Management
Effortlessly record and categorize every penny.
- **Add/Edit Transactions:** Quickly log income or expenses with details like amount, category, date, and description.
- **Transaction History:** A comprehensive list of all your activities with powerful filtering.
- **Categories:** Organize your spending into intuitive categories (Food, Transport, Entertainment, etc.).

![Transactions Screenshot](https://via.placeholder.com/300x600?text=Transaction+List) ![Add Transaction](https://via.placeholder.com/300x600?text=Add+Transaction)

### 🎯 Financial Goals
Save for what matters most.
- **Goal Tracking:** Create specific financial goals (e.g., "New Car", "Vacation Fund").
- **Contribution Log:** Record contributions towards your goals and watch your progress bar grow.
- **Target Dates:** Stay motivated with clear deadlines for each goal.

![Goals Screenshot](https://via.placeholder.com/300x600?text=Financial+Goals)

### 📊 Advanced Insights
Data-driven decisions for better saving.
- **Pie Charts:** Visualize your spending distribution across different categories.
- **Category Analysis:** Identify where most of your money goes and find opportunities to save.
- **Dynamic Filtering:** View insights for different time periods (Weekly, Monthly, Yearly).

![Insights Screenshot](https://via.placeholder.com/300x600?text=Spending+Insights)

### 🔔 Smart Notifications
Stay informed without effort.
- **Daily Reminders:** Never forget to log your daily expenses.
- **Goal Alerts:** Get notified when you reach milestones or when a target date is approaching.

---

## 🚀 How to Use the App

### 1. Setting Up Your Profile
When you first open the app, head to the **Profile** section to select your preferred **Currency**. This will ensure all your financial data is displayed in a way that makes sense to you.

![Profile Screenshot](https://via.placeholder.com/300x600?text=User+Profile)

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
- **Navigation:** [Compose Navigation](https://developer.android.com/jetpack/compose/navigation)

---

## 🏗 Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/Sandeep/PersonalFinanceCompanion.git
   ```
2. Open the project in **Android Studio (Ladybug or newer)**.
3. Sync Project with Gradle Files.
4. Run the app on an Emulator or Physical Device (API 26+).

---

## 🤝 Contributing
Contributions are welcome! If you have suggestions or find bugs, feel free to open an issue or submit a pull request.

---

## 📝 License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

Developed with ❤️ by Sandeep.
