package com.bluehub.fastmixer.audio

import javax.inject.Inject

class RecordingEngineProxy @Inject constructor(){
    fun create(recordingFileDir: String,
               recordingScreenViewModelPassed: Boolean = false): Boolean =
        RecordingEngine.create(recordingFileDir, recordingScreenViewModelPassed)

    fun delete() = RecordingEngine.delete()

    fun setupAudioSource(fd: Int) = RecordingEngine.setupAudioSource(fd)

    fun startRecording() = RecordingEngine.startRecording()

    fun stopRecording() = RecordingEngine.stopRecording()

    fun startLivePlayback() = RecordingEngine.startLivePlayback()

    fun stopLivePlayback() = RecordingEngine.stopLivePlayback()

    fun startPlayback(): Boolean = RecordingEngine.startPlayback()

    fun startPlaybackWithMixingTracks(): Boolean = RecordingEngine.startPlaybackWithMixingTracks()

    fun startPlayingWithMixingTracksWithoutSetup() = RecordingEngine.startPlayingWithMixingTracksWithoutSetup()

    fun startMixingTracksPlayback(): Boolean = RecordingEngine.startMixingTracksPlayback()

    fun stopMixingTracksPlayback() = RecordingEngine.stopMixingTracksPlayback()

    fun stopPlayback() = RecordingEngine.stopPlayback()

    fun pausePlayback() = RecordingEngine.pausePlayback()

    fun flushWriteBuffer() = RecordingEngine.flushWriteBuffer()

    fun restartPlayback() = RecordingEngine.restartPlayback()

    fun restartPlaybackWithMixingTracks() = RecordingEngine.restartPlaybackWithMixingTracks()

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
