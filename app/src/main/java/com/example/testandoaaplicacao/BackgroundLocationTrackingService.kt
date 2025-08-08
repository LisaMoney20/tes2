package com.example.testandoaaplicacao

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch      // Para a função .catch()
import kotlinx.coroutines.flow.launchIn   // Para a função .launchIn()
import kotlinx.coroutines.flow.onEach


class BackgroundLocationTrackingService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationRepository: LocationRepository
    private lateinit var messageService: MessageService

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }

    override fun onCreate() {
        super.onCreate()
        locationRepository = LocationRepository(this)
        messageService = MessageService()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_NOT_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun start() {
        // 1. Cria a notificação obrigatória
        val notification = createNotification()

        // 2. Promove o serviço para "primeiro plano"
        startForeground(1, notification)

        // 3. Inicia a lógica de negócios
        startTrackingLogic()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTrackingLogic() {
        // Conecta o WebSocket
        messageService.connect()

        // Inicia o listener de localização
        locationRepository.locationUpdates(interval = 10000L) // 10s
            .onEach { latLng ->
                println("SERVIÇO OBTEVE LOCALIZAÇÃO: $latLng")
                // Envia a localização através do WebSocket
                messageService.sendLocation(latLng.lat, latLng.lng)
            }
            .catch { e ->
                // Lidar com erros, talvez parar o serviço
                println("Erro no flow de localização: ${e.message}")
            }
            .launchIn(serviceScope) // Lança no escopo do serviço
    }


    private fun stop() {
        // Para o serviço de primeiro plano e remove a notificação
        stopForeground(true)
        // Para o serviço completamente
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        // Limpeza final: cancela todas as coroutines e desconecta o WebSocket
        serviceScope.cancel()
        messageService.disconnect()
        println("Serviço destruído.")
    }

    override fun onBind(intent: Intent?): IBinder? {
        // Não vamos usar binding por enquanto, então retornamos null.
        return null
    }

//    private fun createNotification(): Notification {
//        val channelId = "location_tracking_channel"
//        val channelName = "Location Tracking"
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
//            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
//            manager.createNotificationChannel(channel)
//        }
//
//        return NotificationCompat.Builder(this, channelId)
//            .setContentTitle("Rastreamento Ativo")
//            .setContentText("Enviando sua localização em tempo real.")
//            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone seu
//            .setOngoing(true) // Torna a notificação não-removível pelo usuário
//            .build()
//    }

    @SuppressLint("ObsoleteSdkInt")
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotification(): Notification {
        val channelId = "location_tracking_channel"
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Location Tracking", NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // --- NOVA LÓGICA PARA A AÇÃO DE PARAR ---

        // 1. Cria um Intent para a ação de parar o serviço
        val stopIntent = Intent(this, BackgroundLocationTrackingService::class.java).apply {
            action = ACTION_STOP
        }

        // 2. Cria um PendingIntent que o sistema usará para enviar o Intent em nosso nome
        val stopPendingIntent = PendingIntent.getService(
            this,
            1, // um request code único
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 3. Constrói a notificação e ADICIONA A AÇÃO
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rastreamento Ativo")
            .setContentText("Enviando sua localização em tempo real.")
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Use um ícone seu
            .setOngoing(true)
            .addAction(R.drawable.ic_stop_button, "Parar", stopPendingIntent)
            .build()
    }
}