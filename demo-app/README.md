# SpinWheel Demo App

A React Native demo application showcasing the SpinWheel Android Home Screen Widget.

## Prerequisites

- Node.js >= 18
- React Native CLI
- Android Studio
- JDK 11+

## Installation

1. Install dependencies:
```bash
cd demo-app
npm install
```

2. Build and install the React Native SpinWheel package:
```bash
cd ../react-native-spinwheel
npm install
npm run build
cd ../demo-app
```

3. Run on Android:
```bash
npm run android
```

## Features

- View active widget count
- Refresh widgets from the app
- Trigger spin animation remotely
- Clear widget cache

## Adding the Widget

1. Long press on your Android home screen
2. Tap "Widgets"
3. Find "Spin Wheel" in the list
4. Drag it to your home screen
5. Tap the wheel to spin!

## License

MIT
