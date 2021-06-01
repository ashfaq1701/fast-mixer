package com.bluehub.mixi.screens.mixing.modals

import android.content.Context
import androidx.lifecycle.*
import com.bluehub.mixi.R
import com.bluehub.mixi.common.utils.FileManager
import com.bluehub.mixi.common.utils.getRandomString
import com.bluehub.mixi.screens.mixing.AudioFileStore
import com.bluehub.mixi.screens.mixing.MixingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WriteViewModel @Inject constructor(
    @ApplicationContext val context: Context,
    private val fileManager: FileManager,
    private val audioFileStore: AudioFileStore,
    val mixingRepository: MixingRepository
) : BaseDialogViewModel() {

    private val _fileName: MutableLiveData<String> = MutableLiveData<String>()
    val fileName: LiveData<String>
        get() = _fileName

    private val _writtenFileName: MutableLiveData<String?> = MutableLiveData(null)
    val writtenFileName: LiveData<String?>
        get() = _writtenFileName

    init {
        _fileName.value = getRandomString(15)
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    fun performWrite() {

        val fileNameStr = fileName.value ?: run {
            errorLiveData.value = context.getString(R.string.error_write_file_name_cannot_be_empty)
            return
        }

        if (fileNameStr.isEmpty()) {
            errorLiveData.value = context.getString(R.string.error_write_file_name_cannot_be_empty)
            return
        }

        if (audioFileStore.audioFiles.isEmpty()) {
            errorLiveData.value = context.getString(R.string.error_write_file_list_empty)
            return
        }

        val fileNameWithExt = "$fileNameStr.wav"
        val fd = fileManager.getFileDescriptorForMedia(fileNameWithExt)?.fd ?: run {
            errorLiveData.value = context.getString(R.string.error_could_not_use_file_for_write)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            val pathList = audioFileStore.audioFiles.map { it.path }

            val writeResult = mixingRepository.writeToFile(pathList, fd)

            isLoading.postValue(false)

            if (writeResult) {
                _writtenFileName.postValue(fileNameWithExt)
            } else {
                errorLiveData.postValue(context.getString(R.string.error_file_write_failed))
            }
        }
    }

    fun resetWrittenFileName() {
        _writtenFileName.value = null
    }

    fun cancelWrite() {
        closeDialog()
    }

    fun closeDialog() {
        closeDialogLiveData.value = true
    }

    fun clearFileName() {
        _fileName.value = ""
    }
}
