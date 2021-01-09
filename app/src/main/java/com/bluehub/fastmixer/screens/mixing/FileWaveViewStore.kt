package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import javax.inject.Inject

class FileWaveViewStore @Inject constructor() {
    companion object {
        const val ZOOM_STEP = 1
    }

    private lateinit var mAudioFilesLiveData: LiveData<MutableList<AudioFile>>

    private val measuredWidth: BehaviorSubject<Int> = BehaviorSubject.create()

    private val audioFileUiStateList: MutableList<AudioFileUiState> = mutableListOf()

    val isFileSampleCountMapUpdated: BehaviorSubject<Boolean> = BehaviorSubject.create()

    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    private val fileListObserver: Observer<MutableList<AudioFile>> = Observer {
        calculateSampleCountEachView()
    }

    val fileZoomLevelsUpdated: BehaviorSubject<Boolean> = BehaviorSubject.create()
    
    init {
        measuredWidth.subscribe { 
            calculateSampleCountEachView()
        }

        fileZoomLevelsUpdated.onNext(true)
    }

    fun setAudioFilesLiveData(audioFilesLiveData: LiveData<MutableList<AudioFile>>) {
        mAudioFilesLiveData = audioFilesLiveData
        mAudioFilesLiveData.observeForever(fileListObserver)
    }

    fun updateMeasuredWidth(width: Int) {
        if ((!measuredWidth.hasValue() || measuredWidth.value != width) && width > 0) {
            measuredWidth.onNext(width)
        }
    }
    
    private fun calculateSampleCountEachView() {
        if (mAudioFilesLiveData.value == null || !measuredWidth.hasValue()) {
            return
        }

        val maxNumSamples = mAudioFilesLiveData.value!!.fold(0) { maxSamples, audioFile ->
            if (maxSamples < audioFile.numSamples) {
                audioFile.numSamples
            } else maxSamples
        }

        mAudioFilesLiveData.value!!.forEach { audioFile ->
            val numSamples = (audioFile.numSamples.toFloat() / maxNumSamples.toFloat()) * measuredWidth.value

            if (findAudioFileUiState(audioFile.path) == null) {
                val audioFileUiState = AudioFileUiState(
                    path = audioFile.path,
                    numSamples = audioFile.numSamples,
                    displayPtsCount = numSamples.toInt(),
                    zoomLevel = 1
                )
                audioFileUiStateList.add(audioFileUiState)
            }
        }

        isFileSampleCountMapUpdated.onNext(true)
    }

    private fun findAudioFileUiState(audioFilePath: String) = audioFileUiStateList.find {
        it.path == audioFilePath
    }
    
    fun getSampleCount(filePath: String): Int? {
        val audioFileUiState = findAudioFileUiState(filePath)
        return audioFileUiState?.displayPtsCount
    }

    fun getZoomLevel(filePath: String): Int? {
        val audioFileUiState = findAudioFileUiState(filePath)
        return audioFileUiState?.zoomLevel
    }

    fun zoomIn(audioFile: AudioFile): Boolean {
        val zoomLevel = getZoomLevel(audioFile.path)
        val numSamples = getSampleCount(audioFile.path)

        if (zoomLevel == null || numSamples == null) {
            return false
        }

        return (zoomLevel * numSamples < audioFile.numSamples).also {
            findAudioFileUiState(audioFile.path)?.zoomLevel = zoomLevel + ZOOM_STEP
        }
    }

    fun zoomOut(audioFile: AudioFile): Boolean {
        val zoomLevel = getZoomLevel(audioFile.path) ?: return false

        val newZoomLevel = if (zoomLevel >= 1 + ZOOM_STEP) zoomLevel - ZOOM_STEP else zoomLevel

        if (newZoomLevel != zoomLevel) {
            findAudioFileUiState(audioFile.path)?.zoomLevel = newZoomLevel
            return true
        }
        return false
    }

    fun resetZoomLevel(filePath: String) {
        findAudioFileUiState(filePath)?.zoomLevel = 1
    }

    fun cleanup() {
        mAudioFilesLiveData.removeObserver(fileListObserver)
        coroutineScope.cancel()
    }

    fun groupZoomIn() {
        val min = getMinZoomLevel()
        if (!checkIfZoomLevelIsMaxAllowed(min)) {
            groupSetZoomLevel(min + ZOOM_STEP)
        } else {
            groupSetZoomLevel(min)
        }
        fileZoomLevelsUpdated.onNext(true)
    }

    fun groupZoomOut() {
        val min = getMinZoomLevel()
        if (min >= 1 + ZOOM_STEP) {
            groupSetZoomLevel(min - ZOOM_STEP)
        } else {
            groupSetZoomLevel(min)
        }
        fileZoomLevelsUpdated.onNext(true)
    }

    private fun getMinZoomLevel(): Int {
        return audioFileUiStateList.fold(Int.MAX_VALUE, { acc: Int, curr: AudioFileUiState ->
            if (curr.zoomLevel < acc) curr.zoomLevel else acc
        })
    }

    private fun checkIfZoomLevelIsMaxAllowed(zl: Int): Boolean {
        var ifMaxAllowed = false
        audioFileUiStateList.forEach { audioFileUiState ->
            audioFileUiState.apply {
                if (zoomLevel == zl) {
                    ifMaxAllowed = ifMaxAllowed || (zoomLevel * displayPtsCount >= numSamples)
                }
            }
        }
        return ifMaxAllowed
    }

    fun groupReset() {
        groupSetZoomLevel(1)
        fileZoomLevelsUpdated.onNext(true)
    }

    private fun groupSetZoomLevel(zoomLevel: Int) {
        audioFileUiStateList.forEach {
            it.zoomLevel = zoomLevel
        }
    }
}
