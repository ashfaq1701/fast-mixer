package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.models.AudioViewAction
import com.bluehub.fastmixer.common.utils.Optional
import dagger.hilt.android.scopes.ActivityRetainedScoped
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.*
import java.util.*
import javax.inject.Inject

@ActivityRetainedScoped
class FileWaveViewStore @Inject constructor(val mixingRepository: MixingRepository) {
    companion object {
        const val ZOOM_STEP = 1
    }

    private lateinit var mIsPlayingLiveData: LiveData<Boolean>
    private lateinit var mIsGroupPlayingLiveData: LiveData<Boolean>
    private lateinit var mClipboardHasDataLiveData: LiveData<Boolean>
    lateinit var audioViewActionLiveData: MutableLiveData<AudioViewAction?>

    val isPlayingObservable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)
    val isGroupPlayingObservable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val isAnyPlayingObservable = isPlayingObservable
        .flatMap {  isPlaying ->
            isGroupPlayingObservable.map { isGroupPlaying ->
                isPlaying || isGroupPlaying
            }
        }

    val clipboardHasDataObservable: BehaviorSubject<Boolean> = BehaviorSubject.createDefault(false)

    val isPasteEnabled = clipboardHasDataObservable
        .flatMap { hasClipboardData ->
            isAnyPlayingObservable.map { isAnyPlaying ->
                hasClipboardData && !isAnyPlaying
            }
        }

    private val measuredWidthObservable: BehaviorSubject<Int> = BehaviorSubject.create()

    private lateinit var mAudioFileUiStateListLiveData: LiveData<MutableList<AudioFileUiState>>

    private val audioFileUiStateList: List<AudioFileUiState>
        get() {
            return if (::mAudioFileUiStateListLiveData.isInitialized) {
                mAudioFileUiStateListLiveData.value ?: listOf()
            } else listOf()
        }

    val coroutineScope = CoroutineScope(Job() + Dispatchers.Main)

    private val fileListObserver: Observer<MutableList<AudioFileUiState>> = Observer {
        calculateSampleCountEachView()
    }

    private val isPlayingObserver: Observer<Boolean> = Observer {
        isPlayingObservable.onNext(it)
    }

    private val isGroupPlayingObserver: Observer<Boolean> = Observer {
        isGroupPlayingObservable.onNext(it)
    }

    private val clipboardHasDataObserver: Observer<Boolean> = Observer {
        clipboardHasDataObservable.onNext(it)
    }
    
    init {
        measuredWidthObservable.subscribe {
            updateDisplayPoints()
        }
    }

    fun setAudioFilesLiveData(audioFilesLiveData: LiveData<MutableList<AudioFileUiState>>) {
        mAudioFileUiStateListLiveData = audioFilesLiveData
        mAudioFileUiStateListLiveData.observeForever(fileListObserver)
    }

    fun setIsPlayingLiveData(isPlayingLiveData: LiveData<Boolean>) {
        mIsPlayingLiveData = isPlayingLiveData
        mIsPlayingLiveData.observeForever(isPlayingObserver)
    }

    fun setIsGroupPlayingLiveData(isGroupPlayingLiveData: LiveData<Boolean>) {
        mIsGroupPlayingLiveData = isGroupPlayingLiveData
        mIsGroupPlayingLiveData.observeForever(isGroupPlayingObserver)
    }

    fun setClipboardHasDataLiveData(clipboardHasDataLiveData: LiveData<Boolean>) {
        mClipboardHasDataLiveData = clipboardHasDataLiveData
        mClipboardHasDataLiveData.observeForever(clipboardHasDataObserver)
    }

    fun updateMeasuredWidth(width: Int) {
        if ((!measuredWidthObservable.hasValue() || measuredWidthObservable.value != width) && width > 0) {
            measuredWidthObservable.onNext(width)
        }
    }
    
    private fun calculateSampleCountEachView() {
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

            val newSliderPos = recalculatePlaySliderPositionByDisplayPtsCount(audioFileUiState, numPts.toInt())
            setPlaySliderPosition(audioFileUiState, newSliderPos)

            audioFileUiState.displayPtsCount.onNext(numPts.toInt())

        }
    }

    private fun findAudioFileUiState(audioFilePath: String): AudioFileUiState? {
        return audioFileUiStateList.find {
            it.path == audioFilePath
        }
    }
    
    private fun getSampleCount(filePath: String): Int? {
        val audioFileUiState = findAudioFileUiState(filePath)
        return audioFileUiState?.displayPtsCount?.value
    }

    private fun getZoomLevel(filePath: String): Int? {
        val audioFileUiState = findAudioFileUiState(filePath)
        return audioFileUiState?.zoomLevelValue
    }

    private fun recalculatePlaySliderPositionByZoomLevel(audioFileUiState: AudioFileUiState, newZoomLevel: Int): Int {
        return audioFileUiState.run {
            (playSliderPositionValue.toFloat() / zoomLevelValue.toFloat()) * newZoomLevel
        }.toInt()
    }

    private fun recalculatePlaySliderPositionByDisplayPtsCount(audioFileUiState: AudioFileUiState, newDisplayPtsCount: Int): Int {
        if (audioFileUiState.displayPtsCountValue == 0) return 0
        return audioFileUiState.run {
            (playSliderPositionValue.toFloat() / displayPtsCountValue.toFloat()) * newDisplayPtsCount
        }.toInt()
    }

    private fun setZoomLevel(audioFileUiState: AudioFileUiState, zoomLevel: Int) {
        val newSliderPos = recalculatePlaySliderPositionByZoomLevel(audioFileUiState, zoomLevel)
        setPlaySliderPosition(audioFileUiState, newSliderPos)
        audioFileUiState.zoomLevel.onNext(zoomLevel)
    }

    fun zoomIn(audioFileUiState: AudioFileUiState): Boolean {
        val zoomLevel = getZoomLevel(audioFileUiState.path)
        val numSamples = getSampleCount(audioFileUiState.path)

        if (zoomLevel == null || numSamples == null) {
            return false
        }

        return (zoomLevel * numSamples < audioFileUiState.numSamples).also {
            findAudioFileUiState(audioFileUiState.path)?.let {
                setZoomLevel(it, zoomLevel + ZOOM_STEP)
            }
        }
    }

    fun zoomOut(audioFileUiState: AudioFileUiState): Boolean {
        val zoomLevel = getZoomLevel(audioFileUiState.path) ?: return false

        val newZoomLevel = if (zoomLevel >= 1 + ZOOM_STEP) zoomLevel - ZOOM_STEP else zoomLevel

        if (newZoomLevel != zoomLevel) {
            findAudioFileUiState(audioFileUiState.path)?.let {
                setZoomLevel(it, newZoomLevel)
            }
            return true
        }
        return false
    }

    fun resetZoomLevel(filePath: String) {
        findAudioFileUiState(filePath)?.let {
            setZoomLevel(it, 1)
        }
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
            setZoomLevel(it, zoomLevel)
        }
    }

    fun setFilePaused(filePath: String) {
        val audioFileUiState = findAudioFileUiState(filePath)
        audioFileUiState?.let { setPaused(it) }

    }

    fun togglePlayFlag(filePath: String) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return
        if (!audioFileUiState.isPlaying.hasValue()
            || !audioFileUiState.isPlaying.value) {
            setPlaying(audioFileUiState)
        } else {
            setPaused(audioFileUiState)
        }
    }

    private fun getSliderPosition(audioFileUiState: AudioFileUiState) = audioFileUiState.run {
        val perSample = mixingRepository.getCurrentPlaybackProgress().toFloat() / numSamples.toFloat()
        (perSample * numPtsToPlot).toInt()
    }

    private fun setPlaying(audioFileUiState: AudioFileUiState) {
        audioFileUiState.isPlaying.onNext(true)
        val playTimer = Timer()
        playTimer.schedule(object: TimerTask() {
            override fun run() {
                val sliderPos = getSliderPosition(audioFileUiState)
                setPlaySliderPosition(audioFileUiState, sliderPos)
            }
        }, 0, 10)
        audioFileUiState.playTimer = playTimer
    }

    private fun setPaused(audioFileUiState: AudioFileUiState) {
        if (audioFileUiState.isPlaying.hasValue() && audioFileUiState.isPlaying.value == true) {
            audioFileUiState.isPlaying.onNext(false)
            audioFileUiState.playTimer?.cancel()
            audioFileUiState.playTimer = null

            val sliderPos = getSliderPosition(audioFileUiState)
            setPlaySliderPosition(audioFileUiState, sliderPos)

            audioFileUiState.calculatePlaySliderPositionMs()
        }
    }

    fun setPlayHead(filePath: String, playHeadPointer: Int) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return

        if (audioFileUiState.displayPtsCountValue == 0) return

        val playHead = audioFileUiState.run {
            (playHeadPointer.toFloat() / numPtsToPlot.toFloat()) * numSamples.toFloat()
        }

        if (!audioFileUiState.isPlaying.hasValue() || !audioFileUiState.isPlaying.value) {
            mixingRepository.setSourcePlayHead(filePath, playHead.toInt())
        } else {
            mixingRepository.setPlayerHead(playHead.toInt())
        }
        setPlaySliderPosition(audioFileUiState, playHeadPointer)

        audioFileUiState.calculatePlaySliderPositionMs()
    }

    fun reRenderFile(filePath: String) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return
        audioFileUiState.reRender.onNext(true)
    }

    private fun setPlaySliderPosition(audioFileUiState: AudioFileUiState, playSliderPosition: Int) {
        audioFileUiState.setPlaySliderPosition(playSliderPosition)
    }

    fun setSourceBounds(filePath: String) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return

        audioFileUiState.run {
            mixingRepository.setSourceBounds(filePath, segmentStartSampleValue, segmentEndSampleValue)
        }
    }

    fun resetSourceBounds(filePath: String) {
        findAudioFileUiState(filePath) ?: return

        mixingRepository.resetSourceBounds(filePath)
    }

    fun hideSegmentSelector(filePath: String) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return
        audioFileUiState.showSegmentSelector.onNext(false)

    }

    fun hideAndRemoveSegmentSelector(filePath: String) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return

        hideSegmentSelector(filePath)

        audioFileUiState.run {
            segmentStartSample.onNext(Optional.empty())
            segmentEndSample.onNext(Optional.empty())
        }
    }

    fun recalculateAudioSegment(filePath: String, playSliderPositionSamples: Int? = null) {
        val audioFileUiState = findAudioFileUiState(filePath) ?: return

        audioFileUiState.run {
            numSamples = mixingRepository.getTotalSamples(filePath)

            reRender.onNext(true)

            calculateSampleCountEachView()

            playSliderPositionSamples?.let {
                val playSliderPos = (it.toFloat() / numSamples.toFloat()) * numPtsToPlot.toFloat()
                setPlayHead(path, playSliderPos.toInt())
            }
        }
    }

    fun cleanup() {
        if (::mAudioFileUiStateListLiveData.isInitialized) {
            mAudioFileUiStateListLiveData.removeObserver(fileListObserver)
        }
        mIsPlayingLiveData.removeObserver(isPlayingObserver)
        coroutineScope.cancel()
    }
}
