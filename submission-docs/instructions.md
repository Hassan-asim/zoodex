# Zoodex - Setup Instructions

## Prerequisites
- **Android Studio:** Jellyfish | 2023.3.1 or newer recommended.
- **Java Development Kit (JDK):** JDK 17 (Ensure `JAVA_HOME` is correctly set in your environment variables).
- **Android SDK:** API Level 34 (Android 14) minimum recommended for building.
- **Physical Device:** Android device with GPS capabilities (emulators can be used by mocking location via extended controls).

## Environment Variable Requirements
If running from the command line, ensure:
- `JAVA_HOME` points to your JDK 17 installation.
- `ANDROID_HOME` points to your local Android SDK directory.
The application connects to a Supabase backend. The API keys (`SUPABASE_URL` and `SUPABASE_KEY`) are currently embedded securely in `SupabaseService.kt` for testing convenience, so no manual `.env` configuration is strictly required to test social and territory features.

## How to Install Dependencies
1. Clone or extract the repository to your local machine.
2. Open the project folder in Android Studio.
3. Android Studio will automatically sync the Gradle project.
   - If it fails, open the terminal and run `./gradlew clean` followed by `./gradlew build --refresh-dependencies`.
4. Ensure all MapLibre and Supabase/Kotlin dependencies defined in `build.gradle.kts` are resolved.

## How to Run the Application Locally
1. Connect an Android device via USB debugging or start an Android Virtual Device (AVD).
2. In Android Studio, select the `app` run configuration.
3. Click the **Run** (Play) button, or press `Shift + F10`.
4. Alternatively, use the CLI: 
   ```bash
   ./gradlew installDebug
   ```

## How to Test the Application
### 1. GPS & MapLibre Tracking
- Navigate to the **Territory Conquest (Map)** screen.
- Grant location permissions.
- Tap **Start Tracking** and simulate movement (or walk in the real world). Walk at least 15 meters to register a valid polygon claim.
- Tap **Stop Tracking**; a toast/status will confirm the territory was saved.

### 2. Turn-Based Arena
- Click the **Arena** button from the main hub.
- Observe that the screen forces Landscape mode.
- Select your Squad, which will initialize a battle against a dynamically randomized AI boss.
- Test move buttons, verify HP bars deplete, and confirm XP/Gold is rewarded.

## Troubleshooting Steps
- **Build Fails with "JAVA_HOME is not set":** Open your OS environment variables and point `JAVA_HOME` to your JDK 17 path (e.g., `C:\Program Files\Java\jdk-17`). Restart your terminal.
- **Map shows a blank grid:** Ensure the device has an active internet connection to download the Stamen Toner / OpenFreeMap vector tiles.
- **Cannot claim territory:** GPS spoofers on emulators sometimes fail to trigger `onLocationResult`. Ensure your emulator is actively sending GPS waypoints via the Extended Controls panel.

## Notes for Judges/Reviewers
- The MapLibre implementation utilizes a highly customized dark Stamen-style map to fit the cyberpunk aesthetic.
- The `ArenaScreen` utilizes completely custom UI-drawn HP bars and shape-based animations instead of standard Android widgets to achieve maximum aesthetic control.
- Ensure location permissions are granted; otherwise, the territory conquest mechanic will fall back to a permission request state.
