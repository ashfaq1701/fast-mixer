package com.bluehub.fastmixer.common.models

data class AudioViewAction(val actionType: AudioViewActionType, val filePath: String)

enum class AudioViewActionType {
    NONE, GAIN_ADJUSTMENT
}
