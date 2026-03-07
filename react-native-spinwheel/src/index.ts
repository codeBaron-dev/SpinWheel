import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-spinwheel' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

const SpinWheelWidgetModule = NativeModules.SpinWheelWidget
  ? NativeModules.SpinWheelWidget
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export interface SpinWheelWidgetConfig {
  configUrl?: string;
}

export interface SpinWheelWidgetAPI {
  /**
   * Update all widgets with optional new configuration URL
   * @param configUrl Optional URL to fetch new configuration from
   * @returns Promise that resolves to true on success
   */
  updateWidget(configUrl?: string): Promise<boolean>;

  /**
   * Check if any SpinWheel widgets are installed on the home screen
   * @returns Promise that resolves to true if at least one widget exists
   */
  isWidgetInstalled(): Promise<boolean>;

  /**
   * Get the count of installed SpinWheel widgets
   * @returns Promise that resolves to the number of widgets
   */
  getWidgetCount(): Promise<number>;

  /**
   * Clear all cached configuration and images
   * @returns Promise that resolves to true on success
   */
  clearCache(): Promise<boolean>;

  /**
   * Trigger a spin animation on the first available widget
   * @returns Promise that resolves to true if spin was triggered
   */
  spinWidget(): Promise<boolean>;

  /**
   * Initialize the widget library (called automatically on first use)
   * @returns Promise that resolves to true on success
   */
  initialize(): Promise<boolean>;
}

/**
 * Update all widgets with optional new configuration URL
 */
export const updateWidget = (configUrl?: string): Promise<boolean> => {
  return SpinWheelWidgetModule.updateWidget(configUrl || '');
};

/**
 * Check if any SpinWheel widgets are installed on the home screen
 */
export const isWidgetInstalled = (): Promise<boolean> => {
  return SpinWheelWidgetModule.isWidgetInstalled();
};

/**
 * Get the count of installed SpinWheel widgets
 */
export const getWidgetCount = (): Promise<number> => {
  return SpinWheelWidgetModule.getWidgetCount();
};

/**
 * Clear all cached configuration and images
 */
export const clearCache = (): Promise<boolean> => {
  return SpinWheelWidgetModule.clearCache();
};

/**
 * Trigger a spin animation on the first available widget
 */
export const spinWidget = (): Promise<boolean> => {
  return SpinWheelWidgetModule.spinWidget();
};

/**
 * Initialize the widget library
 */
export const initialize = (): Promise<boolean> => {
  return SpinWheelWidgetModule.initialize();
};

const SpinWheelWidget: SpinWheelWidgetAPI = {
  updateWidget,
  isWidgetInstalled,
  getWidgetCount,
  clearCache,
  spinWidget,
  initialize,
};

export default SpinWheelWidget;
