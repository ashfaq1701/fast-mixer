package com.bluehub.fastmixer.screens.mixing

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

enum class AudioFileType {
    RECORDED, IMPORTED
}

@Parcelize
data class AudioFile(val path: String, val numSamples: Int, val fileType: AudioFileType):
    Serializable, Parcelable
