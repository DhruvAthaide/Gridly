# Gridly

Gridly is a modern Android application built with Jetpack Compose, designed to provide comprehensive data visualization and race countdown features.

## Features

*   **Race Countdown Widget:** specific widget powered by Jetpack Glance to keep track of upcoming races directly from your home screen.
*   **Data Visualization:** Interactive charts and graphs using MPAndroidChart to visualize data effectively.
*   **Local Data Persistence:** Robust local data storage using Room Database for offline access and reliability.
*   **Network Operations:** Efficient networking with Ktor for fetching real-time data.
*   **Modern UI:** A clean and responsive user interface built entirely with Material 3 and Jetpack Compose.
*   **Custom Tabs:** Seamless in-app browsing experience using Android Custom Tabs.

## Tech Stack

*   **Language:** Keep it Kotlin
*   **UI Framework:** Jetpack Compose (Material 3)
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Networking:** Ktor Client (CIO engine, Content Negotiation, Serialization)
*   **Database:** Room Database (KSP)
*   **Dependency Injection:** Manual Dependency Injection / ViewModel Factory
*   **Asynchronous Programming:** Kotlin Coroutines & Flow
*   **Widgets:** Jetpack Glance
*   **Charts:** MPAndroidChart
*   **Serialization:** Kotlinx Serialization

## Installation

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/DhruvAthaide/Gridly.git
    ```
2.  **Open in Android Studio:**
    Open Android Studio and select "Open an existing Android Studio project", then navigate to the cloned directory.
3.  **Sync Gradle:**
    Allow Android Studio to sync the project with Gradle files to download necessary dependencies.
4.  **Run the App:**
    Connect an Android device or start an emulator and click the "Run" button (Shift+F10).

## Contributing

Contributions are welcome! Please follow these steps:

1.  Fork the repository.
2.  Create a new branch for your feature or bug fix: `git checkout -b feature-name`.
3.  Commit your changes: `git commit -m 'Add some feature'`.
4.  Push to the branch: `git push origin feature-name`.
5.  Submit a pull request.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
