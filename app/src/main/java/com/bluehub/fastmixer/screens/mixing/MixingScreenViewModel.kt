package com.bluehub.fastmixer.screens.mixing

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.bluehub.fastmixer.MixerApplication
import com.bluehub.fastmixer.common.permissions.PermissionViewModel
import com.bluehub.fastmixer.common.utils.PermissionManager
import com.bluehub.fastmixer.common.utils.ScreenConstants
import com.bluehub.fastmixer.common.views.FileWaveView
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

class MixingScreenViewModel(override val context: Context, mixerApplication: MixerApplication, override val tag: String): PermissionViewModel(context, mixerApplication, tag) {
    override var TAG: String = javaClass.simpleName

    private var audioFiles: MutableList<AudioFile> = mutableListOf()
    val audioFilesLiveData = MutableLiveData<MutableList<AudioFile>>(mutableListOf())

    @Inject
    override lateinit var permissionManager: PermissionManager

    @Inject
    lateinit var mixingRepository: MixingRepository

    private val _eventRecord = MutableLiveData<Boolean>()
    val eventRecord: LiveData<Boolean>
        get() = _eventRecord

    private val _eventRead = MutableLiveData<Boolean>()
    val eventRead: LiveData<Boolean>
        get() = _eventRead

    init {
        getViewModelComponent().inject(this)
        mixingRepository.createMixingEngine()
    }

    fun onRecord() {
        _eventRecord.value = true
    }

    fun onReadFromDisk() {
        if(!checkReadFilePermission()) {
            setRequestReadFilePermission(ScreenConstants.READ_FROM_FILE)
            return
        }
    }

    fun onSaveToDisk() {
        if(!checkWriteFilePermission()) {
            setRequestWriteFilePermission(ScreenConstants.WRITE_TO_FILE)
            return
        }
    }

    fun onRecordNavigated() {
        _eventRecord.value = false
    }

    fun addRecordedFilePath(filePath: String) {
        val file = File(filePath)
        if (file.exists()) {
            audioFiles.add(AudioFile(filePath, AudioFileType.RECORDED))
            audioFilesLiveData.value = audioFiles
        }
    }

    fun addFile(filePath: String) = viewModelScope.launch {
        mixingRepository.addFile(filePath)
    }

    fun readSamples(index: Int) = fun (numSamples: Int): Array<Float> = runBlocking(Dispatchers.IO) {
        mixingRepository.readSamples(index, numSamples)
    }


    fun deleteFile(index: Int) = viewModelScope.launch {
        mixingRepository.deleteFile(index)

        audioFiles.removeAt(index)

        audioFilesLiveData.value = audioFiles
    }
}