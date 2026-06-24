# Web Sandbox — Native Shell Cabin

**Web Sandbox** (also known as *Native Shell*) is a high-performance Android sandbox application built with Kotlin, Jetpack Compose, and Material Design 3 (High-Density layout). It enables developers and power users to run any web application or URL inside a completely seamless, isolated native frame—with **zero web indicators** (no browser address bars, reload indicators, or navigation chrome).

The app includes advanced support for custom CSS stylesheet injections, dynamic DOM element hiding rules, arbitrary JavaScript package compilation, and strict domain sandboxing to lock navigation within designated boundaries.

---

## 🚀 Key Features

*   **Pure Native Isolation**: Render any URL in 100% full-screen layout. Over-scroll indicators, web scrollbars, and standard browser frames are suppressed, making external sites (like Notion, Linear, GitHub, or custom dashboards) indistinguishable from native apps.
*   **High-Density Design Theme**: A customized Material 3 dynamic-ready slate palette with a clean Grid layout, quick launcher, and modern status bar simulations.
*   **Persistent Sandboxes (Room DB)**: Multi-profile sandbox configurations are stored locally in an offline SQLite database via Jetpack Room, keeping your rules, styling overrides, and scripts saved securely.
*   **Advanced UI Injections**:
    *   *Hide CSS Selectors*: Automatically hide banners, headers, cookie prompts, and ads (with automatic background DOM mutation observers to lock elements in hidden states, even on lazy-loaded pages).
    *   *Custom CSS overrides*: Modify typography, backgrounds, margins, or borders of the remote page.
    *   *Custom JavaScript*: Run custom scripts immediately after the document finishes loading.
*   **Dual-Layer Hidden Escape Gestures**:
    1.  **3-Finger Long Press**: Holding three fingers on the screen simultaneously for `1.3 seconds` launches the hidden Control Center.
    2.  **5-Tap Action**: Tapping the screen rapidly five times in a row instantly brings up the exit prompt.
*   **Domain Containment**: Restricts navigation inside the target sandbox domain, preventing trackers, ad-redirects, or external hyperlinks from breaking isolation.
*   **Direct Boot Mode**: Instantly launches a specified sandbox configuration at application startup, completely bypassing the dashboard to mimic a single-purpose native app.

---

## 🛠️ Tech Stack & Architecture

*   **Runtime & Language**: Kotlin, Android SDK (targeting API Level 36).
*   **UI Framework**: Jetpack Compose & Material 3 (M3 standard components).
*   **Local Storage**: Jetpack Room DB (DAO pattern, SQLite persistence, Flow-based reactive updates).
*   **Concurrency**: Kotlin Coroutines & `StateFlow` state-management.
*   **Hybrid Native Interface**: Custom optimized `WebView` with customized cookies, user-agent overrides (Desktop Mode vs. Mobile), and safe JS-Java bridging.
*   **Testing Suite**: Robolectric for fast JVM database and context testing, Roborazzi for automated visual screenshot regression tests.

---

## 📦 Project Structure

```text
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/example/
│   │   │   │   ├── MainActivity.kt           # App Entry Point & ViewModel Binding
│   │   │   │   ├── data/
│   │   │   │   │   ├── SandboxProfile.kt     # Entity Schema definition
│   │   │   │   │   ├── SandboxDao.kt         # Database Query Interfaces
│   │   │   │   │   ├── SandboxDatabase.kt    # Room Database Configuration
│   │   │   │   │   └── SandboxRepository.kt  # Clean Repository layer
│   │   │   │   └── ui/
│   │   │   │       ├── SandboxViewModel.kt   # Core MVVM State Engine
│   │   │   │       ├── SandboxDashboard.kt   # M3 High-Density Dashboard, Grid, & Forms
│   │   │   │       ├── SandboxWebView.kt     # Optimized Full-screen Canvas, Gestures, & Injection
│   │   │   │       └── theme/                # Custom Theme definition
│   │   │   └── AndroidManifest.xml           # App Manifest & Internet Permissions
│   │   └── test/                             # JVM & Robolectric test suites
│   └── build.gradle.kts                      # App dependencies & Kotlin compiler targets
└── build.gradle.kts                          # Project level build scripts
```

---

## ⚙️ Advanced Customization Examples

### 1. Stripping Headers and Navigation
For a clean mobile GitHub experience, inject the following CSS selector configuration:
*   **Selectors to Hide**: `.js-header-wrapper, .banner-cookie-consent, div[class*='app-down']`
*   **Outcome**: Removes the bulky GitHub header bar and app promotion cards, offering a flawless content canvas.

### 2. Custom Dark Theme Styling
Force a rich slate theme on standard white sites using the Custom CSS box:
```css
body {
  background-color: #0b0b0f !important;
  color: #f1f1f1 !important;
}
div, section, article {
  background-color: transparent !important;
  border-color: #1f1f2e !important;
}
```

---

## 🏁 Building and Running

### Prerequisites
*   Android Studio Ladybug (or newer).
*   JDK 17+.

### Building via CLI
To compile the debug APK:
```bash
gradle assembleDebug
```

To run Robolectric unit and instrumentation-mimic tests:
```bash
gradle :app:testDebugUnitTest
```

---

## 🛡️ License & Attributions
This software is developed securely with client-side local persistence (no external cloud connections or telemetries). All sandboxed contexts are executed under standard sandboxed permissions.

<br>
<div align="center">
  <sub>Built with ❤️ by <a href="https://github.com/Jivaansh-Yadav">Jivaansh</a></sub>
