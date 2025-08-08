package com.example.testandoaaplicacao


import android.Manifest
import android.content.Context
import android.health.connect.datatypes.ExerciseRoute
import android.location.Location
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import com.google.gson.Gson
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.Duration
import okhttp3.*



class MessageService (){
    private val _isConnected = MutableStateFlow(false)
    val isConnected = _isConnected.asStateFlow()

    private val _messages = MutableStateFlow(emptyList<Pair<Boolean, String>>())
    val messages = _messages.asStateFlow()

    private val gson = Gson()


    @RequiresApi(Build.VERSION_CODES.O)
    private val okHttpClient = OkHttpClient.Builder()
        .pingInterval(Duration.ofMillis(20000))
        .build()
    private var webSocket: WebSocket? = null

    private val webSocketListener = object : WebSocketListener() {

        override fun onOpen(webSocket: WebSocket, response: Response) {
            super.onOpen(webSocket, response)
            _isConnected.value = true
            println("ENTROUUUUUU")
            val gson = Gson()


            val messageToSend = WebSocketMessage(type = "CONNECTED", payload =  "I am received message, moneey")
            //(type = "CONNECTED", message = "")


            val jsonMessage = gson.toJson(messageToSend)

            webSocket.send(jsonMessage)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
           println("Mensagem recebida: $text")

            _messages.update {
                val list = it.toMutableList()
                list.add(false to text)
                list
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
            println( "Conexão fechando: $code - $reason")

        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
            _isConnected.value = false
            scheduleReconnect()
          println( "Conexão fechada: $code - $reason")

        }

        @RequiresApi(Build.VERSION_CODES.O)
        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            super.onFailure(webSocket, t, response)
            _isConnected.value = false
            scheduleReconnect()
             println( "FALHA NA CONEXÃO: ${t.message}")
            println("falha: ${response?.code} - ${response?.message}")
        }
    }

    private var reconnecting = false

