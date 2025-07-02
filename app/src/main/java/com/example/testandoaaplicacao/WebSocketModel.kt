package com.example.testandoaaplicacao

import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {

    private val messageService = MessageService()


    val socketStatus = messageService.isConnected
    val messages = messageService.messages

    override fun onCleared() {
        super.onCleared()
        messageService.shutdown()
    }

    fun send(text: String) {
        messageService.sendMessage(text)
    }

    fun connect() {
        messageService.connect()
    }

    fun disconnect() {
        messageService.disconnect()
    }
}
