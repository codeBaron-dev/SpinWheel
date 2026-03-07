# SpinWheel - Android Home Screen Widget

A Kotlin-based Android Home Screen Widget that displays a spin wheel with smooth animations. Built with Clean Architecture and MVI pattern, with React Native wrapper support.

## Features

- Android Home Screen Widget (AppWidget)
- Remote JSON configuration support
- Google Drive-hosted image assets
- Smooth spin animation with customizable easing
- SharedPreferences state persistence
- React Native wrapper (.tgz package)
- Clean Architecture + MVI pattern

## Project Structure

```
SpinWheel/
├── app/                          # Main Android app (demo host)
├── spinwheel-widget/             # Widget library module
│   ├── data/                     # Data layer (DTOs, API, Repository)
│   ├── domain/                   # Domain layer (Models, Use Cases)
│   └── presentation/             # Presentation layer (MVI, Widget)
├── react-native-spinwheel/       # React Native wrapper
└── demo-app/                     # React Native demo app
```

## Architecture

### Clean Architecture Layers

1. **Data Layer** - Remote API, local storage, repositories
2. **Domain Layer** - Business models, use cases
3. **Presentation Layer** - MVI pattern (Intent, State, Reducer, ViewModel)

### MVI Pattern

```
Intent → Reducer → State → UI
           ↓
      SideEffect → Animation/Update
```

## Tech Stack

- **Language**: Kotlin
- **Architecture**: Clean Architecture + MVI
- **DI**: Koin
- **Networking**: OkHttp
- **Serialization**: kotlinx-serialization-json, kotlinx-serialization-cbor
- **Persistence**: SharedPreferences
- **Animation**: ValueAnimator with custom interpolators

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or later
- Kotlin 1.9+
- JDK 11+
- Gradle 8.0+

### Building the Project

1. Clone the repository:
```bash
git clone https://github.com/codebaron/SpinWheel.git
cd SpinWheel
```

2. Open in Android Studio and sync Gradle

3. Build the project:
```bash
./gradlew build
```

4. Run the app:
```bash
./gradlew :app:installDebug
```

### Adding the Widget

1. Long press on your Android home screen
2. Tap "Widgets"
3. Find "Spin Wheel" in the widget list
4. Drag it to your desired location
5. Tap the wheel to spin!

## Configuration

The widget uses a JSON configuration file:

```json
{
  "data": [{
    "id": "wheel_minimal",
    "network": {
      "attributes": {
        "refreshInterval": 300,
        "networkTimeout": 30000,
        "cacheExpiration": 3600
      },
      "assets": {
        "host": "https://your-cdn.com/assets/"
      }
    },
    "wheel": {
      "rotation": {
        "duration": 2000,
        "minimumSpins": 3,
        "maximumSpins": 5,
        "spinEasing": "easeInOutCubic"
      },
      "assets": {
        "bg": "bg.jpeg",
        "wheelFrame": "wheel-frame.png",
        "wheelSpin": "wheel-spin.png",
        "wheel": "wheel.png"
      }
    }
  }]
}
```

## React Native Integration

### Installing the Package

```bash
npm install react-native-spinwheel
# or
yarn add react-native-spinwheel
```

### Usage

```typescript
import SpinWheelWidget from 'react-native-spinwheel';

// Check widget status
const count = await SpinWheelWidget.getWidgetCount();

// Refresh widgets
await SpinWheelWidget.updateWidget();

// Trigger spin
await SpinWheelWidget.spinWidget();
```

See [react-native-spinwheel/README.md](react-native-spinwheel/README.md) for detailed integration guide.

## Creating the .tgz Package

```bash
cd react-native-spinwheel
npm install
npm run build
npm pack
```

This creates `react-native-spinwheel-1.0.0.tgz` for distribution.

## Deliverables

- [x] Kotlin widget library code (Clean Architecture + MVI)
- [x] React Native wrapper (.tgz)
- [x] Demo RN app
- [x] Documentation
- [x] Full source project

## License

MIT License - see [LICENSE](LICENSE) for details.

## Author

CodeBaron
