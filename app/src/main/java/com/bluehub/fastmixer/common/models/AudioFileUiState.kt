package com.bluehub.fastmixer.common.models

import com.bluehub.fastmixer.common.config.Config
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
    var showSegmentSelector: BehaviorSubject<Boolean>,
    var segmentStartSample: Int?,
    var segmentEndSample: Int?,
    var playTimer: Timer?) : Serializable {

    companion object {
        fun create(filePath: String, numSamples: Int): AudioFileUiState {
            return AudioFileUiState(
                path = filePath,
                numSamples = numSamples,
                displayPtsCount = BehaviorSubject.create(),
                zoomLevel = BehaviorSubject.create(),
                isPlaying = BehaviorSubject.createDefault(false),
                playSliderPosition = BehaviorSubject.createDefault(0),
                showSegmentSelector = BehaviorSubject.createDefault(false),
                segmentStartSample = null,
                segmentEndSample = null,
                playTimer = null
            )
        }
    }

        val playSliderPositionMs = BehaviorSubject.createDefault(0)

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

        private val playHeadSample: Int
            get() {
                return ((playSliderPosition.value.toFloat() / numPtsToPlot.toFloat()) * numSamples).toInt()
            }

        val numPtsToPlot: Int
            get() {
                return (zoomLevelValue * displayPtsCountValue).coerceAtMost(numSamples)
            }

        val segmentSelectorLeft : Int
            get() {
                if (numSamples == 0) return 0
                return segmentStartSample ?. let {
                    ((it.toDouble() / numSamples.toDouble()) * numPtsToPlot).toInt()
                } ?: 0
            }

        val segmentSelectorRight : Int
            get() {
                if (numSamples == 0) return 0
                return segmentEndSample ?. let {
                    ((it.toDouble() / numSamples.toDouble()) * numPtsToPlot).toInt().coerceAtMost(numPtsToPlot - 1)
                } ?: 0
            }

        fun calculatePlaySliderPositionMs() {
            val playHeadMs = playHeadSample.toFloat() / (Config.SAMPLE_RATE.toFloat() / 1000.0)
            playSliderPositionMs.onNext(playHeadMs.toInt())
        }
    }
