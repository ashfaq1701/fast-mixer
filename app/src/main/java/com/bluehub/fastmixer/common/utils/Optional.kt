package com.bluehub.fastmixer.common.utils


class Optional<T> {

    companion object {

        fun <T> empty(): Optional<T> {
            return Optional()
        }

        fun <T> of(value: T): Optional<T> {
            return Optional(value)
        }
    }

    private var _value: T?

    val value: T?
        get() = _value

    private constructor() {
        this._value = null
    }

    private constructor(value: T) {
        this._value = value
    }

    fun isEmpty(): Boolean {
        return _value == null
    }
}
