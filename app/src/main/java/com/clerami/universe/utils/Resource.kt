package com.clerami.universe.utils

data class Resource<out T>(val status: Status, val data: T?, val message: String?) {
    companion object {
        fun <T> success(data: T?): Resource<T> = Resource(Status.SUCCESS, data, null)
        fun <T> error(message: String): Resource<T> = Resource(Status.ERROR, null, message)
        fun <T> loading(): Resource<T> = Resource(Status.LOADING, null, null)
    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}
