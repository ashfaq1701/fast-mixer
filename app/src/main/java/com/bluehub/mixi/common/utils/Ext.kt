package com.bluehub.mixi.common.utils

import android.R
import android.util.TypedValue
import android.view.View
import java.util.*


fun <T> List<T>.areEqual(anotherList: List<T>): Boolean {
    if (this.size != anotherList.size) return false
    return this.zip(anotherList).all { (x, y) -> x == y }
}

fun <T> MutableList<T>.reInitList(anotherList: List<T>) {
    removeAll { true }
    this.addAll(anotherList)
}

fun View.getCurrentBackground(): Int? {
    val a = TypedValue()
    context.theme.resolveAttribute(R.attr.windowBackground, a, true)
    return if (a.isColorType) {
        a.data
    } else null
}

fun getRandomString(length: Int) : String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
        .map { allowedChars.random() }
        .joinToString("")
}
