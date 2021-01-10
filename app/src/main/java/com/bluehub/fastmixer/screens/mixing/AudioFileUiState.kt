package com.bluehub.fastmixer.screens.mixing

import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.io.Serializable

data class AudioFileUiState(
    val path: String,
    val numSamples: Int,
    var displayPtsCount: BehaviorSubject<Int>,
    var zoomLevel: BehaviorSubject<Int>) : Serializable {
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
    }
