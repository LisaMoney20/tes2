package com.example.testandoaaplicacao

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MessageService {
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _messages = MutableStateFlow(emptyList<Pair<Boolean, String>>())
    val messages = _messages.asStateFlow()

    private val okHttpClient = OkHttpClient()
    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            _isConnected.value = true
            // LOG DE SUCESSO
            Log.d("WebSocket", "Conexão aberta com sucesso!")
            webSocket.send("Android Client Connected")
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            // LOG DE MENSAGEM
            Log.d("WebSocket", "Mensagem recebida: $text")
            _messages.update {
                val list = it.toMutableList()
                list.add(false to text)
                list
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            // LOG DE FECHAMENTO
            Log.d("WebSocket", "Conexão fechando: $code - $reason")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            _isConnected.value = false
            // LOG DE FECHADO
            Log.d("WebSocket", "Conexão fechada: $code - $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            _isConnected.value = false

            Log.e("WebSocket", "FALHA NA CONEXÃO: ${t.message}", t)
            response?.let {
                Log.e("WebSocket", "Resposta da falha: ${it.code} - ${it.message}")
            }
        }
    }

    fun connect() {
        val webSocketUrl =
            "wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self"
           // "wss://ws.postman-echo.com/raw"
        val meuTokenDeAcesso = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJwcm9maWxlUGljdHVyZVVybCI6Imh0dHBzOi8vcmVzLmNsb3VkaW5hcnkuY29tL2RoZzhlaW1nZC9pbWFnZS91cGxvYWQvdjE2MDY0ODIzMzAvcmVkMzYwLTIvNjE2NDM2MzEtMzM2NS02NjM2LTJkNjItNjUzOTYyMmQzNDY0LnBuZyIsInN1YiI6Ikx5a1pqTnpPZHhFblBhQWQ1OTdwSzhsUVhZR3dSTUIyIiwicm9sZSI6IlJPTEVfQURNSU4iLCJuYW1lIjoiRElNRU5TSVZBIiwiaXNzIjoiYXV0aDAiLCJhc3NvY2lhdGVkQ29tcGFueUtleSI6IlpEZHo1TjFHTTAyS3I2V0VWQXlua1BPYUVxSjlCTDg0IiwiY29tcGFueUtleSI6IlpEZHo1TjFHTTAyS3I2V0VWQXlua1BPYUVxSjlCTDg0Iiwic3RhdGUiOiJDRSIsImV4cCI6MTgwOTcxMDk5MSwib2ZmaWNlS2V5Ijoid3Frcjl5QkxhUE8ybU43S3c3M1F2WGVLcEd4ZzRsWlIiLCJpYXQiOjE3MTUxMDI5OTEsInVzZXJLZXkiOiJMeWtaak56T2R4RW5QYUFkNTk3cEs4bFFYWUd3Uk1CMiJ9.y97dYJnAss_qt_x0Q15TkX9TJEYYzVwMLa1oldmCH0J-pZ_nX1Bpk2uoCpeI7c901qj-dPNmfRGbuHS3RL1ac"

        val request = Request.Builder()
            .url(webSocketUrl)
           // .header("Authorization", meuTokenDeAcesso)
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnected by client")
    }

    fun shutdown() {
        okHttpClient.dispatcher.executorService.shutdown()
    }

    fun sendMessage(text: String) {
        if (_isConnected.value) {
            webSocket?.send(text)
            _messages.update {
                val list = it.toMutableList()
                list.add(Pair(true, text))
                list
            }
        }
    }
}