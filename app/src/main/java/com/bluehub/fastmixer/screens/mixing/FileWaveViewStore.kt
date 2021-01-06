package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class FileWaveViewStore @Inject constructor() {
    companion object {
        const val ZOOM_STEP = 1
    }

    private lateinit var mAudioFilesLiveData: LiveData<MutableList<AudioFile>>

    private val measuredWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    private val fileSampleCountMap: MutableMap<String, Int> = mutableMapOf()
    val isFileSampleCountMapUpdated: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val fileListObserver: Observer<MutableList<AudioFile>> = Observer {
        calculateSampleCountEachView()
    }

    private val fileZoomLevels: MutableMap<String, Int> = mutableMapOf()
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
            fileSampleCountMap[audioFile.path] = numSamples.toInt()

            if (!fileZoomLevels.containsKey(audioFile.path)) {
                fileZoomLevels[audioFile.path] = 1
            }
        }

        isFileSampleCountMapUpdated.onNext(true)
    }
    
    fun getSampleCount(fileName: String): Int? {
        return fileSampleCountMap[fileName]
    }

    fun getZoomLevel(filePath: String): Int? {
        return fileZoomLevels[filePath]
    }

    fun zoomIn(audioFile: AudioFile): Boolean {
        val zoomLevel = getZoomLevel(audioFile.path)
        val numSamples = getSampleCount(audioFile.path)

        if (zoomLevel == null || numSamples == null) {
            return false
        }

        return (zoomLevel * numSamples < audioFile.numSamples).also {
            fileZoomLevels[audioFile.path] = zoomLevel + ZOOM_STEP
        }
    }

    fun zoomOut(audioFile: AudioFile): Boolean {
        val zoomLevel = getZoomLevel(audioFile.path) ?: return false

        val newZoomLevel = if (zoomLevel >= 1 + ZOOM_STEP) zoomLevel - ZOOM_STEP else zoomLevel

        if (newZoomLevel != zoomLevel) {
            fileZoomLevels[audioFile.path] = newZoomLevel
            return true
        }
        return false
    }

    fun resetZoomLevel(filePath: String) {
        fileZoomLevels[filePath] = 1
    }

    fun removeLivedataObservers() {
        mAudioFilesLiveData.removeObserver(fileListObserver)
    }
}