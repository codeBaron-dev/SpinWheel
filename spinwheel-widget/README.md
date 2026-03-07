# SpinWheel Widget Library

Android library module that provides a customizable spin wheel home screen widget.

## Architecture

Built using Clean Architecture with MVI pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                    PRESENTATION LAYER                        │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Intent    │→ │   Reducer   │→ │       State         │  │
│  └─────────────┘  └─────────────┘  └─────────────────────┘  │
│         ↑                ↓                    ↓              │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────┐  │
│  │   Widget    │  │ SideEffect  │  │    ViewModel        │  │
│  │  Provider   │  └─────────────┘  └─────────────────────┘  │
│  └─────────────┘                                             │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                      DOMAIN LAYER                            │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                    Use Cases                         │    │
│  │  FetchConfig | LoadImages | CalculateSpin           │    │
│  └─────────────────────────────────────────────────────┘    │
│  ┌─────────────────────────────────────────────────────┐    │
│  │                  Domain Models                       │    │
│  │  WidgetConfig | WheelRotation | SpinResult          │    │
│  └─────────────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                       DATA LAYER                             │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────────┐   │
│  │   Remote     │  │    Local     │  │    Repository    │   │
│  │  (OkHttp)    │  │  (Prefs)     │  │   (Impl)         │   │
│  └──────────────┘  └──────────────┘  └──────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

## Package Structure

```
com.codebaron.spinwheel.widget/
├── di/                          # Koin dependency injection modules
│   ├── NetworkModule.kt
│   ├── DataModule.kt
│   ├── DomainModule.kt
│   └── PresentationModule.kt
│
├── data/
│   ├── remote/
│   │   ├── api/WidgetConfigApi.kt
│   │   └── dto/*.kt
│   ├── local/
│   │   ├── SharedPreferencesManager.kt
│   │   └── ImageCacheManager.kt
│   ├── mapper/ConfigDtoMapper.kt
│   └── repository/WidgetConfigRepositoryImpl.kt
│
├── domain/
│   ├── model/
│   │   ├── WidgetConfig.kt
│   │   ├── WheelRotation.kt
│   │   └── SpinResult.kt
│   ├── repository/WidgetConfigRepository.kt
│   └── usecase/
│       ├── FetchWidgetConfigUseCase.kt
│       ├── LoadWheelImagesUseCase.kt
│       └── CalculateSpinResultUseCase.kt
│
└── presentation/
    ├── mvi/
    │   ├── WidgetIntent.kt
    │   ├── WidgetState.kt
    │   ├── WidgetSideEffect.kt
    │   └── WidgetReducer.kt
    ├── viewmodel/WidgetViewModel.kt
    ├── widget/
    │   ├── SpinWheelWidgetProvider.kt
    │   └── WidgetAnimationService.kt
    └── renderer/WheelBitmapComposer.kt
```

## Usage

### Initialize in Application

```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        SpinWheelWidget.initialize(this)
    }
}
```

### Public API

```kotlin
// Refresh all widgets
SpinWheelWidget.refreshAllWidgets(context)

// Check widget count
val count = SpinWheelWidget.getWidgetCount(context)

// Check if any widgets exist
val hasWidgets = SpinWheelWidget.hasWidgets(context)
```

## Dependencies

- OkHttp for networking
- kotlinx-serialization for JSON/CBOR parsing
- Koin for dependency injection
- Coroutines for async operations

## Widget Animation

The widget uses frame-by-frame bitmap updates for smooth animation:

1. `WidgetAnimationService` receives spin intent
2. `ValueAnimator` interpolates rotation values
3. `WheelBitmapComposer` composes layered bitmap at each frame
4. `AppWidgetManager.partiallyUpdateAppWidget` updates the widget

Supported easing functions:
- `easeInOutCubic` (default)
- `linear`
- `easeIn`
- `easeOut`

## License

MIT
