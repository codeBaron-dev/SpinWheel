# react-native-spinwheel

React Native wrapper for SpinWheel Android Home Screen Widget.

## Installation

```bash
npm install react-native-spinwheel
# or
yarn add react-native-spinwheel
```

### Android Setup

1. Add the SpinWheel widget module to your React Native project's `android/settings.gradle`:

```gradle
include ':spinwheel-widget'
project(':spinwheel-widget').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-spinwheel/android/../../../spinwheel-widget')

include ':react-native-spinwheel'
project(':react-native-spinwheel').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-spinwheel/android')
```

2. Add the dependency to your app's `android/app/build.gradle`:

```gradle
dependencies {
    implementation project(':react-native-spinwheel')
}
```

3. Register the package in your `MainApplication.java` or `MainApplication.kt`:

```kotlin
// Kotlin
import com.codebaron.spinwheel.rn.SpinWheelPackage

override fun getPackages(): List<ReactPackage> {
    return listOf(
        MainReactPackage(),
        SpinWheelPackage()
    )
}
```

```java
// Java
import com.codebaron.spinwheel.rn.SpinWheelPackage;

@Override
protected List<ReactPackage> getPackages() {
    return Arrays.asList(
        new MainReactPackage(),
        new SpinWheelPackage()
    );
}
```

4. Initialize the widget in your Application class:

```kotlin
import com.codebaron.spinwheel.widget.SpinWheelWidget

class MainApplication : Application(), ReactApplication {
    override fun onCreate() {
        super.onCreate()
        SpinWheelWidget.initialize(this)
    }
}
```

## Usage

```typescript
import SpinWheelWidget from 'react-native-spinwheel';

// Check if widget is installed
const hasWidget = await SpinWheelWidget.isWidgetInstalled();

// Get widget count
const count = await SpinWheelWidget.getWidgetCount();

// Refresh widgets
await SpinWheelWidget.updateWidget();

// Trigger spin animation
await SpinWheelWidget.spinWidget();

// Clear cache
await SpinWheelWidget.clearCache();
```

## API Reference

### `updateWidget(configUrl?: string): Promise<boolean>`
Update all widgets with optional new configuration URL.

### `isWidgetInstalled(): Promise<boolean>`
Check if any SpinWheel widgets are installed on the home screen.

### `getWidgetCount(): Promise<number>`
Get the count of installed SpinWheel widgets.

### `clearCache(): Promise<boolean>`
Clear all cached configuration and images.

### `spinWidget(): Promise<boolean>`
Trigger a spin animation on the first available widget.

### `initialize(): Promise<boolean>`
Initialize the widget library (called automatically on first use).

## License

MIT
