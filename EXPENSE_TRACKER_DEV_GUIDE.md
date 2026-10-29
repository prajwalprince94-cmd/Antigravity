# 📱 Native Android Expense Tracker — Development Guide
### Inspired by Pixel Budget Tracker · Material 3 Expressive · Offline-First · Supabase Sync · India-Targeted
**Version 1.0 / 1.1 Master Blueprint for Solo Developers**

> **Golden Rule of This Guide:**
> Build one small feature → Test on a real device → Commit to Git → Only then move to the next feature.
>
> **Start with Level 0 today. Do not jump ahead.**

---

## 📑 Table of Contents

1. [What You Are Building & Golden Rules for Success](#1-what-you-are-building--rules-for-success)
2. [Absolute Beginner Setup (Install Everything)](#2-absolute-beginner-setup-install-everything)
3. [Recommended Learning Resources (High-Star Repos)](#3-recommended-learning-resources-high-star-repos)
4. [Material 3 Expressive UI Guide (Best Catalogs)](#4-material-3-expressive-ui-guide)
5. [Project Structure & Architecture Rules](#5-project-structure--architecture-rules)
6. [Level 0 – Empty Project That Runs](#6-level-0--empty-project-that-runs)
7. [Level 1 – Theme + Navigation Skeleton](#7-level-1--theme--navigation-skeleton)
8. [Level 2 – Room Database (Local Storage & Money Rules)](#8-level-2--room-database-local-storage)
9. [Level 3 – Accounts & Categories Screens](#9-level-3--accounts--categories-screens)
10. [Level 4 – Transaction Entry (The Most Important Screen)](#10-level-4--transaction-entry-most-important-screen)
11. [Level 5 – Dashboard + Budgets](#11-level-5--dashboard--budgets)
12. [Level 6 – Analytics Charts](#12-level-6--analytics-charts)
13. [Level 7 – Polish (Theming, App Lock, Hindi, CSV Export)](#13-level-7--polish-theming-app-lock-hindi-export)
14. [Level 8 – Supabase Sync + Subscription Billing](#14-level-8--supabase-sync--subscription-billing)
15. [Level 9 – Testing Checklist Before Every Ship](#15-level-9--testing-checklist-before-every-ship)
16. [Play Store Release Checklist](#16-play-store-release-checklist)
17. [1.0 vs 1.1 Feature Split (Final)](#17-10-vs-11-feature-split-final)
18. [Daily & Weekly Habits for Solo Developers](#18-daily--weekly-habits-for-solo-developers)

---

## 1. What You Are Building & Rules for Success

You are building a clean, modern native Android personal expense tracker. It looks and feels like Google's Pixel system UI using **Material 3 Expressive**, works **100% offline first** with a local Room database, and later synchronizes to the cloud via **Supabase** for Pro subscribers.

### Core Value Proposition
- **Lightning-fast transaction entry**: under 5 seconds with a custom number keypad.
- **Financial correctness**: transfers are never expenses, credit cards reflect liabilities, and money is **never** floating-point (`Float` or `Double`).
- **India-targeted**: Default currency is INR (₹), accounts include UPI / Cash / Bank, default categories match Indian spending habits, and Hindi language support is built in.

### 🏆 Golden Rules (Never Break These)
1. **One small feature at a time.** Never write 200 lines without compiling and running.
2. **Test on a real physical phone.** The emulator does not represent true touch latency, keypad ergonomics, or real battery/memory pressure.
3. **Commit to Git after every working feature.** If something breaks 30 minutes later, Git lets you roll back instantly.
4. **Do not touch Supabase or Billing until the offline app is rock solid.** Cloud sync adds distributed system complexity; your local core must be flawless first.
5. **The free version must be fully usable without any account.** Users should download the app and track expenses immediately.
6. **Search exact error messages.** Copy the red line from Logcat directly into Google + Stack Overflow.
7. **Sleep and rest.** Solo developer burnout is the #1 killer of projects. Consistency beats frantic sprints.

*Target: Ship a rock-solid offline v1.0 in 4–5 months of steady work (20–25 hrs/week).*

---

## 2. Absolute Beginner Setup (Install Everything)

### Step 2.1 – Download Android Studio
1. Visit [developer.android.com/studio](https://developer.android.com/studio) and download the latest stable release (Ladybug / Koala / Hedgehog).
2. Run the installer with default settings.
3. On first launch, choose **Standard Installation**. Allow it to download the Android SDK, build tools, and platform images (requires 10–20 GB of disk space and 30–60 minutes depending on internet speed).

### Step 2.2 – Create a Google Account for Play Console (Later)
- You will need a Google Play Console developer account ($25 one-time registration fee).
- **Do not pay this now.** Only register when Level 7 is complete and you are preparing for Level 9 testing.

### Step 2.3 – Install & Configure Git
1. Check terminal: `git --version`.
2. Configure your identity:
   ```bash
   git config --global user.name "Your Name"
   git config --global user.email "your.email@example.com"
   ```

### Step 2.4 – Create GitHub Account & Repository
1. Sign up on [github.com](https://github.com).
2. Create a private repository named `ExpenseTracker`.

### Step 2.5 – Create Supabase Account (Week 9+, Level 8 Only)
- Sign up at [supabase.com](https://supabase.com). Do not write any backend code until the offline app (Levels 0–7) is completely built and tested.

> 📁 **Work folder recommendation:** Keep your workspace organized at `~/ExpenseTrackerDev/ExpenseTracker`.

---

## 3. Recommended Learning Resources (High-Star Repos)

Do not reinvent standard Compose patterns. Bookmark and clone these open-source repositories to study real code when you get stuck:

### 🌟 Official Google Repositories
- **[android/compose-samples](https://github.com/android/compose-samples)** (23.5k ⭐)
  - Google's official architectural references: *Jetsnack* (theming, bottom bars), *Now in Android* (modular architecture, Room, Flow), *Rally* (finance UI, charts).
- **[Gurupreet/ComposeCookBook](https://github.com/Gurupreet/ComposeCookBook)** (6.9k ⭐)
  - A massive visual recipe book of Compose UI widgets, animations, custom buttons, and layouts.

### 💰 Expense Tracker Reference Apps
- **[darkvortex144/Expenso](https://github.com/darkvortex144/Expenso)** — Offline-first expense tracker built with Jetpack Compose, Room, and Material 3. Study its transaction input and database flow.
- **[nkuppan/expensemanager](https://github.com/nkuppan/expensemanager)** (158 ⭐) — Clean architecture with multi-account balances, budgets, and currency management.
- **[UsmanAnsari/ExpenseFlow](https://github.com/UsmanAnsari/ExpenseFlow)** — Clean Architecture + MVVM + Room example.

### 🎨 Material 3 Expressive Catalogs
- **[meticha/material-3-expressive-catalog](https://github.com/meticha/material-3-expressive-catalog)** (217 ⭐) — The premier interactive playground of Expressive components (FABs, toolbars, cards).
- **[emertozd/Compose-Material-3-Expressive-Catalog](https://github.com/emertozd/Compose-Material-3-Expressive-Catalog)** — Components extracted directly from official AndroidX snapshots.
- **[mohdamaan1/JustForPixel-ExpressiveLab](https://github.com/mohdamaan1/JustForPixel-ExpressiveLab)** — Pixel-style motion, fluid spring animations, and expressive cards.

---

## 4. Material 3 Expressive UI Guide

Pixel Budget Tracker uses Material 3 Expressive. Your app should look friendly, modern, and lively.

### Key Visual Principles
1. **Dynamic Color**: UI accents adjust according to user wallpaper on Android 12+ (`dynamicLightColorScheme` / `dynamicDarkColorScheme`).
2. **Generous Corner Radii**: Cards and containers use 16 dp to 28 dp rounded corners rather than sharp edges.
3. **Spring & Bouncy Physics**: Subtle spring animations on button presses and card expansions.
4. **Floating Navigation Bar**: Elevated floating navigation pill with clear active state pills.
5. **Clear Hierarchy**:
   - Giant bold amounts for balances and totals (`displaySmall` / `headlineLarge`).
   - Soft elevated cards for transactions and account lists.
6. **Strict Financial Color Semantics (Never Change These!)**:
   - 🟢 **Green** (`#2E7D32` / `Color(0xFF2E7D32)`): Income / positive cash flow
   - 🔴 **Red / Deep Orange** (`#C62828` / `Color(0xFFC62828)`): Expenses / liabilities
   - 🔵 **Blue / Teal** (`#1565C0` / `Color(0xFF1565C0)`): Transfers between owned accounts
7. **True Black (OLED Mode)**: Pure `#000000` surface option for battery conservation and contrast on OLED displays.

---

## 5. Project Structure & Architecture Rules

Keep it simple for a solo developer: single module first (`app`).

### Recommended Package Structure
```
com.yourname.expensetracker/
├── data/
│   ├── local/
│   │   ├── entity/          (AccountEntity, CategoryEntity, TransactionEntity, BudgetEntity)
│   │   ├── dao/             (AccountDao, CategoryDao, TransactionDao, BudgetDao)
│   │   └── AppDatabase.kt   (Room database declaration, pre-populated seeds)
│   └── repository/          (ExpenseRepositoryImpl, AccountRepositoryImpl)
├── domain/                  (Domain models, financial business rules, balance calculations)
├── ui/
│   ├── home/                (DashboardScreen, DashboardViewModel, SummaryCards)
│   ├── transaction/         (AddTransactionScreen, CustomKeypad, CategoryPicker)
│   ├── accounts/            (AccountsScreen, AccountDetailScreen, AddAccountDialog)
│   ├── budgets/             (BudgetsScreen, BudgetProgressBar, AddBudgetDialog)
│   ├── analytics/           (AnalyticsScreen, SpendingPieChart, DailyBarChart)
│   ├── settings/            (SettingsScreen, ThemeSelection, AppLockSettings)
│   ├── theme/               (Color.kt, Type.kt, Theme.kt, Shape.kt)
│   └── components/          (AmountText, AccountChip, ExpressiveCard, BottomNavBar)
├── navigation/              (AppNavHost, ScreenRoutes, BottomBarDestination)
└── util/                    (CurrencyFormatter, DateUtils, Constants)
```

### Architecture Rule for Beginners
```
Screen (Composable) ──► ViewModel ──► Repository ──► Room DAO ──► SQLite Database
```
- **Rule 1:** A Composable never talks to the database directly. It only displays State and sends events to the ViewModel.
- **Rule 2:** The database is the single local source of truth. UI observes Kotlin `Flow`s emitted from Room.

---

## 6. Level 0 – Empty Project That Runs

**Goal:** Create a clean project that builds, installs, and runs on your physical phone without errors.

### Step-by-Step Instructions
1. Open **Android Studio** → Click **New Project**.
2. Select **Empty Activity** (make sure it indicates Compose).
3. Fill in the project details:
   - **Name:** `ExpenseTracker`
   - **Package name:** `com.yourname.expensetracker` *(replace `yourname` with your handle)*
   - **Save location:** `ExpenseTrackerDev/ExpenseTracker`
   - **Language:** `Kotlin`
   - **Minimum SDK:** `API 26 (Android 8.0 Oreo)` *(covers ~96% of active devices)*
   - **Build configuration language:** `Kotlin DSL (build.gradle.kts)`
4. Click **Finish** and wait for the Gradle sync progress bar at the bottom to finish.
5. Plug your Android phone into your computer via USB (ensure **Developer Options** and **USB Debugging** are enabled).
6. Select your physical phone from the device dropdown at the top.
7. Click the green **Run (▶)** button.
8. Verify that the app launches and displays "Hello Android!" on your phone.

### 🧪 Test Checklist — Level 0
- [ ] Android Studio Gradle sync finishes with zero errors.
- [ ] Your real phone is detected via USB debugging.
- [ ] The app compiles and installs onto your phone.
- [ ] The app opens without crashing and displays the default greeting text.
- [ ] Initial Git repository initialized and committed:
  ```bash
  git init
  git add .
  git commit -m "feat: level 0 - empty compose project that runs on device"
  ```

---

## 7. Level 1 – Theme + Navigation Skeleton

**Goal:** Establish the Material 3 Expressive theme with dynamic color support and a floating bottom navigation bar that seamlessly switches between 4 placeholder screens.

### Screens to Create
1. **Home (Dashboard)**
2. **Transactions (Ledger)**
3. **Budgets (Budget Goals)**
4. **Settings (Preferences & App Lock)**

### Key Files
- `ui/theme/Color.kt` — Seed palette with dedicated Income Green, Expense Red, Transfer Blue.
- `ui/theme/Theme.kt` — Material 3 theme supporting `dynamicLightColorScheme` / `dynamicDarkColorScheme`.
- `navigation/AppNavigation.kt` — Compose `NavHost` with `NavigationBar` (or floating pill).
- Placeholder screens in `ui/home/`, `ui/transaction/`, `ui/budgets/`, `ui/settings/`.

### 🧪 Test Checklist — Level 1
- [ ] App launches with Material 3 styling (status bar and surface colors match).
- [ ] Bottom navigation displays 4 distinct items with icons and labels.
- [ ] Tapping each bottom bar icon transitions to the corresponding screen without delay or backstack duplication.
- [ ] Switching system theme between Light and Dark mode updates app colors properly.
- [ ] Git commit: `git commit -m "feat: level 1 - theme and 4-screen navigation skeleton"`

---

## 8. Level 2 – Room Database (Local Storage)

**Goal:** Establish the local database with tables for Accounts, Categories, Transactions, and Budgets, along with pre-seeded Indian categories and default accounts.

### 🚨 Critical Rule for Money Values
> **NEVER store monetary amounts as `Float` or `Double`.**
> Binary floating-point arithmetic produces precision errors (e.g., `0.1 + 0.2 = 0.30000000000000004`).
> **Always store amounts as `Long` in paise / cents (minor units).**
> - ₹100.00 = `10000L` paise
> - ₹10.50 = `1050L` paise
> Formatting as "₹100.50" happens **only** at the UI boundary.

### Entities Required
1. **`AccountEntity`**:
   - `id: String` (UUID)
   - `name: String` ("HDFC Bank", "Cash Wallet", "Paytm / UPI")
   - `type: String` ("CASH", "BANK", "CREDIT_CARD", "SAVINGS")
   - `balancePaise: Long`
   - `currency: String` (Default "INR")
   - `colorHex: String`
   - `iconKey: String`
   - `isArchived: Boolean`

2. **`CategoryEntity`**:
   - `id: String` (UUID)
   - `name: String` ("Groceries", "Food & Dining", "Rent", "Salary")
   - `type: String` ("EXPENSE", "INCOME")
   - `iconKey: String`
   - `colorHex: String`
   - `parentId: String?` (Optional subcategory)
   - `isSystem: Boolean`

3. **`TransactionEntity`**:
   - `id: String` (UUID)
   - `amountPaise: Long` (> 0)
   - `type: String` ("EXPENSE", "INCOME", "TRANSFER")
   - `accountId: String`
   - `categoryId: String?`
   - `occurredAt: Long` (Epoch milliseconds timestamp)
   - `note: String?`
   - `transferToAccountId: String?` (Linked destination account for transfers)

4. **`BudgetEntity`**:
   - `id: String` (UUID)
   - `categoryId: String?` (Null = Overall monthly budget)
   - `amountPaise: Long`
   - `month: Int` (1-12)
   - `year: Int` (e.g. 2026)

### Default Seed Data on First Run
- **Accounts:** "Cash" (₹0), "Main Bank" (₹0)
- **Expense Categories:** Food & Dining, Groceries, Shopping, Transportation, Rent & Bills, Entertainment, Health & Medical, Utilities.
- **Income Categories:** Salary, Freelance, Investments, Gifts.

### 🧪 Test Checklist — Level 2
- [ ] Database initial migration creates tables without error on fresh app start.
- [ ] Pre-seeded categories and default accounts exist on first launch.
- [ ] Adding a test transaction inserts into Room and emits updated records via `Flow`.
- [ ] Amounts are verified as integer `Long` in paise.
- [ ] Git commit: `git commit -m "feat: level 2 - room database entities, daos, and seeds"`

---

## 9. Level 3 – Accounts & Categories Screens

**Goal:** Provide full management (create, view, edit, archive) for accounts and categories.

### Free Limits (Hardcoded for v1.0)
- **Accounts:** Maximum 3 active accounts in free tier (e.g., Cash, Primary Bank, UPI).
- **Custom Categories:** Up to 10 user-created categories in addition to system defaults.
- Show an informative "Pro Feature" bottom sheet when limits are exceeded.

### 🧪 Test Checklist — Level 3
- [ ] User can create Cash, Bank, and UPI accounts with initial opening balance.
- [ ] User can add a custom category with custom icon and color picker.
- [ ] Reaching 3 accounts prevents adding a 4th and displays the upgrade prompt.
- [ ] Archiving an account hides it from active pickers without deleting transaction history.
- [ ] All data persists after terminating and reopening the app.
- [ ] Git commit: `git commit -m "feat: level 3 - account and category management with free limits"`

---

## 10. Level 4 – Transaction Entry (Most Important Screen)

**Goal:** Create the fastest, smoothest expense entry experience on Android.

### Must-Have UX Requirements
1. **Big Custom Keypad**: Large, comfortable numeric buttons (`1-9`, `0`, `.`, `⌫`, `✓`). Do not force the system soft keyboard for amount entry.
2. **Three-Way Segmented Toggle**: `[ Expense | Income | Transfer ]`
   - Expense turns amount display Red
   - Income turns amount display Green
   - Transfer turns amount display Blue
3. **Quick Account & Category Chips**: Horizontally scrolling or grid chips for one-tap selection.
4. **Date Selector**: Defaults to "Today" with quick options ("Yesterday", "Choose Date...").
5. **Atomic Balance Updates**:
   - Adding an Expense deducts from the selected account balance.
   - Adding an Income credits the account balance.
   - Transfer moves funds between two accounts without altering total net worth or counting as an expense.

### 🧪 Test Checklist — Level 4
- [ ] Expense can be entered and saved in under 5 seconds.
- [ ] Amount keypad responds instantly with zero input lag.
- [ ] Account balance updates immediately upon saving.
- [ ] Transfers correctly decrement source account and increment target account.
- [ ] User can edit and delete transactions with immediate balance recalculation.
- [ ] Git commit: `git commit -m "feat: level 4 - fast custom keypad transaction entry"`

---

## 11. Level 5 – Dashboard + Budgets

**Goal:** Open the app and immediately understand your overall financial state for the current month.

### Dashboard Requirements
- **Total Income this month** (Green).
- **Total Expense this month** (Red).
- **Net Cash Flow or Remaining Budget** (Bold, prominent).
- **Budget Progress Bars**:
  - Green when spending < 75% of budget.
  - Orange when spending is between 75% and 99%.
  - Red when 100%+ (Display "+₹X over budget" instead of just 100%).
- **Recent 5–10 Transactions** list with one-tap access to full details.

### 🧪 Test Checklist — Level 5
- [ ] Dashboard figures match exact sums of Room transactions.
- [ ] Progress bars reflect accurate percentage and dynamically switch colors.
- [ ] Selecting previous or next month updates all figures and transaction lists.
- [ ] Over-budget alerts appear when spending crosses threshold.
- [ ] Git commit: `git commit -m "feat: level 5 - dashboard summaries and budget progress"`

---

## 12. Level 6 – Analytics Charts

**Goal:** Deliver visual insight into spending habits using lightweight Compose charting (via Vico or custom Canvas drawing).

### Minimum Visualizations for v1.0
1. **Category Donut / Pie Chart**: Breakdown of spending by category with percentages and legends.
2. **Daily Spending Bar Chart**: Visual bars for each day of the current month to spot spending spikes.
3. **Month-over-Month Comparison**: Simple card comparing total spending of current month vs. previous month.

### 🧪 Test Checklist — Level 6
- [ ] Charts update automatically when a new transaction is recorded.
- [ ] Empty state renders cleanly when no transactions exist for the selected period.
- [ ] Tapping a chart section highlights the corresponding category total.
- [ ] Git commit: `git commit -m "feat: level 6 - analytics donut and daily bar charts"`

---

## 13. Level 7 – Polish (Theming, App Lock, Hindi, Export)

**Goal:** Elevate the app to a complete, release-ready offline product before introducing cloud connectivity.

### Features
1. **True Black OLED Mode**: Deep AMOLED background option.
2. **App Lock**: Biometric (fingerprint/face) + PIN fallback using Android `BiometricPrompt`.
3. **Hindi Language Support**: `values-hi/strings.xml` localization for main navigation, dashboard, and transaction screens.
4. **CSV Export**: Export all transactions within a date range to a `.csv` file saved to device storage or shared via Intent.
5. **Polished Empty States & Error Messages**: Friendly illustrations and helpful guidance.

### 🧪 Test Checklist — Level 7
- [ ] App Lock triggers on app launch and resuming from background.
- [ ] Switching device language to Hindi displays Hindi labels seamlessly.
- [ ] CSV export successfully generates a file readable in Google Sheets or Excel.
- [ ] All empty states look deliberate and polished.
- [ ] Git commit: `git commit -m "feat: level 7 - app lock, hindi localization, true black, csv export"`

---

## 14. Level 8 – Supabase Sync + Subscription Billing

> ⚠️ **Prerequisite:** Start Level 8 only after Levels 0–7 are fully functional and tested on physical hardware.

### High-Level Architecture
1. **Supabase PostgreSQL Schema**: Mirror Room tables with UUID primary keys and `user_id` foreign keys.
2. **Row Level Security (RLS)**: Enforce `auth.uid() = user_id` on every table.
3. **Authentication**: Supabase Auth supporting Email + Google Sign-In.
4. **Durable Sync Outbox**:
   - `sync_operations` table in Room.
   - Every local mutation records an outbox entry in the same atomic database transaction.
   - `WorkManager` background worker drains outbox when connectivity is available.
5. **Subscription Entitlement**: Google Play Billing Library integration verified server-side.

### 🧪 Test Checklist — Level 8
- [ ] Offline mutations queue up and upload automatically when internet reconnects.
- [ ] Logging in on a second device downloads the full financial history.
- [ ] Free users can continue using the offline core without hindrance.
- [ ] Pro entitlements unlock cloud sync and extended capabilities upon verified purchase.
- [ ] Git commit: `git commit -m "feat: level 8 - supabase sync and google play billing"`

---

## 15. Level 9 – Testing Checklist Before Every Ship

Always run through this exact checklist on a mid-range physical phone before shipping an APK/AAB:

- [ ] **Fresh Install**: App installs and initializes on a clean device without crashes.
- [ ] **High Volume**: Adding 50+ transactions rapidly produces no UI lag or ANR.
- [ ] **Math Verification**: Manually sum 5 transactions and confirm displayed account balance matches to the exact paisa.
- [ ] **Airplane Mode**: Turn on Airplane mode; all read, add, edit, and delete operations work seamlessly.
- [ ] **Theme Contrast**: Both Dynamic Color Light/Dark and True Black maintain readable contrast on financial amounts.
- [ ] **Language Consistency**: Check Hindi UI for cut-off strings or untranslated placeholders.
- [ ] **App Lock**: App Lock successfully blocks unauthorized entry and unlocks with fingerprint/PIN.
- [ ] **Data Export**: CSV export opens properly with accurate date and amount columns.
- [ ] **Configuration Changes**: Rotating screen or backgrounding the app during transaction entry preserves user input.
- [ ] **Process Death**: Force kill app from recent apps and reopen; state and data remain 100% intact.

---

## 16. Play Store Release Checklist

- [ ] Create application in **Google Play Console**.
- [ ] Provide App Title, Short Description, Full Description.
- [ ] Upload Store Assets:
  - High-res Icon (512 × 512 PNG).
  - Feature Graphic (1024 × 500 PNG).
  - Minimum 4 phone screenshots showing Keypad, Dashboard, Budgets, and Analytics.
- [ ] Complete Content Rating Questionnaire.
- [ ] Provide Privacy Policy URL (hosted on GitHub Pages).
- [ ] Target latest required API level (Android 14+ / API 34+).
- [ ] Enable Play App Signing.
- [ ] Build signed Android App Bundle (AAB):
  ```bash
  ./gradlew bundleRelease
  ```
- [ ] Upload to **Closed Testing Track** (invite 10–20 testers).
- [ ] Resolve any Pre-Launch Report warnings or crashes before promoting to Production.

---

## 17. 1.0 vs 1.1 Feature Split (Final)

| Feature | Version 1.0 (Ship First) | Version 1.1 (Next Release) |
|---|:---:|:---:|
| **Local Offline Transaction Tracking** | ✅ Yes | ✅ Yes |
| **Custom Fast Amount Keypad** | ✅ Yes | ✅ Yes |
| **Accounts (Cash, Bank, UPI)** | ✅ Yes (Max 3 free) | ✅ Unlimited (Pro) |
| **Categories & Custom Categories** | ✅ Yes (10 custom free) | ✅ Unlimited (Pro) |
| **Budgets & Dashboard Summaries** | ✅ Yes | ✅ Yes |
| **Basic Analytics (Category & Daily)** | ✅ Yes | ✅ Advanced Trends |
| **Receipt Image Attachment** | ✅ Yes (Local/Manual) | ✅ Cloud Backup |
| **True Black & Dynamic Color Theming** | ✅ Yes | ✅ Custom Accent Themes |
| **App Lock (Biometric + PIN)** | ✅ Yes | ✅ Yes |
| **Hindi Language Support** | ✅ Yes | ✅ Additional Regional Languages |
| **CSV Export** | ✅ Yes | ✅ PDF Statement Export |
| **Supabase Cloud Sync** | Basic (Pro) | Advanced Conflict UI |
| **Recurring Transactions & Reminders** | ❌ Deferred | ✅ Yes |
| **SMS Auto-Detection (India)** | ❌ Deferred | 🔍 Post-Policy Review |
| **Multi-Currency** | ❌ (INR Only) | 🔍 v2 Exploration |

---

## 18. Daily & Weekly Habits for Solo Developers

### Daily Workflow
- **Work in 90-minute focused blocks**: Eliminate distractions; focus on one specific feature or bug.
- **End every day with a working build**: Never leave your project in a broken state overnight. Commit your working progress to Git.
- **Maintain a 1-sentence developer log**: Record what was built today and what blocked you.

### Weekly Rhythm
- **Review progress against this guide**: Are you staying on track with your current Level?
- **Full hardware test**: Install your debug build fresh on your physical phone at least once a week.
- **Take 1 full rest day**: Rest prevents burnout and restores problem-solving creativity.

---
*Generated for the Native Android Expense Tracker project. Start with Level 0 today.*
