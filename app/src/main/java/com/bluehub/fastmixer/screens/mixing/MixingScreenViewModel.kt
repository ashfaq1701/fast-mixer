package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.common.models.AudioViewAction
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.*
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject


class MixingScreenViewModel @Inject constructor(override val context: Context,
                                                override val permissionManager: PermissionManager,
                                                private val fileManager: FileManager,
                                                private val mixingRepository: MixingRepository,
                                                private val audioFileStore: AudioFileStore,
                                                val fileWaveViewStore: FileWaveViewStore)
    : PermissionViewModel(context) {

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

    private val audioFilesLiveData = MutableLiveData<MutableList<AudioFile>>(mutableListOf())

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

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean>
        get() = _isPlaying

    private val _isGroupPlaying = MutableLiveData<Boolean>()
    val isGroupPlaying: LiveData<Boolean>
        get() = _isGroupPlaying

    private val playList: MutableList<String> = mutableListOf()

    val audioViewAction: MutableLiveData<AudioViewAction?> = MutableLiveData<AudioViewAction?>()

    init {
        mixingRepository.createMixingEngine()

        fileWaveViewStore.setAudioFilesLiveData(audioFilesLiveData)
        fileWaveViewStore.setIsPlayingLiveData(isPlaying)
        fileWaveViewStore.setIsGroupPlayingLiveData(isGroupPlaying)
        fileWaveViewStore.audioViewActionLiveData = audioViewAction

        fileWaveViewStore.setCurrentPlaybackProgressGetter { getCurrentPlaybackProgress() }
        fileWaveViewStore.setPlayerHeadSetter { playHead: Int -> setPlayerHead(playHead) }
        fileWaveViewStore.setSourcePlayHeadSetter { filePath: String, playHead: Int ->
            setSourcePlayHead(filePath, playHead)
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
                        audioFiles.add(AudioFile(filePath, totalSamples, AudioFileType.RECORDED))
                    }
                    audioFilesLiveData.value = audioFiles
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
                    audioFiles.add(AudioFile(newPath, totalSamples, AudioFileType.IMPORTED))

                    withContext(Dispatchers.Main) {
                        audioFilesLiveData.value = audioFiles
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
                    audioFilesLiveData.value = audioFiles
                    _itemRemovedIdx.value = it
                }
            }
        }
    }

    fun togglePlay(filePath: String) {
        if (_isPlaying.value == null || _isPlaying.value == false) {
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
            _isPlaying.postValue(true)
        }
    }

    private fun pauseAudio() {
        viewModelScope.launch(Dispatchers.IO) {
            mixingRepository.pausePlayback()
            _isPlaying.postValue(false)
        }
    }

    private fun setFilesPaused() {
        playList.forEach { filePath ->
            fileWaveViewStore.setFilePaused(filePath)
        }

        if (_isGroupPlaying.value == true) {
            _isGroupPlaying.postValue(false)
        }
    }

    fun resetItemRemovedIdx() {
        _itemRemovedIdx.value = null
    }


    private fun getTotalSamples(filePath: String): Int = mixingRepository.getTotalSamples(filePath)

    private fun getCurrentPlaybackProgress(): Int = mixingRepository.getCurrentPlaybackProgress()

    private fun setPlayerHead(playHead: Int) = mixingRepository.setPlayerHead(playHead)

    private fun setSourcePlayHead(filePath: String, playHead: Int) =
        mixingRepository.setSourcePlayHead(filePath, playHead)

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
            _isGroupPlaying.postValue(true)
        }
    }

    private fun groupPause() {
        viewModelScope.launch(Dispatchers.IO) {
            mixingRepository.pausePlayback()
            _isGroupPlaying.postValue(false)
        }
    }

    fun toggleGroupPlay() {
        if (_isGroupPlaying.value == null || _isGroupPlaying.value == false) {
            groupPlay()
        } else {
            groupPause()
        }
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
