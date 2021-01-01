package com.bluehub.fastmixer.screens.mixing

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class AudioViewSampleCountStore @Inject constructor() {
    private lateinit var mAudioFilesLiveData: LiveData<MutableList<AudioFile>>

    private val measuredWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    private val fileSampleCountMap: MutableMap<String, Int> = mutableMapOf()
    val isFileSampleCountMapUpdated: BehaviorSubject<Boolean> = BehaviorSubject.create()

    private val fileListObserver: Observer<MutableList<AudioFile>> = Observer {
        calculateSampleCountEachView()
    }
    
    init {
        measuredWidth.subscribe { 
            calculateSampleCountEachView()
        }
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
        }

        isFileSampleCountMapUpdated.onNext(true)
    }
    
    fun getSampleCount(fileName: String): Int? {
        return fileSampleCountMap[fileName]
    }

    fun removeLivedataObservers() {
        mAudioFilesLiveData.removeObserver(fileListObserver)
    }
}