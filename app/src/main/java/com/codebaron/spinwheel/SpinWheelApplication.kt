package com.codebaron.spinwheel

import android.app.Application
import com.codebaron.spinwheel.widget.SpinWheelWidget

class SpinWheelApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize the SpinWheel Widget library
        SpinWheelWidget.initialize(this)
    }
}
