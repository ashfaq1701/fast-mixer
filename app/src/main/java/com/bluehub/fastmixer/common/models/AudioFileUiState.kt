package com.bluehub.fastmixer.common.models

import com.bluehub.fastmixer.common.config.Config
import com.bluehub.fastmixer.common.utils.Optional
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
    var segmentStartSample: BehaviorSubject<Optional<Int>>,
    var segmentEndSample: BehaviorSubject<Optional<Int>>,
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
                segmentStartSample = BehaviorSubject.createDefault(Optional.empty()),
                segmentEndSample = BehaviorSubject.createDefault(Optional.empty()),
                playTimer = null
            )
        }
    }

        val playSliderPositionMs: BehaviorSubject<Int> = BehaviorSubject.createDefault(0)

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

        val numSamplesMs: Int
            get() {
                return (numSamples.toFloat() / (Config.SAMPLE_RATE.toFloat() / 1000.0)).toInt()
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
                return segmentStartSample.value.value ?. let {
                    ((it.toDouble() / numSamples.toDouble()) * numPtsToPlot).toInt()
                } ?: 0
            }

        val segmentSelectorRight : Int
            get() {
                if (numSamples == 0) return 0
                return segmentEndSample.value.value ?. let {
                    ((it.toDouble() / numSamples.toDouble()) * numPtsToPlot).toInt().coerceAtMost(numPtsToPlot - 1)
                } ?: 0
            }

        val segmentStartSampleMs: Int?
            get() {
                return segmentStartSample.value.value ?. let {
                    (it.toFloat() / (Config.SAMPLE_RATE.toFloat() / 1000.0)).toInt()
                }
            }

        val segmentEndSampleMs: Int?
            get() {
                return segmentEndSample.value.value ?. let {
                    (it.toFloat() / (Config.SAMPLE_RATE.toFloat() / 1000.0)).toInt()
                }
            }

        fun calculatePlaySliderPositionMs() {
            val playHeadMs = playHeadSample.toFloat() / (Config.SAMPLE_RATE.toFloat() / 1000.0)
            playSliderPositionMs.onNext(playHeadMs.toInt())
        }

        fun setSegmentStartInMs(segmentStartInMs: Int) {
            val segmentStart = segmentStartInMs * (Config.SAMPLE_RATE / 1000)
            segmentStartSample.onNext(Optional.of(segmentStart))
        }

        fun setSegmentEndInMs(segmentEndInMs: Int) {
            val segmentEnd = segmentEndInMs * (Config.SAMPLE_RATE / 1000)
            segmentEndSample.onNext(Optional.of(segmentEnd))
        }
    }
