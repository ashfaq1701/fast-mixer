package com.bluehub.fastmixer.common.models

import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.Serializable
import java.util.*

data class AudioFileUiState(
    val path: String,
    val numSamples: Int,
    var displayPtsCount: BehaviorSubject<Int>,
    var zoomLevel: BehaviorSubject<Int>,
    var isPlaying: BehaviorSubject<Boolean>,
    var playSliderPosition: BehaviorSubject<Int>,
    var playTimer: Timer?) : Serializable {

        val reRender: BehaviorSubject<Boolean> = BehaviorSubject.create()

        val playSliderPositionValue: Int
            get() = if (playSliderPosition.hasValue()) {
                playSliderPosition.value
            } else 0

        val zoomLevelValue: Int
            get() = if (zoomLevel.hasValue()) {
                zoomLevel.value
            } else {
                1
            }

        val displayPtsCountValue: Int
            get() = if (displayPtsCount.hasValue()) {
                displayPtsCount.value
            } else {
                0
            }

        val numPtsToPlot: Int
            get() {
                return (zoomLevelValue * displayPtsCountValue).coerceAtMost(numSamples)
            }
    }
