package com.bluehub.fastmixer.audio

import javax.inject.Inject

class RecordingEngineProxy @Inject constructor(){
    fun create(recordingFileDir: String,
               recordingScreenViewModelPassed: Boolean = false): Boolean =
        RecordingEngine.create(recordingFileDir, recordingScreenViewModelPassed)

    fun delete() = RecordingEngine.delete()

    fun startRecording() = RecordingEngine.startRecording()

    fun stopRecording() = RecordingEngine.stopRecording()

    fun startLivePlayback() = RecordingEngine.startLivePlayback()

    fun stopLivePlayback() = RecordingEngine.stopLivePlayback()

    fun startPlayback(fd: Int): Boolean = RecordingEngine.startPlayback(fd)

    fun startPlaybackWithMixingTracks(fd: Int): Boolean = RecordingEngine.startPlaybackWithMixingTracks(fd)

    fun startPlayingWithMixingTracksWithoutSetup() = RecordingEngine.startPlayingWithMixingTracksWithoutSetup()

    fun startMixingTracksPlayback(fd: Int): Boolean = RecordingEngine.startMixingTracksPlayback(fd)

    fun stopMixingTracksPlayback() = RecordingEngine.stopMixingTracksPlayback()

    fun stopPlayback() = RecordingEngine.stopPlayback()

    fun pausePlayback() = RecordingEngine.pausePlayback()

    fun flushWriteBuffer() = RecordingEngine.flushWriteBuffer()

    fun restartPlayback(fd: Int) = RecordingEngine.restartPlayback(fd)

    fun restartPlaybackWithMixingTracks(fd: Int) = RecordingEngine.restartPlaybackWithMixingTracks(fd)

    fun getCurrentMax(): Int = RecordingEngine.getCurrentMax()

    fun resetCurrentMax() = RecordingEngine.resetCurrentMax()

    fun getTotalSampleFrames() = RecordingEngine.getTotalSampleFrames()

    fun getCurrentPlaybackProgress() = RecordingEngine.getCurrentPlaybackProgress()

    fun setPlayHead(position: Int) = RecordingEngine.setPlayHead(position)

    fun getDurationInSeconds(): Int = RecordingEngine.getDurationInSeconds()

    fun addSources(filePaths: Array<String>) = RecordingEngine.addSources(filePaths)

    fun resetRecordingEngine() = RecordingEngine.resetRecordingEngine()

    fun closeFd(fd: Int) = RecordingEngine.closeFd(fd)
}
