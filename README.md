# Desktop Digital Wellbeing Monitor

A complete, production-ready Windows desktop application built with Java 17, JavaFX, SQLite, and JNA. This application replicates Android-like screen time tracking by monitoring the active application natively on Windows.

## Features

- **Background Tracking**: Continuously monitors the active application window using native Windows APIs (via JNA).
- **Accurate App Identification**: Extracts the actual executable process name (e.g., `chrome.exe`) rather than relying on transient window titles, providing an accurate, aggregated app usage summary.
- **SQLite Database**: Persists daily statistics and individual application usage securely via JDBC.
- **Modern UI**: A sleek, dark-themed JavaFX dashboard providing:
  - Total screen time for the day.
  - Pie chart breakdown of the top 5 applications.
  - Detailed, scrollable list of all tracked applications.
  - Weekly usage history (Bar Chart).
  - Settings to configure future time limits and notifications.

## Requirements

1. **Java Development Kit (JDK) 17 or higher** installed and added to your system `PATH`. (e.g., OpenJDK 17, Oracle JDK 17).
2. **Apache Maven** installed and added to your system `PATH` (for building the project).
3. **Windows Operating System**: Required since the core tracker uses native Windows APIs (User32, Kernel32, Psapi).

## Setup & Run Instructions

### Step 1: Clone or Open the Project
Open this project directory (`C:\CODES\DIGITAL WELLBEING`) in your preferred terminal or IDE (IntelliJ IDEA, Eclipse, VS Code).

### Step 2: Build the Application
Open a Command Prompt or PowerShell in the root directory (where `pom.xml` is located) and run:
```shell
mvn clean install
```
This will download all necessary dependencies (JavaFX, JNA, SQLite-JDBC) and compile the source code.

### Step 3: Run the Application
You can run the application directly via the JavaFX Maven plugin:
```shell
mvn javafx:run
```
Alternatively, you can compile and run it from your IDE by executing `com.wellbeing.Main` as your main class.

## Project Structure (Clean Architecture)

- **`com.wellbeing.Main`**: Entry point for bypassing Java module restrictions.
- **`com.wellbeing.ui`**: Contains the JavaFX Application class (`MainApp`) and FXML Controller (`MainController`).
- **`com.wellbeing.tracker`**: Contains `AppTracker` which polls Windows APIs using JNA to track application usage.
- **`com.wellbeing.service`**: `AnalyticsService` computes data for the Dashboard charts.
- **`com.wellbeing.database`**: `DBManager` configures the SQLite `wellbeing.db` and performs CRUD operations.
- **`com.wellbeing.model`**: Data models (`AppUsage`, `AppStats`).
- **`src/main/resources`**: Contains `MainView.fxml` (UI layout) and `style.css` (Dark theme).

## Required Permissions / Settings
- **No Administrator Privileges Required**: By default, JNA uses standard `PROCESS_QUERY_INFORMATION` and `PROCESS_VM_READ` access rights which work for user-level applications. Some highly secured system processes (like `Taskmgr.exe` or `dwm.exe` running as Admin) might block access; in these cases, the tracker will safely fallback to tracking the window title.
- **Antivirus Exceptions**: Because the app polls the active process every 1 second, highly restrictive antivirus software might flag the behavior. It is safe to allow it.