    @OptIn(DelicateCoroutinesApi::class)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun scheduleReconnect() {
        GlobalScope.launch {
        if(reconnecting == false){
                reconnecting = true
                delay(5000)
                _messages.update {
                    val list = it.toMutableList()
                    list.add(false to "TENTANDO RECONECTAR")
                    list
                }
                connect()
                reconnecting = false
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun connect() {
        val meuTokenDeAcesso = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJRR0JXVlhkYWxFcHFvNXk3ZGJnMGI2TFpNcjM4Tkp3biIsImlzcyI6ImF1dGgwIiwibmFtZSI6IklUQUxPIEJBU1RPUyIsInVzZXJuYW1lIjoiaXRhbG8uc29sYXJAZGltZW5zaXZhLmNvbS5iciIsInJvbGUiOiJST0xFX0FETUlOIiwidXNlcktleSI6IlFHQldWWGRhbEVwcW81eTdkYmcwYjZMWk1yMzhOSnduIiwiY29tcGFueUtleSI6Ik95NllNMkU0TjlsUmtlem80NVFyTHFidnBEQkFqWDB3Iiwib2ZmaWNlS2V5IjoibzhEbE8xbUFSeGpyM3FnalZnSjZkVkdOTG4wYlk3S00iLCJhc3NvY2lhdGVkQ29tcGFueUtleSI6Ik95NllNMkU0TjlsUmtlem80NVFyTHFidnBEQkFqWDB3IiwiY29tcGFueU5hbWUiOiJTT0xBUiIsInN0YXRlIjoiQ0UiLCJzdGF0ZUtleSI6InAyYUxlRFlkeTRrQm1BZ0xrUHdSeFc3MG92bDkzSlhaIiwiZmlwcyI6ImFwaS1ici5yZWQzNjAuYXBwIiwidGlkIjoieDY0TVhxMEtXbkE1R3JSQjcwMmtLenlvakRMTmtWcmVRR0JXVlhkYWxFcHFvNXk3ZGJnMGI2TFpNcjM4Tkp3bk95NllNMkU0TjlsUmtlem80NVFyTHFidnBEQkFqWDB3IiwidWlkIjoiNDJsb0FRUktyYXd6Sm5KTFlyNW0wcXhnajZQYlhFeUw6YXBpLWJyLnJlZDM2MC5hcHAiLCJpYXQiOjE3NTMyMDc1NzIsImV4cCI6MTc2MDk4MzU3Mn0.JNPz4W-6jwHAxWKvhDCansxTCXzq6LZDuXBkeE1BI6Vc1V-0354zrine8jB4gCAO3dWhm95BpcNl9T7FdMoSFQ"
            //"eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvd0pCbDJXeURSUWtiNURET2s1WTRYcTA2bjdOOWR2aiIsImlzcyI6ImF1dGgwIiwibmFtZSI6Ik1PTkVMSVNBIiwidXNlcm5hbWUiOiJtb25lbGlzYS5zb2xhckBkaW1lbnNpdmEuY29tLmJyIiwicm9sZSI6IlJPTEVfQURNSU4iLCJ1c2VyS2V5Ijoib3dKQmwyV3lEUlFrYjVERE9rNVk0WHEwNm43TjlkdmoiLCJjb21wYW55S2V5IjoiT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJvZmZpY2VLZXkiOiJvOERsTzFtQVJ4anIzcWdqVmdKNmRWR05MbjBiWTdLTSIsImFzc29jaWF0ZWRDb21wYW55S2V5IjoiT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJjb21wYW55TmFtZSI6IlNPTEFSIiwic3RhdGUiOiJDRSIsInN0YXRlS2V5IjoicDJhTGVEWWR5NGtCbUFnTGtQd1J4Vzcwb3ZsOTNKWFoiLCJmaXBzIjoiYXBpLWJyLnJlZDM2MC5hcHAiLCJ0aWQiOiJLd20wTE02eHJXYmc3S2VSTm1LMHlnZU9YbEVrYXZuOW93SkJsMld5RFJRa2I1RERPazVZNFhxMDZuN045ZHZqT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJ1aWQiOiJyV0JvTk80MFllZHo2bk1rcW01bUUzdjJwUkExeXdKajphcGktYnIucmVkMzYwLmFwcCIsImlhdCI6MTc1MzIwNzAzMiwiZXhwIjoxNzYwOTgzMDMyfQ.YB9mdc9hF3Pv6j9gkuunnX9e0K3eS02QEOrWC-2o-mbKGEnz63kXf6WCOn6rq9YWbfjgRS1uKKThWQjOXrWWUA"
            //"eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvd0pCbDJXeURSUWtiNURET2s1WTRYcTA2bjdOOWR2aiIsImlzcyI6ImF1dGgwIiwibmFtZSI6Ik1PTkVMSVNBIiwidXNlcm5hbWUiOiJtb25lbGlzYS5zb2xhckBkaW1lbnNpdmEuY29tLmJyIiwicm9sZSI6IlJPTEVfQURNSU4iLCJ1c2VyS2V5Ijoib3dKQmwyV3lEUlFrYjVERE9rNVk0WHEwNm43TjlkdmoiLCJjb21wYW55S2V5IjoiT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJvZmZpY2VLZXkiOiJvOERsTzFtQVJ4anIzcWdqVmdKNmRWR05MbjBiWTdLTSIsImFzc29jaWF0ZWRDb21wYW55S2V5IjoiT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJjb21wYW55TmFtZSI6IlNPTEFSIiwic3RhdGUiOiJDRSIsInN0YXRlS2V5IjoicDJhTGVEWWR5NGtCbUFnTGtQd1J4Vzcwb3ZsOTNKWFoiLCJmaXBzIjoiYXBpLWJyLnJlZDM2MC5hcHAiLCJ0aWQiOiJLd20wTE02eHJXYmc3S2VSTm1LMHlnZU9YbEVrYXZuOW93SkJsMld5RFJRa2I1RERPazVZNFhxMDZuN045ZHZqT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJ1aWQiOiJyV0JvTk80MFllZHo2bk1rcW01bUUzdjJwUkExeXdKajphcGktYnIucmVkMzYwLmFwcCIsImlhdCI6MTc1MzIwNzAzMiwiZXhwIjoxNzYwOTgzMDMyfQ.YB9mdc9hF3Pv6j9gkuunnX9e0K3eS02QEOrWC-2o-mbKGEnz63kXf6WCOn6rq9YWbfjgRS1uKKThWQjOXrWWUA"
            //"eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJwcm9maWxlUGljdHVyZVVybCI6Imh0dHBzOi8vcmVzLmNsb3VkaW5hcnkuY29tL2RoZzhlaW1nZC9pbWFnZS91cGxvYWQvdjE2MDY0ODIzMzAvcmVkMzYwLTIvNjE2NDM2MzEtMzM2NS02NjM2LTJkNjItNjUzOTYyMmQzNDY0LnBuZyIsInN1YiI6Ikx5a1pqTnpPZHhFblBhQWQ1OTdwSzhsUVhZR3dSTUIyIiwicm9sZSI6IlJPTEVfQURNSU4iLCJuYW1lIjoiRElNRU5TSVZBIiwiaXNzIjoiYXV0aDAiLCJhc3NvY2lhdGVkQ29tcGFueUtleSI6IlpEZHo1TjFHTTAyS3I2V0VWQXlua1BPYUVxSjlCTDg0IiwiY29tcGFueUtleSI6IlpEZHo1TjFHTTAyS3I2V0VWQXlua1BPYUVxSjlCTDg0Iiwic3RhdGUiOiJDRSIsImV4cCI6MTgwOTcxMDk5MSwib2ZmaWNlS2V5Ijoid3Frcjl5QkxhUE8ybU43S3c3M1F2WGVLcEd4ZzRsWlIiLCJpYXQiOjE3MTUxMDI5OTEsInVzZXJLZXkiOiJMeWtaak56T2R4RW5QYUFkNTk3cEs4bFFYWUd3Uk1CMiJ9.y97dYJnAss_qt_x0Q15TkX9TJEYYzVwMLa1oldmCH0J-pZ_nX1Bpk2uoCpeI7c901qj-dPNmfRGbuHS3RL1acQ"
        val webSocketUrl =
            //"https://api-dev.routino.io/smart-route/create-user-steps.json"
            "wss://ws.dimensiva.io/io"
//            "ws://echo.websocket.org"
          // "ws://ws.red360.app/io"
           // "wss://demo.piesocket.com/v3/channel_123?api_key=VCXCEuvhGcBDP7XhiJJUDvR1e1D3eiVjgZ9VRiaV&notify_self"
//           "wss://ws.postman-echo.com/raw"
//            "ws://ws.red360.app/io".replace("http","https")
         // " ws://ws.red360.app/io?Authorization=$meuTokenDeAcesso"
        //val uri = webSocketUrl.toUri()
        val request = Request.Builder()
            .url("$webSocketUrl")
            .header("Authorization" , "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJvd0pCbDJXeURSUWtiNURET2s1WTRYcTA2bjdOOWR2aiIsImlzcyI6ImF1dGgwIiwibmFtZSI6Ik1PTkVMSVNBIiwidXNlcm5hbWUiOiJtb25lbGlzYS5zb2xhckBkaW1lbnNpdmEuY29tLmJyIiwicm9sZSI6IlJPTEVfQURNSU4iLCJ1c2VyS2V5Ijoib3dKQmwyV3lEUlFrYjVERE9rNVk0WHEwNm43TjlkdmoiLCJjb21wYW55S2V5IjoiT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJvZmZpY2VLZXkiOiJvOERsTzFtQVJ4anIzcWdqVmdKNmRWR05MbjBiWTdLTSIsImFzc29jaWF0ZWRDb21wYW55S2V5IjoiT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJjb21wYW55TmFtZSI6IlNPTEFSIiwic3RhdGUiOiJDRSIsInN0YXRlS2V5IjoicDJhTGVEWWR5NGtCbUFnTGtQd1J4Vzcwb3ZsOTNKWFoiLCJmaXBzIjoiYXBpLWJyLnJlZDM2MC5hcHAiLCJ0aWQiOiJLd20wTE02eHJXYmc3S2VSTm1LMHlnZU9YbEVrYXZuOW93SkJsMld5RFJRa2I1RERPazVZNFhxMDZuN045ZHZqT3k2WU0yRTROOWxSa2V6bzQ1UXJMcWJ2cERCQWpYMHciLCJ1aWQiOiJyV0JvTk80MFllZHo2bk1rcW01bUUzdjJwUkExeXdKajphcGktYnIucmVkMzYwLmFwcCIsImlhdCI6MTc1MzIwNzAzMiwiZXhwIjoxNzYwOTgzMDMyfQ.YB9mdc9hF3Pv6j9gkuunnX9e0K3eS02QEOrWC-2o-mbKGEnz63kXf6WCOn6rq9YWbfjgRS1uKKThWQjOXrWWUA")
        //("Authorization", meuTokenDeAcesso)
            .build()

        webSocket = okHttpClient.newWebSocket(request, webSocketListener)
        //println("${request.toString()}")
    }

    fun disconnect() {
        webSocket?.close(1000, "Disconnected by client")
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun shutdown() {
        okHttpClient.dispatcher.executorService.shutdown()
    }

    fun sendMessage(text: String,) {
        if (_isConnected.value) {
            val messageToSend = WebSocketMessage(type = "MESSAGE", payload = text)
            val jsonMessage = gson.toJson(messageToSend)
            webSocket?.send(jsonMessage)
            _messages.update {
                val list = it.toMutableList()
                list.add(Pair(true, text))
                list
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun sendLocation(lat: Double, lng: Double, context: Context) {
        val ultimaLocalizacao : Location? = null
        if (_isConnected.value) {
            val payload = LocationPayload(lat = lat, long = lng)
            val messageToSend = WebSocketMessage(type = "LOCATION_UPDATE", payload = payload)
            val jsonMessage = gson.toJson(messageToSend)
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            fusedLocationClient.lastLocation.addOnSuccessListener{
                location: Location? ->
                if (location != null){
                    if (ultimaLocalizacao == null || location.distanceTo(ultimaLocalizacao!!) > 1){
                        ultimaLocalizacao = location
                        enviarDadosParaAPI(git statusDadosPost(
                            latitude = location.latitude,
                            longitude = location.longitude
                        ))
                    }
                }

            }

            println("Enviando localização: $jsonMessage")
//            webSocket?.send(jsonMessage)
//            _messages.update {
//                val list = it.toMutableList()
//                list.add(true to "Localização enviada: ${"%.4f".format(lat)}, ${"%.4f".format(lng)}")
//                list
//            }
        }
    }
    fun enviarDadosParaAPI() {
        viewModelS.launch {
            try {

                // val response = RetrofitClient.apiService.enviarDados(dados)
            } catch (e: Exception) {

            }
        }
    }
}

}