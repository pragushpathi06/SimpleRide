# VoltRide – Landscape HMI Ride Dashboard

A Jetpack Compose dashboard showing simulated vehicle speed (km/h), motor ON/OFF and kill-switch Active/Inactive. No hardware, CAN or backend is used.

## Features
- Vehicle speed display (km/h).
- Motor ON/OFF status.
- Kill-switch Active/Inactive status.
- Driving mode and vehicle issue indicators.
- Landscape-oriented HMI dashboard.
- Simulated vehicle data updating approximately once per second.
- Invalid and missing signal handling.
- Stale-data detection and SIGNAL LOST indication.
- Jetpack Compose UI with reusable composables and Preview support.

## Tech Stack
- **Language:** Kotlin
- **UI:** Jetpack Compose, Material 3
- **Architecture:** ViewModel-based UI state management
- **Asynchronous Programming:** Kotlin Coroutines and Flow
- **Dependency Injection:** Hilt
- **Build System:** Gradle
- **IDE:** Android Studio Ladybug or newer
- **JDK:** 17
- **Minimum Android Version:** API 26

## Build & run
1. Open the project in Android Studio (Ladybug or newer), JDK 17.
2. Let Gradle sync.
3. Create/select an emulator (any phone or tablet, API 26+). The app forces landscape.
4. Run the `app` configuration. The dashboard starts updating immediately.

Build from the command line:

```bash
./gradlew assembleDebug
```

On Windows:

```bash
gradlew.bat assembleDebug
```

## Run unit tests

```bash
./gradlew testDebugUnitTest
```

On Windows:

```bash
gradlew.bat testDebugUnitTest
```

Or right-click `src/test` in Android Studio and choose Run Tests.

## Architecture

```text
SimulatedVehicleDataSource (Flow<VehicleSignal>, ~1 Hz)
        │ raw, possibly invalid signals
        ▼
VehicleStateMapper (validation + DriveMode derivation)
        ▼
DashBoardViewModel (transformLatest stale timeout → StateFlow<DashBoardUiState>)
        ▼
DashboardScreen (collectAsStateWithLifecycle, stateless composables)
```

- **UI:** `presentation/ui/screens/dashboard`, design system in `presentation/ui/design_system`.
- **ViewModel:** `DashBoardViewModel` owns state and exposes a `StateFlow`.
- **Data provider:** `data/local/SimulatedVehicleDataSource`, injected via Hilt (`di/DataModule`).
- **Domain:** `VehicleState`, `DriveMode`, `VehicleStateMapper`.

## Simulation script (3 ticks of 1 s each unless noted)

| Step | Speed | Motor | Kill | Mode shown |
|---|---:|---|---|---|
| Parked | 0 | OFF | Inactive | PARKED |
| Ready | 0 | ON | Inactive | READY |
| Riding | 25 | ON | Inactive | RIDING |
| Accelerating | 50 | ON | Inactive | ACCELERATING |
| Motor disabled | 0 | OFF | Active | MOTOR DISABLED |
| Invalid speed (2 ticks) | -12 | ON | Inactive | DEGRADED DATA |
| Data dropout (4 ticks, nothing sent) | – | – | – | SIGNAL LOST |
| Motor flag lost (2 ticks) | 25 | Missing | Inactive | DEGRADED DATA |

The script loops. The "LIVE SCENARIO" strip highlights the active mode.

## Edge-case handling

- **Invalid speed:** Negative, NaN, infinite, or > 200 km/h values are discarded, displayed as `--`, and flagged `SPEED_INVALID`.
- **Missing fields:** Become `UNKNOWN` states and are flagged; missing data should not crash the application.
- **Stale data:** No signal within 2.5 s → `SIGNAL_LOST`, values hidden.
- **Motor OFF ≠ speed 0:** Motor OFF while moving is shown as COASTING.
- **Kill-switch conflict:** Kill switch Active while motor reports ON is flagged as inconsistent data.

## Tests

- `VehicleStateMapperTest` – covers the five normal scenarios plus invalid speed, missing signals, coasting, and kill/motor conflict.
- `DashBoardViewModelTest` – verifies that signal updates change state, silence triggers signal-lost, and timely signals keep the dashboard live.

Test dependency (`app/build.gradle.kts`):

```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
```

## Assumptions & limitations

- All vehicle signals are simulated; no physical vehicle or CAN integration is included.
- No backend or live vehicle data source is required.
- Driving modes are derived from the simulated signals and mapping rules.
- Signal timing and dropout behavior depend on the simulation implementation.
- Unit tests validate application logic, not real vehicle hardware.
- Emulator testing and Compose Previews do not replace testing on target hardware.

## Test execution checklist

- [ ] Build the application successfully.
- [ ] Run `testDebugUnitTest`.
- [ ] Verify the dashboard launches in landscape orientation.
- [ ] Confirm speed, motor, kill-switch, and driving mode updates.
- [ ] Verify invalid and missing signal handling.
- [ ] Verify SIGNAL LOST after a data dropout.
- [ ] Confirm normal operation resumes when signals return.
- [ ] Check UI alignment, text overlap, and readability.

## Project structure

```text
app/
└── src/
    ├── main/
    │   ├── java/
    │   │   └── .../
    │   │       ├── data/local/
    │   │       │   └── SimulatedVehicleDataSource
    │   │       ├── di/
    │   │       │   └── DataModule
    │   │       ├── domain/
    │   │       │   ├── VehicleState
    │   │       │   ├── DriveMode
    │   │       │   └── VehicleStateMapper
    │   │       └── presentation/ui/
    │   │           ├── design_system/
    │   │           └── screens/dashboard/
    │   ├── test/
    │   └── androidTest/
```

*Note: The project structure is illustrative; actual paths may differ.*

## Future improvements

- Expand automated UI test coverage.
- Improve accessibility and screen-size adaptability.
- Add more signal-validation and recovery scenarios.
- Integrate real vehicle data if hardware support becomes necessary.
- Improve dashboard diagnostics and error reporting.
