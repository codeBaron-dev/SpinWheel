import React, {useState, useEffect} from 'react';
import {
  SafeAreaView,
  StyleSheet,
  Text,
  View,
  TouchableOpacity,
  StatusBar,
  Alert,
} from 'react-native';
import SpinWheelWidget from 'react-native-spinwheel';

const App = () => {
  const [widgetCount, setWidgetCount] = useState<number>(0);
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    checkWidgetStatus();
  }, []);

  const checkWidgetStatus = async () => {
    try {
      const count = await SpinWheelWidget.getWidgetCount();
      setWidgetCount(count);
    } catch (error) {
      console.error('Error checking widget status:', error);
    }
  };

  const handleRefresh = async () => {
    setIsLoading(true);
    try {
      await SpinWheelWidget.updateWidget();
      await checkWidgetStatus();
      Alert.alert('Success', 'Widgets refreshed successfully!');
    } catch (error) {
      Alert.alert('Error', 'Failed to refresh widgets');
    } finally {
      setIsLoading(false);
    }
  };

  const handleSpin = async () => {
    try {
      const result = await SpinWheelWidget.spinWidget();
      if (!result) {
        Alert.alert('Info', 'No widget found to spin. Add a widget first!');
      }
    } catch (error) {
      Alert.alert('Error', 'Failed to spin widget');
    }
  };

  const handleClearCache = async () => {
    try {
      await SpinWheelWidget.clearCache();
      Alert.alert('Success', 'Cache cleared successfully!');
    } catch (error) {
      Alert.alert('Error', 'Failed to clear cache');
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor="#1a1a2e" />

      <View style={styles.content}>
        <Text style={styles.title}>Spin Wheel</Text>
        <Text style={styles.subtitle}>Widget Demo</Text>

        <View style={styles.iconContainer}>
          <Text style={styles.icon}>🎡</Text>
        </View>

        <View style={styles.statusCard}>
          <Text style={styles.statusLabel}>Widget Status</Text>
          <Text style={styles.statusCount}>{widgetCount}</Text>
          <Text style={styles.statusText}>
            {widgetCount === 1 ? 'Widget Active' : 'Widgets Active'}
          </Text>
        </View>

        <TouchableOpacity
          style={[styles.button, styles.primaryButton]}
          onPress={handleRefresh}
          disabled={isLoading}>
          <Text style={styles.buttonText}>
            {isLoading ? 'Refreshing...' : 'Refresh Widgets'}
          </Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.secondaryButton]}
          onPress={handleSpin}>
          <Text style={styles.buttonText}>Spin Widget</Text>
        </TouchableOpacity>

        <TouchableOpacity
          style={[styles.button, styles.outlineButton]}
          onPress={handleClearCache}>
          <Text style={[styles.buttonText, styles.outlineButtonText]}>
            Clear Cache
          </Text>
        </TouchableOpacity>

        <View style={styles.instructionsCard}>
          <Text style={styles.instructionsTitle}>How to Add Widget</Text>
          <Text style={styles.instructionStep}>
            1. Long press on your home screen
          </Text>
          <Text style={styles.instructionStep}>2. Tap 'Widgets'</Text>
          <Text style={styles.instructionStep}>
            3. Find 'Spin Wheel' widget
          </Text>
          <Text style={styles.instructionStep}>
            4. Drag it to your home screen
          </Text>
          <Text style={styles.instructionStep}>5. Tap the wheel to spin!</Text>
        </View>
      </View>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1a1a2e',
  },
  content: {
    flex: 1,
    padding: 24,
    alignItems: 'center',
    justifyContent: 'center',
  },
  title: {
    fontSize: 36,
    fontWeight: 'bold',
    color: '#ffffff',
  },
  subtitle: {
    fontSize: 18,
    color: '#e94560',
    marginBottom: 32,
  },
  iconContainer: {
    width: 120,
    height: 120,
    backgroundColor: '#0f3460',
    borderRadius: 24,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 32,
  },
  icon: {
    fontSize: 64,
  },
  statusCard: {
    backgroundColor: '#16213e',
    borderRadius: 16,
    padding: 20,
    width: '100%',
    alignItems: 'center',
    marginBottom: 24,
  },
  statusLabel: {
    fontSize: 16,
    color: '#9ca3af',
  },
  statusCount: {
    fontSize: 48,
    fontWeight: 'bold',
    color: '#e94560',
    marginVertical: 8,
  },
  statusText: {
    fontSize: 14,
    color: '#9ca3af',
  },
  button: {
    width: '100%',
    paddingVertical: 16,
    borderRadius: 12,
    alignItems: 'center',
    marginBottom: 12,
  },
  primaryButton: {
    backgroundColor: '#e94560',
  },
  secondaryButton: {
    backgroundColor: '#0f3460',
  },
  outlineButton: {
    backgroundColor: 'transparent',
    borderWidth: 1,
    borderColor: '#e94560',
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
  },
  outlineButtonText: {
    color: '#e94560',
  },
  instructionsCard: {
    backgroundColor: 'rgba(22, 33, 62, 0.5)',
    borderRadius: 16,
    padding: 20,
    width: '100%',
    marginTop: 24,
  },
  instructionsTitle: {
    fontSize: 16,
    fontWeight: '600',
    color: '#ffffff',
    marginBottom: 12,
  },
  instructionStep: {
    fontSize: 14,
    color: '#9ca3af',
    marginVertical: 4,
  },
});

export default App;
