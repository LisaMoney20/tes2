package com.example.testandoaaplicacao

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel


class MainViewModel : ViewModel() {

    private val messageService = MessageService()


    val socketStatus = messageService.isConnected
    val messages = messageService.messages

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCleared() {
        super.onCleared()
        messageService.shutdown()
    }

    fun send(text: String) {
        messageService.sendMessage(text)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect() {
        messageService.connect()
    }

    fun disconnect() {
        messageService.disconnect()
//        messageService.connect()
    }
}
