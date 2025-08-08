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

        val notification = createNotification()


        startForeground(1, notification)


        startTrackingLogic()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun startTrackingLogic() {

        messageService.connect()


        locationRepository.locationUpdates(interval = 10000L) // 10s
            .onEach { latLng ->
                println("SERVIÇO OBTEVE LOCALIZAÇÃO: $latLng")

                messageService.sendLocation(latLng.lat, latLng.lng, )
            }
            .catch { e ->

                println("Erro no flow de localização: ${e.message}")
            }
            .launchIn(serviceScope)
    }


    private fun stop() {

        stopForeground(true)

        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()

        serviceScope.cancel()
        messageService.disconnect()
        println("Serviço destruído.")
    }

    override fun onBind(intent: Intent?): IBinder? {

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


        val stopIntent = Intent(this, BackgroundLocationTrackingService::class.java).apply {
            action = ACTION_STOP
        }


        val stopPendingIntent = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Rastreamento Ativo")
            .setContentText("Enviando sua localização em tempo real.")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .addAction(R.drawable.ic_stop_button, "Parar", stopPendingIntent)
            .build()
    }
}