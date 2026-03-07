# Integration Guide

This guide covers integrating the SpinWheel Widget into your Android or React Native project.

## Android Integration

### 1. Add the Module Dependency

In your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":spinwheel-widget"))
}
```

Or if using as an AAR:

```kotlin
dependencies {
    implementation(files("libs/spinwheel-widget.aar"))
}
```

### 2. Initialize the Library

In your `Application` class:

```kotlin
import com.codebaron.spinwheel.widget.SpinWheelWidget

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        SpinWheelWidget.initialize(this)
    }
}
```

### 3. Register the Application

In `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ...>
</application>
```

### 4. Widget Declaration (Automatic via Manifest Merge)

The widget provider and service are automatically merged from the library manifest. No additional configuration needed.

---

## React Native Integration

### 1. Install the Package

```bash
# From .tgz file
npm install ./react-native-spinwheel-1.0.0.tgz

# Or link locally during development
npm install ../react-native-spinwheel
```

### 2. Android Configuration

#### settings.gradle

```gradle
include ':spinwheel-widget'
project(':spinwheel-widget').projectDir = new File(
    rootProject.projectDir,
    '../node_modules/react-native-spinwheel/android/../../../spinwheel-widget'
)

include ':react-native-spinwheel'
project(':react-native-spinwheel').projectDir = new File(
    rootProject.projectDir,
    '../node_modules/react-native-spinwheel/android'
)
```

#### app/build.gradle

```gradle
dependencies {
    implementation project(':react-native-spinwheel')
}
```

#### MainApplication.java (Java)

```java
import com.codebaron.spinwheel.rn.SpinWheelPackage;
import com.codebaron.spinwheel.widget.SpinWheelWidget;

public class MainApplication extends Application implements ReactApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        SpinWheelWidget.initialize(this);
    }

    @Override
    protected List<ReactPackage> getPackages() {
        return Arrays.asList(
            new MainReactPackage(),
            new SpinWheelPackage()
        );
    }
}
```

#### MainApplication.kt (Kotlin)

```kotlin
import com.codebaron.spinwheel.rn.SpinWheelPackage
import com.codebaron.spinwheel.widget.SpinWheelWidget

class MainApplication : Application(), ReactApplication {
    override fun onCreate() {
        super.onCreate()
        SpinWheelWidget.initialize(this)
    }

    override fun getPackages(): List<ReactPackage> = listOf(
        MainReactPackage(),
        SpinWheelPackage()
    )
}
```

### 3. Using in JavaScript/TypeScript

```typescript
import SpinWheelWidget from 'react-native-spinwheel';

// Initialize (optional - auto-initializes on first call)
await SpinWheelWidget.initialize();

// Check if widget exists
const hasWidget = await SpinWheelWidget.isWidgetInstalled();

// Get widget count
const count = await SpinWheelWidget.getWidgetCount();

// Refresh all widgets
await SpinWheelWidget.updateWidget();

// Update with custom config URL
await SpinWheelWidget.updateWidget('https://example.com/config.json');

// Trigger spin animation
await SpinWheelWidget.spinWidget();

// Clear cached data
await SpinWheelWidget.clearCache();
```

---

## Custom Configuration

### Remote JSON Configuration

Host your configuration JSON on a CDN or server:

```json
{
  "data": [{
    "id": "my_wheel",
    "name": "Custom Wheel",
    "type": "Widget",
    "network": {
      "attributes": {
        "refreshInterval": 300,
        "networkTimeout": 30000,
        "retryAttempts": 3,
        "cacheExpiration": 3600,
        "debugMode": false
      },
      "assets": {
        "host": "https://your-cdn.com/wheel-assets/"
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
        "bg": "background.jpg",
        "wheelFrame": "frame.png",
        "wheelSpin": "button.png",
        "wheel": "wheel.png"
      }
    }
  }],
  "meta": {
    "version": 1,
    "copyright": "Your Company"
  }
}
```

### Asset Requirements

| Asset | Dimensions | Format | Purpose |
|-------|------------|--------|---------|
| bg | 500x500 | JPEG/PNG | Background layer |
| wheel | 500x500 | PNG (transparent) | Rotating wheel |
| wheelFrame | 500x500 | PNG (transparent) | Static frame overlay |
| wheelSpin | ~125x125 | PNG (transparent) | Center button |

### Google Drive Hosting

To host assets on Google Drive:

1. Upload images to Google Drive
2. Get shareable link for each file
3. Convert to direct download URL:
   - Share URL: `https://drive.google.com/file/d/FILE_ID/view`
   - Direct URL: `https://drive.google.com/uc?export=download&id=FILE_ID`

The library automatically converts share URLs to direct download URLs.

---

## Troubleshooting

### Widget Not Appearing

1. Ensure the app is installed on the device (not emulator)
2. Check that the application class initializes `SpinWheelWidget`
3. Verify manifest merge completed successfully

### Images Not Loading

1. Check internet permission in manifest
2. Verify image URLs are accessible
3. Check cache directory for corrupted files

### Animation Stuttering

1. Reduce frame rate in `WidgetAnimationService` (increase `frameIntervalMs`)
2. Reduce output bitmap size in `WheelBitmapComposer`
3. Use smaller source images

### React Native Bridge Issues

1. Ensure native module is registered in `getPackages()`
2. Rebuild the app completely (`./gradlew clean && ./gradlew assembleDebug`)
3. Check that all projects are included in `settings.gradle`

---

## API Reference

### SpinWheelWidget (Kotlin)

```kotlin
object SpinWheelWidget {
    fun initialize(context: Context)
    fun refreshAllWidgets(context: Context)
    fun getWidgetCount(context: Context): Int
    fun hasWidgets(context: Context): Boolean
}
```

### SpinWheelWidget (React Native)

```typescript
interface SpinWheelWidgetAPI {
    updateWidget(configUrl?: string): Promise<boolean>
    isWidgetInstalled(): Promise<boolean>
    getWidgetCount(): Promise<number>
    clearCache(): Promise<boolean>
    spinWidget(): Promise<boolean>
    initialize(): Promise<boolean>
}
```
