package com.bluehub.fastmixer.screens.mixing

class MixingEngine {
    companion object {
        init {
            System.loadLibrary("mixingEngine")
        }

        @JvmStatic external fun create(): Boolean
    }
}