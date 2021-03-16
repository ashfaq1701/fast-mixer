package com.bluehub.fastmixer.screens.mixing.modals

import android.content.Context
import androidx.lifecycle.*
import com.bluehub.fastmixer.R
import com.bluehub.fastmixer.common.utils.FileManager
import com.bluehub.fastmixer.common.utils.getRandomString
import com.bluehub.fastmixer.screens.mixing.AudioFileStore
import com.bluehub.fastmixer.screens.mixing.MixingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import java.util.*
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

    private val writeFileDirectory: String?
        get() = context.getExternalFilesDir(null)?.absolutePath

    private val _writtenFilePath: MutableLiveData<String?> = MutableLiveData(null)
    val writtenFilePath: LiveData<String?>
        get() = _writtenFilePath

    init {
        _fileName.value = getRandomString(10)
    }

    fun setFileName(name: String) {
        _fileName.value = name
    }

    private fun createAndGetFilePath(directory: String, fileName: String) : String? {
        val targetDir = "$directory/output/"
        val filePath = "$targetDir$fileName"

        val fileObj = File(filePath)

        return if (fileObj.exists()) {
            null
        } else {
            File(targetDir).mkdirs()

            fileObj.createNewFile()

            filePath
        }
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

        val writeDir = writeFileDirectory ?: run {
            errorLiveData.value = context.getString(R.string.error_file_write_directory_could_not_be_obtained)
            return
        }

        val filePath = createAndGetFilePath(writeDir, fileNameStr) ?: run {
            errorLiveData.value = context.getString(R.string.error_file_exists_in_target_directory)
            return
        }

        val fd = fileManager.getFdForPath(filePath)?.fd ?: run {
            errorLiveData.value = context.getString(R.string.error_could_not_use_file_for_write)
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            isLoading.postValue(true)
            val pathList = audioFileStore.audioFiles.map { it.path }

            val writeResult = mixingRepository.writeToFile(pathList, fd)

            isLoading.postValue(false)

            if (writeResult) {
                _writtenFilePath.postValue(filePath)
            } else {
                File(filePath).delete()
                errorLiveData.value = context.getString(R.string.error_file_write_failed)
            }
        }
    }

    fun resetWrittenFilePath() {
        _writtenFilePath.value = null
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
