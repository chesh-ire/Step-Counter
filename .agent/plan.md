# Project Plan

Fixing step counting latency and adding goal setting (steps, water, weight) to the StepCounter app. Re-implementing core features if missing.

## Project Brief

# Project Brief: StepCounter Fitness Companion (Enhanced)

An all-in-one health and weight loss tracker.

### Features
*   **Real-time Step Tracking**: Accurate, low-latency step counting using SensorManager and a Foreground Service.
*   **Goal Setting**: User-defined daily goals for steps, water intake, and target weight.
*   **Unified Health Dashboard**: Bento-style UI showing progress against goals for steps, calories, water, and weight.
*   **Activity & Nutrition Logging**: Manual logging for water and calories.
*   **Smart Behavioral Nudges**: Hourly reminders for movement and hydration.
*   **Advanced Analytics**: Interactive charts for weight trends and step history with goal overlays and smart insights.
*   **Persistent Storage**: Room database for logs and user preferences for goals.

### High-Level Technical Stack
*   **Kotlin**, **Jetpack Compose (Material 3)**.
*   **Room (KSP)** for health data and **DataStore** for user goals.
*   **SensorManager API** & **Foreground Service**.
*   **WorkManager** for nudges.
*   **Canvas** for custom interactive charts.

## Implementation Steps

### Task_1_Database_and_Step_Tracking: Set up the Room database for health data (Steps, Water, Calories, Weight) and implement the SensorManager logic to track steps in the background.
- **Status:** COMPLETED
- **Updates:** Room database and DAOs implemented for StepEntry, WaterEntry, CalorieEntry, and WeightEntry. StepCounterService (foreground service) created to track steps via SensorManager. HealthRepository established with reactive Flow support. Material 3 theme and Edge-to-Edge display configured. Permissions for ACTIVITY_RECOGNITION and POST_NOTIFICATIONS added. Adaptive icon created.
- **Acceptance Criteria:**
  - Room database and DAOs are functional
  - SensorManager correctly detects and records steps
  - Data repository provides reactive flows for UI updates

### Task_2_Dashboard_and_Logging: Develop the Material 3 Bento-style dashboard and logging interfaces for manual entry of water, calories, and weight.
- **Status:** COMPLETED
- **Updates:** Bento-style Dashboard implemented with real-time step counts, calorie, water, and weight summaries. Logging interfaces for manual entry (Water, Calories, Weight) created as interactive dialogs within the dashboard. MainViewModel established to manage dashboard state and repository interactions. Navigation Compose integrated for future screen expansions. UI strictly follows Material 3 guidelines with vibrant colors and edge-to-edge support.
- **Acceptance Criteria:**
  - Dashboard displays real-time step counts and daily summaries
  - Logging screens/dialogs allow manual data entry
  - UI follows Material 3 guidelines and uses ViewModels

### Task_3_Nudges_and_Analytics: Implement WorkManager for hourly behavioral nudges and create progress charts to visualize health trends over time.
- **Status:** COMPLETED
- **Updates:** WorkManager integrated for hourly behavioral nudges (activity and hydration). Analytics Screen created with custom Canvas-based interactive charts for weight and step trends. Smart insights engine developed in MainViewModel to provide personalized feedback based on historical data. Navigation between Dashboard and Analytics implemented. Vibrant Material 3 UI maintained throughout.
- **Acceptance Criteria:**
  - WorkManager triggers hourly notifications for activity/hydration
  - Interactive charts display weight and progress trends
  - Smart insights are derived from historical data

### Task_4_M3_Refinement_and_Final_Verification: Apply vibrant Material 3 theming, implement edge-to-edge display, create an adaptive app icon, and perform a final verification run.
- **Status:** COMPLETED
- **Updates:** Vibrant Material 3 color scheme and dynamic theming (Android 12+) applied. Full Edge-to-Edge display with proper WindowInsets handling implemented in Dashboard and Analytics screens. Refined adaptive app icon (ic_launcher.xml) with background and foreground layers. UI consistency and alignment verified across all screens and dialogs. Final build successful.
- **Acceptance Criteria:**
  - Vibrant M3 color scheme and dynamic theming applied
  - Full Edge-to-Edge display implemented
  - Adaptive app icon created
  - App builds and runs without crashes
  - All existing tests pass
  - Application stability and UI alignment verified

### Task_5_Goal_Setting_and_Progress: Implement daily goal setting for steps, water, and weight using Jetpack DataStore. Update the Dashboard and Analytics screens to integrate progress tracking and goal overlays.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - DataStore correctly persists and retrieves goals
  - Goal setting UI allows users to input targets
  - Dashboard shows progress indicators relative to goals
  - Analytics charts include goal lines/overlays
- **StartTime:** 2026-03-25 22:19:09 IST

### Task_6_Latency_Optimization_and_Final_Run: Optimize StepCounterService for low-latency, real-time tracking and perform a final comprehensive verification run.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Step tracking is responsive with minimal latency
  - Foreground service operates reliably
  - App builds and runs without crashes
  - All existing tests pass
  - Critic_agent verifies stability and requirement alignment

