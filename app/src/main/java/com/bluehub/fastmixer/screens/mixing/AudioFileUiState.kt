package com.bluehub.fastmixer.screens.mixing

data class AudioFileUiState(
    val path: String,
    val numSamples: Int,
    var displayPtsCount: Int,
    var zoomLevel: Int)
