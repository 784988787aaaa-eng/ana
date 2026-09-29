package com.smartledger.aldaftar.data.cloud

class CloudOperationException(
    val statusCode: Int = 0,
    val errorCode: String = "",
    val userMessage: String,
    cause: Throwable? = null
) : Exception(userMessage, cause)
