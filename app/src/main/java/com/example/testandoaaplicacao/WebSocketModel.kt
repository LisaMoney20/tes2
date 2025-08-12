package com.example.testandoaaplicacao

import android.app.Application
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach


class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val messageService = MessageService()

    private val locationRepository = LocationRepository(application)
    val socketStatus = messageService.isConnected
    val messages = messageService.messages
    private var locationJob: Job? = null
    val currentLocation: StateFlow<User> = messageService.currentLocation
//    fun startLocationUpdates()
//    {
//        locationJob?.cancel()
//        locationJob = locationRepository.locationUpdates(interval = 10000L)
//            .onEach {latLng ->
//                println("ViewModel recebeu localização: $latLng")
//                messageService.sendLocation(latLng.lat, latLng.lng)
//
//            }
//            .catch {e ->
//                println("Erro ao obter localização: ${e.message}")
//            }
//            .launchIn(viewModelScope)
//
//    }
//
//    fun stopLocationUpdates() {
//        locationJob?.cancel()
//        println("Atualizações de localização paradas.")
//    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    override fun onCleared() {
//        super.onCleared()
//        messageService.shutdown()
//    }

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
