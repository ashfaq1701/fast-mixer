package com.bluehub.fastmixer.common.utils

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

class BooleanCombinedLiveData(
    val init: Boolean,
    vararg items: LiveData<Boolean>,
    val combiner: (Boolean, Boolean) -> Boolean) : MediatorLiveData<Boolean>() {

    private val itemValues: MutableList<Boolean> = MutableList(items.size) {false}

    init {

        for(i in items.indices) {
            super.addSource(items[i]) {
                itemValues[i] = it


                value = itemValues.fold(init) { acc, current ->
                    combiner(acc, current)
                }
            }
        }
    }
}
