package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.*
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import javax.inject.Inject

class FileWaveViewStore @Inject constructor() {
    companion object {
        const val ZOOM_STEP = 1
    }

    private lateinit var mAudioFilesLiveData: LiveData<MutableList<AudioFile>>

    private lateinit var mIsPlayingLiveData: LiveData<Boolean>
    val isPlayingObservable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    private val measuredWidthObservable: BehaviorSubject<Int> = BehaviorSubject.create()

    private var audioFileUiStateList: MutableList<AudioFileUiState> = mutableListOf()
    val audioFileUiStateListLiveData =
        MutableLiveData<MutableList<AudioFileUiState>>(mutableListOf())

    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    private val fileListObserver: Observer<MutableList<AudioFile>> = Observer {
        calculateSampleCountEachView()
    }

    private val isPlayingObserver: Observer<Boolean> = Observer {
        isPlayingObservable.onNext(it)
    }
    
    init {
        measuredWidthObservable.subscribe {
            updateDisplayPoints()
        }
    }

    fun setAudioFilesLiveData(audioFilesLiveData: LiveData<MutableList<AudioFile>>) {
        mAudioFilesLiveData = audioFilesLiveData
        mAudioFilesLiveData.observeForever(fileListObserver)
    }

    fun setIsPlaying(isPlayingLiveData: LiveData<Boolean>) {
        mIsPlayingLiveData = isPlayingLiveData
        mIsPlayingLiveData.observeForever(isPlayingObserver)
    }

    fun updateMeasuredWidth(width: Int) {
        if ((!measuredWidthObservable.hasValue() || measuredWidthObservable.value != width) && width > 0) {
            measuredWidthObservable.onNext(width)
        }
    }
    
    private fun calculateSampleCountEachView() {
        val audioFiles = mAudioFilesLiveData.value ?: return

        audioFileUiStateList.filter {
            findAudioFile(it.path) == null
        }.onEach {
            audioFileUiStateList.remove(it)
        }

        audioFiles.forEach { audioFile ->
            findAudioFileUiState(audioFile.path) ?: let {
                val audioFileUiState = AudioFileUiState(
                    path = audioFile.path,
                    numSamples = audioFile.numSamples,
                    displayPtsCount = BehaviorSubject.create(),
                    zoomLevel = BehaviorSubject.create(),
                    isPlaying = BehaviorSubject.createDefault(false)
                )
                audioFileUiStateList.add(audioFileUiState)
            }
        }

        audioFileUiStateListLiveData.value = audioFileUiStateList
        updateDisplayPoints()
    }

    private fun updateDisplayPoints() {
        val measuredWidth = if (measuredWidthObservable.hasValue()) {
            measuredWidthObservable.value
        } else 0

        val maxNumSamples = audioFileUiStateList.fold(0) { maxSamples, audioFile ->
            if (maxSamples < audioFile.numSamples) {
                audioFile.numSamples
            } else maxSamples
        }

        audioFileUiStateList.forEach { audioFileUiState ->
            val numPts = (audioFileUiState.numSamples.toFloat() / maxNumSamples.toFloat()) * measuredWidth
            audioFileUiState.displayPtsCount.onNext(numPts.toInt())
        }
    }

    private fun findAudioFile(audioFilePath: String): AudioFile? {
        return mAudioFilesLiveData.value?.find {
            it.path == audioFilePath
        }
    }

    private fun findAudioFileUiState(audioFilePath: String) = audioFileUiStateList.find {
        it.path == audioFilePath
    }
    
    private fun getSampleCount(filePath: String): Int? {
        val audioFileUiState = findAudioFileUiState(filePath)
        return audioFileUiState?.displayPtsCount?.value
    }

    private fun getZoomLevel(filePath: String): Int? {
        val audioFileUiState = findAudioFileUiState(filePath)
        return audioFileUiState?.zoomLevelValue
    }

    fun zoomIn(audioFileUiState: AudioFileUiState): Boolean {
        val zoomLevel = getZoomLevel(audioFileUiState.path)
        val numSamples = getSampleCount(audioFileUiState.path)

        if (zoomLevel == null || numSamples == null) {
            return false
        }

        return (zoomLevel * numSamples < audioFileUiState.numSamples).also {
            findAudioFileUiState(audioFileUiState.path)?.zoomLevel?.onNext(zoomLevel + ZOOM_STEP)
        }
    }

    fun zoomOut(audioFileUiState: AudioFileUiState): Boolean {
        val zoomLevel = getZoomLevel(audioFileUiState.path) ?: return false

        val newZoomLevel = if (zoomLevel >= 1 + ZOOM_STEP) zoomLevel - ZOOM_STEP else zoomLevel

        if (newZoomLevel != zoomLevel) {
            findAudioFileUiState(audioFileUiState.path)?.zoomLevel?.onNext(newZoomLevel)
            return true
        }
        return false
    }

    fun resetZoomLevel(filePath: String) {
        findAudioFileUiState(filePath)?.zoomLevel?.onNext(1)
    }

    fun groupZoomIn() {
        val min = getMinZoomLevel()
        if (!checkIfZoomLevelIsMaxAllowed(min)) {
            groupSetZoomLevel(min + ZOOM_STEP)
        } else {
            groupSetZoomLevel(min)
        }
    }

    fun groupZoomOut() {
        val min = getMinZoomLevel()
        if (min >= 1 + ZOOM_STEP) {
            groupSetZoomLevel(min - ZOOM_STEP)
        } else {
            groupSetZoomLevel(min)
        }
    }

    private fun getMinZoomLevel(): Int {
        return audioFileUiStateList.fold(Int.MAX_VALUE, { acc: Int, curr: AudioFileUiState ->
            if (curr.zoomLevelValue < acc) curr.zoomLevelValue else acc
        })
    }

    private fun checkIfZoomLevelIsMaxAllowed(zl: Int): Boolean {
        var ifMaxAllowed = false
        audioFileUiStateList.forEach { audioFileUiState ->
            audioFileUiState.apply {
                if (zoomLevelValue == zl) {
                    ifMaxAllowed = ifMaxAllowed ||
                        (zoomLevelValue * displayPtsCountValue >= numSamples)
                }
            }
        }
        return ifMaxAllowed
    }

    fun groupReset() {
        groupSetZoomLevel(1)
    }

    private fun groupSetZoomLevel(zoomLevel: Int) {
        audioFileUiStateList.forEach {
            it.zoomLevel.onNext(zoomLevel)
        }
    }

    fun cleanup() {
        mAudioFilesLiveData.removeObserver(fileListObserver)
        mIsPlayingLiveData.removeObserver(isPlayingObserver)
        coroutineScope.cancel()
    }
}
