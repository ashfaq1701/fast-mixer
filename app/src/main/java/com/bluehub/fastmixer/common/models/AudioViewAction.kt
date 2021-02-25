package com.bluehub.fastmixer.common.models

data class AudioViewAction(val actionType: AudioViewActionType, val uiState: AudioFileUiState)

enum class AudioViewActionType {
    NONE, GAIN_ADJUSTMENT, SEGMENT_ADJUSTMENT, SHIFT, COPY, CUT, MUTE, PASTE
}
