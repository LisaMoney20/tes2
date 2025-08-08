package com.example.testandoaaplicacao

data class LocationPayload(val lat: Double, val long: Double)

data class WebSocketMessage(
    val type: String,
    val payload: Any
)
