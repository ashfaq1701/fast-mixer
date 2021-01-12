package com.bluehub.fastmixer.common.utils

fun <T> List<T>.areEqual(anotherList: List<T>): Boolean {
    return contains(anotherList) && anotherList.contains(this)
}

fun <T> MutableList<T>.reInitList(anotherList: List<T>) {
    removeAll { true }
    this.addAll(anotherList)
}
