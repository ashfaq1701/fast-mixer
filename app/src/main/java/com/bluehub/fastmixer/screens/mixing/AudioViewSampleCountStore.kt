package com.bluehub.fastmixer.screens.mixing

import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class AudioViewSampleCountStore @Inject constructor() {

    private val audioFilesObservable: BehaviorSubject<MutableList<AudioFileWithNumSamples>> = BehaviorSubject.create()
    private val measuredWidth: BehaviorSubject<Int> = BehaviorSubject.create()
    private val fileSampleCountMap: MutableMap<String, Int> = mutableMapOf()
    val isFileSampleCountMapUpdated: BehaviorSubject<Boolean> = BehaviorSubject.create()
    
    init {
        audioFilesObservable.onNext(mutableListOf())

        audioFilesObservable.subscribe {
            calculateSampleCountEachView()
        }
        
        measuredWidth.subscribe { 
            calculateSampleCountEachView()
        }
    }

    fun updateMeasuredWidth(width: Int) {
        if (!measuredWidth.hasValue() || measuredWidth.value != width) {
            measuredWidth.onNext(width)
        }
    }

    fun addAudioFile(audioFile: AudioFileWithNumSamples) {
        val currentList = audioFilesObservable.value
        val filteredList = currentList.filter {
            it.path == audioFile.path
        }

        if (filteredList.isEmpty()) {
            currentList.add(audioFile)
            audioFilesObservable.onNext(currentList)
        }
    }

    fun removeAudioFile(audioFilePath: String) {
        val currentList = audioFilesObservable.value
        val itemIndex = currentList.indexOfFirst {
            it.path == audioFilePath
        }

        if (itemIndex != -1) {
            currentList.removeAt(itemIndex)
            audioFilesObservable.onNext(currentList)
        }
    }
    
    private fun calculateSampleCountEachView() {
        if (!audioFilesObservable.hasValue() || !measuredWidth.hasValue()) {
            return
        }

        val maxNumSamples = audioFilesObservable.value.fold(0) { maxSamples, audioFile ->
            if (maxSamples < audioFile.numSamples) {
                audioFile.numSamples
            } else maxSamples
        }

        audioFilesObservable.value.forEach { audioFile ->
            val numSamples = (audioFile.numSamples / maxNumSamples) * measuredWidth.value
            fileSampleCountMap[audioFile.path] = numSamples
        }
        
        isFileSampleCountMapUpdated.onNext(true)
    }
    
    fun getSampleCount(fileName: String): Int? {
        return fileSampleCountMap[fileName]
    }
}