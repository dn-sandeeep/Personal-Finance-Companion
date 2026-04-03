# Personal Finance Companion 💰

**Empower your financial journey with a modern, intuitive, and data-driven companion.**

Personal Finance Companion is a premium Android application designed to transform how you manage your money. Built with **Modern Android Development (MAD)** standards, it provides a seamless, high-performance experience for tracking transactions, setting ambitious goals, and conquering financial challenges.

---

## 📖 Feature Guide: Screen-by-Screen Breakdown

### 1. 🏠 Dashboard (Home Screen)
The command center for your financial life. This is where you get an instant snapshot of your health.

- **Snapshot Cards:** View your **Current Balance**, **Monthly Income**, and **Monthly Expenses** at a glance.
- **Budget Monitoring:** The **Monthly Budget Tracker** (circular ring) shows your total expenses against your user-set limit. Tap the **Edit (Pencil)** icon to set your goal.
- **Interactive Weekly Trend:** A 7-day bar chart of your spending. Tap any bar to open a detailed list of transactions for that specific day.
- **Category Breakdown Summary:** A color-coded bar illustrating which categories are consuming your budget. Tap any category to see its full history.

![Dashboard Home](screenshots/dashboard.png) ![Weekly Trend Details](screenshots/weekly_trend_details.png)

---

### 2. 📜 Transaction History
Effortlessly record and review every penny.

- **Viewing History:** Access a comprehensive list of all your financial activities, sorted by date.
- **Logging Transactions:** 
    - Tap the **"+" (Add)** button to log new entries.
    - Choose **Income** (Green) or **Expense** (Red).
    - Select from intuitive categories (e.g., 🍔 Food, 🚗 Transport).
    - Add an amount, date, and optional notes.
- **Dynamic Sync:** Every new transaction immediately updates your Home Screen balance and budget ring.

![Transaction History](screenshots/transaction_history.png) ![Add Transaction](screenshots/add_transaction_guide.png)

---

### 3. 🎯 Financial Goals & Challenges
Save for what matters most and build discipline.

#### **Saving Goals**
- **Create & Track:** Set goals like "New Car" or "Dream Home" with specific targets.
- **Add Contributions:** Tap a goal card and select **"Add Contribution"** to log your savings.
- **Progress Tracking:** Watch the progress bar and percentage grow as you get closer to your target.

#### **No Spend Challenge**
- **Gamify Discipline:** Activate the challenge to build the habit of avoiding unnecessary costs.
- **Streak Tracking:** Monitor your consistency with **Current** and **Best** streak records.
- **Potential Savings:** See an estimate of how much you're saving by staying disciplined.
- **Challenge Calendar:** Use the mini-calendar to visualize your "No Spend" days over the last 30 days.

![Saving Goals](screenshots/goals_management.png) ![No Spend Challenge](screenshots/no_spend_guide.png)

---

### 4. 📊 Advanced Insights
Understand the "Why" behind your spending with data-driven analytics.

- **Visualization:** High-fidelity pie charts and breakdown bars showing your spending distribution.
- **Leakage Analysis:** Identify trends and categories where you are spending more than planned.
- **Temporal Filtering:** View insights filtered by day, week, month, or year to see how your habits evolve.

![Spending Insights](screenshots/category_analysis.png)

---

### 5. 🌍 Global Settings & Personalization
Tailor the app to your unique financial needs.

- **Global Currency System:** Switch between **10+ major world currencies** (USD, INR, EUR, JPY, etc.). The entire app (Balance, History, Goals) updates its formatting instantly.
- **Smart Notifications:** Reliable background reminders (powered by WorkManager) to ensure you log your daily transactions and hit your milestones.

![Currency & Profile](screenshots/currency_selection.png)

---

## 🛠 Tech Stack & Architecture

- **Language:** [Kotlin](https://kotlinlang.org/)
- **UI Framework:** [Jetpack Compose](https://developer.android.com/jetpack/compose)
- **Dependency Injection:** [Hilt](https://developer.android.com/training/dependency-injection/hilt-android)
- **Persistence:** [Room](https://developer.android.com/training/data-storage/room) & [DataStore](https://developer.android.com/topic/libraries/architecture/datastore)
- **Background Operations:** [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)

---

## 🚀 Getting Started

1.  **Clone the Repository:** `git clone https://github.com/Sandeep/PersonalFinanceCompanion.git`
2.  **Open in Android Studio:** Sync Gradle and select your device/emulator (API 26+).
3.  **Run:** Click the Green Play button!

---

## 📄 License
MIT License.

---

**Developed with ❤️ by Sandeep.**
