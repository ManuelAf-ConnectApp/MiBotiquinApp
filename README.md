# CleanArch - Kotlin Multiplatform Project

CleanArch is a **Kotlin Multiplatform (KMP)** project designed with a robust **Clean Architecture** and **MVI (Model-View-Intent)** pattern. It targets both **Android** and **iOS** platforms using **Compose Multiplatform** for a unified UI experience.

## 🚀 Project Overview

The main goal of this project is to demonstrate a scalable and maintainable architecture for cross-platform mobile development. It leverages the latest technologies in the Kotlin ecosystem to provide a high-quality user experience with a single codebase for most of the logic.

## 🏗️ Architecture

The project is divided into several modules following Clean Architecture principles:

### 1. `:domain` (Core Business Logic)
*   **Pure Kotlin**: Contains no platform-specific code or dependencies on other layers.
*   **Models**: Defines the data structures used throughout the application (e.g., `User`).
*   **Repositories**: Interface definitions for data operations.
*   **Use Cases**: Implementation of specific business rules (e.g., `LoginUseCase`, `RegisterUseCase`).

### 2. `:data` (Infrastructure)
*   **Repository Implementations**: Concrete implementations of the interfaces defined in the domain layer.
*   **Data Sources**: Handles data retrieval from remote (API) or local (Database/Preferences) sources.
*   **Mappers**: Converts data transfer objects (DTOs) to domain models.

### 3. `:presentation` (UI Logic & State Management)
*   **MVI Pattern**: Uses `State`, `Intent`, and `Effect` to manage UI logic predictably.
*   **ViewModels**: Handles the business logic for each screen, interacting with the domain layer.
*   **Screens**: Composable functions that define the UI for both platforms.
*   **Components**: Reusable UI elements like `CustomTopBar` and `CustomBottomBar`.

### 4. `:composeApp` (Platform Entry Points)
*   **Common Main**: Entry point for shared Compose code and Dependency Injection (Koin) setup.
*   **Android Main**: Android-specific implementation and Activity.
*   **iOS Main**: iOS-specific implementation and ViewController.

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
*   **Dependency Injection**: [Koin](https://insert-koin.io/)
*   **Concurrency**: Kotlin Coroutines & Flow
*   **Navigation**: Navigation3 / Compose Navigation
*   **State Management**: MVI (Model-View-Intent)
*   **Networking/Persistence**: (Ready for Ktor / SQLDelight)

## ✨ Key Features

- [x] **Splash Screen**: Initial loading and navigation logic.
- [x] **Authentication**:
    - Login with validation.
    - User Registration.
    - Forgot Password flow.
- [x] **Home Dashboard**: Modern dashboard with categorized information cards.
- [x] **Settings & Profile**:
    - User profile overview.
    - App preferences (Notifications, Dark Mode).
    - Account security and legal information.
- [x] **Theming**: Integrated Material 3 design system.

## 🏁 Getting Started

### Prerequisites
*   Android Studio Ladybug or later.
*   Xcode (for iOS development).
*   Kotlin Multiplatform plugin.

### Build and Run

#### Android
- From Android Studio: Select the `composeApp` run configuration.
- From Terminal:
  ```shell
  ./gradlew :composeApp:assembleDebug
  ```

#### iOS
- Open the `iosApp` directory in Xcode and run the project.
- Or use the `iosApp` run configuration in Android Studio if configured.

---

*Developed with ❤️ using Kotlin Multiplatform.*
