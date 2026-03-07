package com.codebaron.spinwheel.widget.domain.usecase

import com.codebaron.spinwheel.widget.domain.model.SpinResult
import com.codebaron.spinwheel.widget.domain.model.WheelRotation
import kotlin.random.Random

class CalculateSpinResultUseCase {

    operator fun invoke(
        currentRotation: Float,
        rotation: WheelRotation,
        numberOfSegments: Int = 8
    ): SpinResult {
        // Random number of full spins between min and max
        val spins = Random.nextInt(rotation.minimumSpins, rotation.maximumSpins + 1)

        // Random extra degrees for final position (0-360)
        val extraDegrees = Random.nextFloat() * 360f

        // Total rotation
        val totalRotation = (spins * 360f) + extraDegrees
        val finalRotation = currentRotation + totalRotation

        // Calculate which segment the wheel landed on
        val normalizedRotation = finalRotation % 360f
        val segmentAngle = 360f / numberOfSegments
        val segment = ((360f - normalizedRotation) / segmentAngle).toInt() % numberOfSegments

        return SpinResult(
            finalRotation = finalRotation,
            totalSpins = spins,
            segment = segment
        )
    }
}
