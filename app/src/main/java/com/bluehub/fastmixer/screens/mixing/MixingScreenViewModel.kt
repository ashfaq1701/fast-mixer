package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.*
import kotlinx.coroutines.*
import java.io.File
import java.util.*
import javax.inject.Inject


class MixingScreenViewModel @Inject constructor(override val context: Context,
                                                override val permissionManager: PermissionManager,
                                                val fileManager: FileManager,
                                                val mixingRepository: MixingRepository,
                                                val fileWaveViewStore: FileWaveViewStore): PermissionViewModel(context) {
    var audioFiles: MutableList<AudioFile> = mutableListOf()
    val audioFilesLiveData = MutableLiveData<MutableList<AudioFile>>(mutableListOf())

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

    private val playList: MutableList<String> = mutableListOf()

    init {
        mixingRepository.createMixingEngine()
        fileWaveViewStore.setAudioFilesLiveData(audioFilesLiveData)
        fileWaveViewStore.setIsPlaying(isPlaying)
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
                audioFiles.add(AudioFile(newPath, totalSamples, AudioFileType.IMPORTED))

                withContext(Dispatchers.Main) {
                    audioFilesLiveData.value = audioFiles
                    _itemAddedIdx.value = audioFiles.size - 1
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

    fun togglePlay(filePath: String) {
        if (_isPlaying.value == null || _isPlaying.value == false) {
            playFile(filePath)
        } else {
            pauseFile()
        }
    }

    private fun playFile(filePath: String) {
        viewModelScope.launch {
            val pathList = listOf(filePath)
            if (!playList.areEqual(pathList)) {
                mixingRepository.loadFiles(pathList)
                playList.reInitList(listOf(filePath))
            }

            mixingRepository.startPlayback()
            _isPlaying.value = true
        }
    }

    private fun pauseFile() {
        viewModelScope.launch {
            mixingRepository.pausePlayback()
            _isPlaying.value = false
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

    override fun onCleared() {
        super.onCleared()
        mixingRepository.clearSources()
        mixingRepository.deleteMixingEngine()
        fileWaveViewStore.cleanup()
    }
}
