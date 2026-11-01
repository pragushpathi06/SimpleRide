# VoltRide – Landscape HMI Ride Dashboard

A Jetpack Compose dashboard showing simulated vehicle speed (km/h), motor ON/OFF and
kill-switch Active/Inactive. No hardware, CAN or backend is used.

## Build & run
1. Open the project in Android Studio (Ladybug or newer), JDK 17.
2. Let Gradle sync.
3. Create/select an emulator (any phone or tablet, API 26+). The app forces landscape.
4. Run the `app` configuration. The dashboard starts updating immediately.

## Run unit tests
```
./gradlew testDebugUnitTest
```
Or right-click `src/test` in Android Studio and choose Run Tests.

## Architecture
```
SimulatedVehicleDataSource (Flow<VehicleSignal>, ~1 Hz)
        │  raw, possibly invalid signals
        ▼
VehicleStateMapper  (validation + DriveMode derivation)
        ▼
DashBoardViewModel  (transformLatest stale timeout → StateFlow<DashBoardUiState>)
        ▼
DashboardScreen     (collectAsStateWithLifecycle, stateless composables)
```
* **UI:** `presentation/ui/screens/dashboard`, design system in `presentation/ui/design_system`.
* **ViewModel:** `DashBoardViewModel` owns state and exposes a `StateFlow`.
* **Data provider:** `data/local/SimulatedVehicleDataSource`, injected via Hilt (`di/DataModule`).
* **Domain:** `VehicleState`, `DriveMode`, `VehicleStateMapper`.

## Simulation script (3 ticks of 1 s each unless noted)
| Step | Speed | Motor | Kill | Mode shown |
|---|---|---|---|---|
| Parked | 0 | OFF | Inactive | PARKED |
| Ready | 0 | ON | Inactive | READY |
| Riding | 25 | ON | Inactive | RIDING |
| Accelerating | 50 | ON | Inactive | ACCELERATING |
| Motor disabled | 0 | OFF | Active | MOTOR DISABLED |
| Invalid speed (2 ticks) | -12 | ON | Inactive | DEGRADED DATA |
| Data dropout (4 ticks, nothing sent) | – | – | – | SIGNAL LOST |
| Motor flag lost (2 ticks) | 25 | missing | Inactive | DEGRADED DATA |

The script loops. The "LIVE SCENARIO" strip highlights the active mode.

## Edge-case handling
* **Invalid speed** (negative, NaN, infinite, > 200 km/h) is discarded, shown as `--`, flagged `SPEED_INVALID`.
* **Missing fields** become `UNKNOWN` states and are flagged, never crash.
* **Stale data:** no signal within 2.5 s → `SIGNAL_LOST`, values hidden.
* **Motor OFF ≠ speed 0:** motor off while moving is shown as COASTING.
* **Kill switch active while motor reports ON** is flagged as inconsistent data.

## Tests
* `VehicleStateMapperTest` – all five scenarios plus invalid speed, missing signals, coasting, kill/motor conflict.
* `DashBoardViewModelTest` – signal updates state, silence triggers signal-lost, timely signal keeps state live.

Test dependencies (`app/build.gradle.kts`):
```kotlin
testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
```
