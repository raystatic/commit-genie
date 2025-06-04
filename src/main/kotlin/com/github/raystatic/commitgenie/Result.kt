package com.github.raystatic.commitgenie


sealed class Result {
    data class Success(val message: String) : Result()
    data class Failure(val error: String, val code: Int? = null) : Result()
}