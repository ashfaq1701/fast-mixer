package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import com.bluehub.fastmixer.common.models.AudioFileUiState
import com.bluehub.fastmixer.common.models.AudioViewAction
import com.bluehub.fastmixer.common.utils.*
import com.bluehub.fastmixer.common.viewmodel.BaseViewModel
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.File
import java.util.*
import javax.inject.Inject


class MixingScreenViewModel @Inject constructor(val context: Context,
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

            setCutToClipboard(::cutToClipboard)
            setCopyToClipboard(::copyToClipboard)
            setMuteAndCopyToClipboard(::muteAndCopyToClipboard)
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

        audioFileStore.run {
            if (audioFiles.filter {
                    it.path == filePath
                }.count() == 0) {
                viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        mixingRepository.addFile(filePath)
                        val totalSamples = getTotalSamples(filePath)
                        audioFiles.add(AudioFileUiState.create(filePath, totalSamples))
                    }
                    _audioFilesLiveData.value = audioFiles
                    _itemAddedIdx.value = audioFiles.size - 1
                }
            }
        }
    }

    fun addReadFile(fileUri: Uri) {
        val fileName = fileManager.getFileNameFromUri(context.contentResolver, fileUri)
        viewModelScope.launch(Dispatchers.IO) {
            val newFilePath = fileName?.let { name ->
                val fileInputStream = context.contentResolver.openInputStream(fileUri)
                fileInputStream?.use {
                    val dir = createImportedFileCacheDirectory()
                    val importId = UUID.randomUUID().toString()

                    fileManager.copyFileFromUri(it, name, dir, importId)
                }
            }

            newFilePath?.let { newPath ->
                mixingRepository.addFile(newPath)
                val totalSamples = getTotalSamples(newPath)

                audioFileStore.run {
                    audioFiles.add(AudioFileUiState.create(newPath, totalSamples))

                    withContext(Dispatchers.Main) {
                        _audioFilesLiveData.value = audioFiles
                        _itemAddedIdx.value = audioFiles.size - 1
                    }
                }
            }
        }
    }

    private fun createImportedFileCacheDirectory(): String {
        val cacheDir = "${context.cacheDir}/imported"
        val fileObj = File(cacheDir)
        if (!fileObj.exists()) {
            fileObj.mkdir()
        }
        return cacheDir
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
            val pathList = audioFileStore.audioFiles.map { it.path }
            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(pathList)
            }
            mixingRepository.startPlayback()
            playFlagStore.isGroupPlaying.postValue(true)
        }
    }

    private fun groupPause() {
        viewModelScope.launch(Dispatchers.IO) {
            mixingRepository.pausePlayback()
            playFlagStore.isGroupPlaying.postValue(false)
        }
    }

    fun toggleGroupPlay() {
        if (playFlagStore.isGroupPlaying.value == null || playFlagStore.isGroupPlaying.value == false) {
            groupPlay()
        } else {
            groupPause()
        }
    }

    private fun cutToClipboard(filePath: String) {
        val audioFileUiState = audioFileStore.findAudioFileByPath(filePath) ?: return

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

    private fun copyToClipboard(filePath: String) {
        val audioFileUiState = audioFileStore.findAudioFileByPath(filePath) ?: return

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

    private fun muteAndCopyToClipboard(filePath: String) {
        val audioFileUiState = audioFileStore.findAudioFileByPath(filePath) ?: return

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

    fun pasteAsNew() {

    }

    fun resetStates() {
        audioViewAction.value = null
    }

    override fun onCleared() {
        super.onCleared()
        mixingRepository.pausePlayback()
        mixingRepository.clearSources()
        mixingRepository.deleteMixingEngine()
        fileWaveViewStore.cleanup()
    }
}
