package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.models.AudioViewAction
import com.bluehub.fastmixer.common.utils.*
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MixingScreenViewModel @Inject constructor(@ApplicationContext val context: Context,
                                                    private val fileManager: FileManager,
                                                    private val mixingRepository: MixingRepository,
                                                    private val audioFileStore: AudioFileStore,
                                                    private val playFlagStore: PlayFlagStore,
                                                    val fileWaveViewStore: FileWaveViewStore)
    : BaseViewModel() {

    companion object {
        private lateinit var instance: MixingScreenViewModel

        fun setInstance(vmInstance: MixingScreenViewModel) {
            instance = vmInstance
        }

        @JvmStatic
        public fun setStopPlay() {
            if (::instance.isInitialized) {
                instance.pauseAudio()
                instance.setFilesPaused()
            }
        }
    }

    private var seekbarTimer: Timer? = null

    private val _isLoading: MutableLiveData<Boolean> = MutableLiveData()
    val isLoading: LiveData<Boolean>
        get() = _isLoading

    private val _audioFilesLiveData = MutableLiveData<MutableList<AudioFileUiState>>(mutableListOf())
    val audioFilesLiveData: LiveData<MutableList<AudioFileUiState>>
        get() = _audioFilesLiveData

    private val _eventDrawerOpen = MutableLiveData<Boolean>()
    val eventDrawerOpen: LiveData<Boolean>
        get() = _eventDrawerOpen

    private val _eventRecord = MutableLiveData<Boolean>()
    val eventRecord: LiveData<Boolean>
        get() = _eventRecord

    private val _eventRead = MutableLiveData<Boolean>()
    val eventRead: LiveData<Boolean>
        get() = _eventRead

    private val _itemRemovedIdx = MutableLiveData<Int>()
    val itemRemovedIdx: LiveData<Int>
        get() = _itemRemovedIdx

    private val _itemAddedIdx = MutableLiveData<Int>()
    val itemAddedIdx: LiveData<Int>
        get() = _itemAddedIdx

    val isPlaying: LiveData<Boolean>
        get() = playFlagStore.isPlaying

    val isGroupPlaying: LiveData<Boolean>
        get() = playFlagStore.isGroupPlaying

    private val _isGroupPlayOverlayOpen: MutableLiveData<Boolean> = MutableLiveData()
    val isGroupPlayOverlayOpen: LiveData<Boolean>
        get() = _isGroupPlayOverlayOpen

    val isGroupOverlayCancelEnabled = MediatorLiveData<Boolean>().apply {
        addSource(isGroupPlaying) { value -> setValue(!value) }
    }

    private val _groupPlaySeekbarProgress = MutableLiveData(0)
    val groupPlaySeekbarProgress: LiveData<Int>
        get() = _groupPlaySeekbarProgress

    private val _groupPlaySeekbarMaxValue = MutableLiveData<Int>(0)
    val groupPlaySeekbarMaxValue: LiveData<Int>
        get() = _groupPlaySeekbarMaxValue

    private val _clipboardHasData = MutableLiveData(false)
    val clipboardHasData: LiveData<Boolean>
        get() = _clipboardHasData

    private val playList: MutableList<String> = mutableListOf()

    val audioViewAction: MutableLiveData<AudioViewAction?> = MutableLiveData<AudioViewAction?>()

    init {
        mixingRepository.createMixingEngine()

        fileWaveViewStore.run {
            setAudioFilesLiveData(audioFilesLiveData)
            setIsPlayingLiveData(isPlaying)
            setIsGroupPlayingLiveData(isGroupPlaying)
            setClipboardHasDataLiveData(clipboardHasData)
            audioViewActionLiveData = audioViewAction
        }
    }

    fun onRecord() {
        _eventRecord.value = true
    }

    fun onReadFromDisk() {
        _eventRead.value = true
    }

    fun resetReadFromDisk() {
        _eventRead.value = false
    }

    fun onSaveToDisk() {

    }

    fun onRecordNavigated() {
        _eventRecord.value = false
    }

    fun resetItemAddedIdx() {
        _itemAddedIdx.value = null
    }

    fun toggleBottomDrawer() {
        _eventDrawerOpen.value = _eventDrawerOpen.value == null || _eventDrawerOpen.value == false
    }

    fun closeBottomDrawer() {
        if (_eventDrawerOpen.value == true) {
            _eventDrawerOpen.value = false
        }
    }

    fun addRecordedFilePath(filePath: String) {
        if (!fileManager.fileExists(filePath)) return
        val fd = fileManager.getFdForPath(filePath) ?: return

        audioFileStore.run {
            if (audioFiles.filter {
                    it.path == filePath
                }.count() == 0) {
                viewModelScope.launch {
                    _isLoading.value = true

                    withContext(Dispatchers.IO) {
                        mixingRepository.addFile(filePath, fd)
                        val totalSamples = getTotalSamples(filePath)
                        audioFiles.add(AudioFileUiState.create(filePath, totalSamples))
                    }
                    _audioFilesLiveData.value = audioFiles
                    _itemAddedIdx.value = audioFiles.size - 1

                    _isLoading.value = false
                }
            }
        }
    }

    fun addReadFile(fileUri: Uri) {
        val parcelFd = context.contentResolver.openFileDescriptor(fileUri, "r") ?: return

        val newFilePath = fileManager.getFileNameFromUri(context.contentResolver, fileUri)?.let {
            val ext = File(it).extension

            val uid = UUID.randomUUID().toString()
            "$uid.$ext"
        }

        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            newFilePath?.let { newPath ->
                mixingRepository.addFile(newPath, parcelFd.detachFd())

                val totalSamples = getTotalSamples(newPath)

                audioFileStore.run {
                    audioFiles.add(AudioFileUiState.create(newPath, totalSamples))

                    withContext(Dispatchers.Main) {
                        _audioFilesLiveData.value = audioFiles
                        _itemAddedIdx.value = audioFiles.size - 1
                    }
                }
            }

            _isLoading.postValue(false)
        }
    }

    fun readSamples(filePath: String) = fun(countPoints: Int): Deferred<Array<Float>> =
        viewModelScope.async {
            mixingRepository.readSamples(filePath, countPoints)
        }


    fun deleteFile(filePath: String) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                mixingRepository.deleteFile(filePath)
            }

            audioFileStore.run {
                val idxToRemove = audioFiles.foldIndexed(listOf<Int>(), { idx, list, file ->
                    if (file.path == filePath) {
                        list + idx
                    } else {
                        list
                    }
                })

                val firstIdx = idxToRemove.firstOrNull()

                firstIdx?.let {
                    val removedFile = audioFiles.removeAt(it)
                    fileManager.removeFile(removedFile.path)

                    playFlagStore.apply {
                        if (isPlaying.value == true) {
                            pauseAudio()
                        } else if (isGroupPlaying.value == true) {
                            groupPause()
                        }
                    }

                    _audioFilesLiveData.value = audioFiles
                    _itemRemovedIdx.value = it
                }
            }
        }
    }

    fun togglePlay(filePath: String) {
        if (playFlagStore.isPlaying.value == null || playFlagStore.isPlaying.value == false) {
            playAudioFile(filePath)
        } else {
            pauseAudio()
        }
    }

    private fun playAudioFile(filePath: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val pathList = listOf(filePath)

            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(listOf(filePath))
            }

            mixingRepository.startPlayback()
            playFlagStore.isPlaying.postValue(true)
        }
    }

    private fun pauseAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            mixingRepository.pausePlayback()
            playFlagStore.isPlaying.postValue(false)
        }
    }

    private fun setFilesPaused() {
        playList.forEach { filePath ->
            fileWaveViewStore.setFilePaused(filePath)
        }

        if (playFlagStore.isPlaying.value == true) {
            playFlagStore.isPlaying.postValue(false)
        }

        if (playFlagStore.isGroupPlaying.value == true) {
            playFlagStore.isGroupPlaying.postValue(false)
        }
    }

    fun resetItemRemovedIdx() {
        _itemRemovedIdx.value = null
    }


    private fun getTotalSamples(filePath: String): Int = mixingRepository.getTotalSamples(filePath)

    fun groupZoomIn() {
        fileWaveViewStore.groupZoomIn()
    }

    fun groupZoomOut() {
        fileWaveViewStore.groupZoomOut()
    }

    fun groupReset() {
        fileWaveViewStore.groupReset()
    }

    private fun groupPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            val pathList = audioFileStore.audioFiles.map { it.path }
            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(pathList)
            }
            startGroupPlay()
            playFlagStore.isGroupPlaying.postValue(true)

            _isLoading.postValue(false)

            _isGroupPlayOverlayOpen.postValue(true)
        }
    }

    private fun groupPause() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.postValue(true)

            pauseGroupPlay()
            playFlagStore.isGroupPlaying.postValue(false)

             _isLoading.postValue(false)
        }
    }

    fun startGroupPlay() {
        viewModelScope.launch(Dispatchers.IO) {

            if (_isLoading.value == false) {
                _isLoading.postValue(true)
            }

            mixingRepository.startPlayback()

            if (_isLoading.value == true) {
                _isLoading.postValue(false)
            }
        }
    }

    fun pauseGroupPlay() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isLoading.value == false) {
                _isLoading.postValue(true)
            }

            mixingRepository.pausePlayback()

            if (_isLoading.value == true) {
                _isLoading.postValue(false)
            }
        }
    }

    fun setPlayerHead(playHead: Int) = mixingRepository.setPlayerHead(playHead)

    fun toggleGroupPlay() {
        if (playFlagStore.isGroupPlaying.value == null || playFlagStore.isGroupPlaying.value == false) {
            groupPlay()
        } else {
            groupPause()
        }
    }

    fun cutToClipboard(audioFileUiState: AudioFileUiState) {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileUiState.run {
                audioFileUiState.isLoading.onNext(true)

                val playHeadPosition = mixingRepository.cutToClipboard(path, segmentStartSampleValue, segmentEndSampleValue)

                if (playHeadPosition >= 0) {
                    _clipboardHasData.postValue(true)

                    fileWaveViewStore.hideAndRemoveSegmentSelector(path)
                    fileWaveViewStore.recalculateAudioSegment(path, playHeadPosition)
                }

                audioFileUiState.isLoading.onNext(false)
            }
        }
    }

    fun copyToClipboard(audioFileUiState: AudioFileUiState) {

        viewModelScope.launch(Dispatchers.IO) {
            audioFileUiState.run {
                audioFileUiState.isLoading.onNext(true)

                val result = mixingRepository.copyToClipboard(path, segmentStartSampleValue, segmentEndSampleValue)

                if (result) {
                    _clipboardHasData.postValue(true)
                }

                audioFileUiState.isLoading.onNext(false)
            }
        }
    }

    fun muteAndCopyToClipboard(audioFileUiState: AudioFileUiState) {

        viewModelScope.launch(Dispatchers.IO) {

            audioFileUiState.run {
                audioFileUiState.isLoading.onNext(true)
                val result = mixingRepository.muteAndCopyToClipboard(path, segmentStartSampleValue, segmentEndSampleValue)

                if (result) {
                    reRender.onNext(true)

                    fileWaveViewStore.hideSegmentSelector(path)
                    _clipboardHasData.postValue(true)
                }

                audioFileUiState.isLoading.onNext(false)
            }
        }
    }

    fun pasteFromClipboard(audioFileUiState: AudioFileUiState) {
        viewModelScope.launch(Dispatchers.IO) {
            audioFileUiState.run {
                audioFileUiState.isLoading.onNext(true)
                mixingRepository.pasteFromClipboard(path, playSliderPositionSample)
                fileWaveViewStore.recalculateAudioSegment(path, playSliderPositionSample)
                audioFileUiState.isLoading.onNext(false)
            }
        }
    }

    fun pasteAsNew() {
        val fileId = UUID.randomUUID().toString()

        viewModelScope.launch {
            _isLoading.postValue(true)

            audioFileStore.run {
                withContext(Dispatchers.IO) {
                    mixingRepository.pasteNewFromClipboard(fileId)
                    val totalSamples = getTotalSamples(fileId)
                    audioFiles.add(AudioFileUiState.create(fileId, totalSamples))
                }
                _audioFilesLiveData.value = audioFiles
                _itemAddedIdx.value = audioFiles.size - 1
            }

            _isLoading.postValue(false)
        }
    }

    fun closeGroupPlayingOverlay() {
        _isGroupPlayOverlayOpen.value = false
    }

    fun applyCommonSegmentBounds() {
        _isGroupPlayOverlayOpen.value = false
    }

    fun resetStates() {
        audioViewAction.value = null
    }

    fun startGroupPlayTimer() {
        _groupPlaySeekbarProgress.value = 0
        _groupPlaySeekbarMaxValue.value = mixingRepository.getTotalSampleFrames()
        stopGroupPlayTimer()
        seekbarTimer = Timer()
        seekbarTimer?.schedule(object: TimerTask() {
            override fun run() {
                _groupPlaySeekbarProgress.postValue(mixingRepository.getCurrentPlaybackProgress())
            }
        }, 0, 10)
    }

    fun stopGroupPlayTimer() {
        seekbarTimer?.cancel()
        seekbarTimer = null
    }

    override fun onCleared() {
        super.onCleared()
        mixingRepository.pausePlayback()
        stopGroupPlayTimer()
        mixingRepository.clearSources()
        mixingRepository.deleteMixingEngine()
        fileWaveViewStore.cleanup()
    }
}
