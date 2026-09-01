# Mémoire 🧩

Mémoire is a high-fidelity, offline-first memory game for Android. It has been modernized from its original concept into a premium experience featuring fluid animations, dynamic layouts, and a robust custom game creation system.

## ✨ Features

-   **🎨 Pure Jetpack Compose UI**: A modern, declarative UI built entirely with Compose and Material 3.
-   **🌓 Immersive Dark & Light Modes**: Centralized color palette with an immersive pitch-black "OLED" dark mode.
-   **📐 Dynamic Grid Fitting**: A custom algorithm that ensures all cards fit on one screen without scrolling, regardless of the board size or device orientation.
-   **🎭 Premium Animations**:
    -   High-fidelity **3D Y-axis flips** for cards.
    -   Staggered grid pop-in effects.
    -   Cross-fade board transitions.
    -   Celebratory confetti effects on wins.
-   **🛠️ Custom Board Creator**:
    -   Modern **Android Photo Picker** integration with selection limits and numeric ordering.
    -   Local image processing and scaling for optimal performance.
    -   Interactive grid management allowing you to swap or remove images before saving.
-   **📂 Saved Boards Gallery**:
    -   A dedicated page to browse your custom creations with "Sticker-style" thumbnails.
    -   Full-screen board previews to inspect your gallery before playing.
    -   One-tap play and deletion management.
-   **💾 Offline-First**: Powered by **Room Database** and internal file storage. No internet connection or cloud accounts required.
-   **🔊 Audio Feedback**: Infrastructure for low-latency sound effects (card flips, matches, failures, and wins).

## 🛠️ Tech Stack

-   **Language**: Kotlin (2.0.21)
-   **UI Framework**: Jetpack Compose
-   **Dependency Injection**: Hilt
-   **Local Database**: Room
-   **Image Loading**: Coil (with local Uri and Bitmap support)
-   **Serialization**: Gson
-   **Concurrency**: Coroutines & Flow
-   **Build System**: Gradle 8.13.2

## 🚀 Getting Started

### Prerequisites
-   Android Studio Ladybug (or newer)
-   Android SDK 23+ (Min SDK 23, Target SDK 35)

### Installation
1.  Clone the repository.
2.  Sync the project with Gradle files.
3.  Add your preferred sound effects to `app/src/main/res/raw/` (Optional):
    -   `card_flip.mp3`
    -   `match_success.mp3`
    -   `match_fail.mp3`
    -   `game_win.mp3`
4.  Run the app on an emulator or physical device.

---
*Inspired by the original concept by Rahul Pandey.*
