package com.example.testandoaaplicacao

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent



@SuppressLint("ObsoleteSdkInt")
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebSocketChatScreen(
    viewModel: MainViewModel = viewModel() // viewModel() cria a instância corretamente
) {
    val context = LocalContext.current
    val status by viewModel.socketStatus.collectAsState(false)

    val messages by viewModel.messages.collectAsState(emptyList())
    val listState = rememberLazyListState()
   // val currentLocation by viewModel.currentLocation.collectAsState()
//    LaunchedEffect(messages.size) {
//        if (messages.isNotEmpty()) { listState.animateScrollToItem(messages.lastIndex) }
//    }

    var hasLocationPermission by remember { mutableStateOf(hasLocationPermission(context)) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            hasLocationPermission = permissions.values.all { it }

        }
    )

    Scaffold(
        topBar = {
            TopAppBar(isConnected = status,
               onConnect = viewModel::connect,
                onDisconnect = viewModel::disconnect
           )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {

            LaunchedEffect(hasLocationPermission) {
                if (hasLocationPermission) {
                    Intent(context, BackgroundLocationTrackingService::class.java).also {
                        it.action = BackgroundLocationTrackingService.ACTION_START
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            context.startForegroundService(it)
                        } else {
                            context.startService(it)
                        }
                    }
                }
            }


            if (!hasLocationPermission) {
                PermissionRequestUI(
                    onPermissionRequest = {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                )
            }
//            currentLocation?.let { (lat, lng) ->
//                Text("Posição: Lat ${"%.4f".format(lat)}, Lon ${"%.4f".format(lng)}")
//            }
            LazyColumn(state = listState, modifier = Modifier.weight(1f).fillMaxWidth()) {
                items(messages) { item -> MessageItem(item = item) }
            }
            BottomPanel(onSend = viewModel::send)
        }
    }
}

//@Composable
//private fun TrackingControlPanel() {
//    val context = LocalContext.current
//    Card(modifier = Modifier.fillMaxWidth().padding(16.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
//        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
//            Text("Controle do Rastreamento", style = MaterialTheme.typography.titleMedium)
//            Spacer(Modifier.height(16.dp))
//            Row(horizontalArrangement = Arrangement.Center) {
//                Button(onClick = {
//                    Intent(context, BackgroundLocationTrackingService::class.java).also {
//                        it.action = BackgroundLocationTrackingService.ACTION_START
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            context.startForegroundService(it)
//                        } else {
//                            context.startService(it)
//                        }
//                    }
//                }) { Text("Iniciar Serviço") }
//                Spacer(Modifier.width(16.dp))
//                Button(onClick = {
//                    Intent(context, BackgroundLocationTrackingService::class.java).also {
//                        it.action = BackgroundLocationTrackingService.ACTION_STOP
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                            context.startForegroundService(it)
//                        } else {
//                            context.startService(it)
//                        }
//                    }
//                }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Parar Serviço") }
//            }
//        }
//    }
//
//}
//    val context = LocalContext.current
//    val status by viewModel.socketStatus.collectAsState(false)
//    val messages by viewModel.messages.collectAsState(emptyList())
//
//    val listState = rememberLazyListState()
//
//    // Efeito para rolar a lista para a última mensagem
//    LaunchedEffect(messages.size) {
//        if (messages.isNotEmpty()) {
//            listState.animateScrollToItem(messages.lastIndex)
//        }
//    }
//
//
//
//    // 1. Estado para saber se a permissão foi concedida
//    var hasLocationPermission by remember {
//        mutableStateOf(hasLocationPermission(context))
//    }
//
//    // 2. Launcher para pedir as permissões ao usuário
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            // Verifica se TODAS as permissões pedidas foram concedidas
//            hasLocationPermission = permissions.values.all { it }
//        }
//    )
//
//    // 3. Efeito que inicia as atualizações de localização quando a permissão é concedida.
//    //    Ele é re-executado se 'hasLocationPermission' mudar de false para true.
//    LaunchedEffect(hasLocationPermission) {
//        if (hasLocationPermission) {
//            viewModel.startLocationUpdates()
//        }
//    }
//
//    // 4. Efeito que garante que as atualizações parem quando a tela sai da composição (é fechada).
//    //    Isso é CRUCIAL para não gastar bateria desnecessariamente.
//    DisposableEffect(Unit) {
//        onDispose {
//            viewModel.stopLocationUpdates()
//        }
//    }
//
//    // --- FIM DA LÓGICA DE PERMISSÃO ---
//
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                isConnected = status,
//                onConnect = viewModel::connect,
//                onDisconnect = viewModel::disconnect
//            )
//        }
//    ) { padding ->
//        Column(
//            modifier = Modifier
//                .padding(padding)
//                .fillMaxSize()
//        ) {
//
//            // 5. UI que aparece para pedir a permissão se ela ainda não foi concedida
//            if (!hasLocationPermission) {
//                PermissionRequestUI(
//                    onPermissionRequest = {
//                        // Lança o pedido de permissão
//                        permissionLauncher.launch(
//                            arrayOf(
//                                Manifest.permission.ACCESS_FINE_LOCATION,
//                                Manifest.permission.ACCESS_COARSE_LOCATION
//                            )
//                        )
//                    }
//                )
//            }
//
//            LazyColumn(
//                state = listState,
//                modifier = Modifier
//                    .weight(1f)
//                    .fillMaxWidth()
//            ) {
//                items(messages) { item ->
//                    MessageItem(item = item)
//                }
//            }
//            BottomPanel(onSend = viewModel::send)
//        }
//    }
//}



@Composable
private fun PermissionRequestUI(onPermissionRequest: () -> Unit) {
    Card(
        modifier = Modifier
          //  .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
//        Column(
//            modifier = Modifier.padding(16.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(
//                text = "Permissão de Localização Necessária",
//                style = MaterialTheme.typography.titleMedium
//            )
//            Spacer(Modifier.height(8.dp))
//            Text(
//                "Para o rastreamento em tempo real, precisamos que você conceda a permissão de localização.",
//                style = MaterialTheme.typography.bodyMedium
//            )
//            Spacer(Modifier.height(16.dp))
            Button(onClick = onPermissionRequest) {
                Text("Conceder Permissão")
            }
        //}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val statusText = if (isConnected) "Conectado" else "Desconectado"
    val contentDesc = if (!isConnected) "Conectar" else "Desconectar"
    val buttonIcon = if (isConnected) Icons.Outlined.Close else Icons.Outlined.Check
    TopAppBar(
        title = { Text(statusText) },
        actions = {
            IconButton(onClick = {
                if (!isConnected) {
                    onConnect()
                } else {
                    onDisconnect()
                }
            }) {
                Icon(imageVector = buttonIcon, contentDescription = contentDesc)
            }
        },
    )
}

@Composable
private fun MessageItem(item: Pair<Boolean, String>) {
    val (iAmTheSender, message) = item

    val alignment = if (iAmTheSender) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(8.dp)
        )
    }
}


@Composable
private fun BottomPanel(
    onSend: (String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier.padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { s -> text = s },
            modifier = Modifier.weight(1f),
            placeholder = { Text("Digite uma mensagem...")},
            maxLines = 1
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(
            onClick = {
                onSend(text)
                text = ""
            },
            enabled = text.isNotBlank(),
        ) {
            Text("Enviar")
        }
    }
}



private fun hasLocationPermission(context: Context): Boolean {
    val fineLocationGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    val coarseLocationGranted = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    return fineLocationGranted && coarseLocationGranted
}

//@RequiresApi(Build.VERSION_CODES.O)
//@Composable
//fun WebSocketChatScreen(
//    viewModel: MainViewModel = viewModel()
//) {
//    val status by viewModel.socketStatus.collectAsState(false)
//    val messages by viewModel.messages.collectAsState(emptyList())
//    val listState = rememberLazyListState()
//
//    val context = LocalContext.current
//
//    var hasLocationPermission by remember {
//        mutableStateOf(hasLocationPermission(context))
//    }
//    val permissionLauncher = rememberLauncherForActivityResult(
//        contract = ActivityResultContracts.RequestMultiplePermissions(),
//        onResult = { permissions ->
//            hasLocationPermission = permissions.values.reduce { acc, isGranted -> acc && isGranted }
//        }
//    )
//
//    LaunchedEffect(messages.size) {
//        if (hasLocationPermission) {
//            listState.animateScrollToItem(messages.lastIndex)
//        }
//    }
//
//    Scaffold(
//        topBar = {
//            TopAppBar(
//                isConnected = status,
//                onConnect = viewModel::connect,
//                onDisconnect = viewModel::disconnect
//            )
//        }
//    ) { padding ->
//        Column(Modifier.padding(padding).fillMaxSize()) {
//            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
//                items (messages) {item ->
//                    MessageItem(item = item)
//                }
//            }
//            BottomPanel(onSend = viewModel::send)
//        }
//    }
//}
//
//@OptIn(ExperimentalMaterial3Api::class)
//@Composable
//private fun TopAppBar(
//    isConnected: Boolean,
//    onConnect: () -> Unit,
//    onDisconnect: () -> Unit,
//) {
//    val statusText = if (isConnected) "Connected" else "Disconnected"
//    val contentDesc = if (!isConnected) "Connect" else "Disconnect"
//    val buttonIcon = if (isConnected) Icons.Outlined.Close else Icons.Outlined.Check
//    TopAppBar(
//        title = { Text(statusText) },
//        actions = {
//            IconButton(onClick = {
//                if (!isConnected) {
//                    onConnect()
//                } else {
//                    onDisconnect()
//                }
//            }) {
//                Icon(imageVector = buttonIcon, contentDescription = contentDesc)
//            }
//        },
//    )
//}
//
//@Composable
//private fun MessageItem(item: Pair<Boolean, String>) {
//    val (iAmTheSender, message) = item
//    Text(
//        text = "${if (iAmTheSender) "You: " else "Other: "} $message",
//        modifier = Modifier.padding(8.dp)
//    )
//}
//
//@Composable
//private fun BottomPanel(
//    onSend: (String) -> Unit
//) {
//    var text by remember {
//        mutableStateOf("")
//    }
//    Row(Modifier.padding(8.dp)) {
//        OutlinedTextField(
//            value = text,
//            onValueChange = { s -> text = s },
//            modifier = Modifier.weight(1f),
//        )
//        TextButton(
//            onClick = {
//                onSend(text)
//                text = ""
//            },
//            enabled = text.isNotBlank(),
//        ) {
//            Text("Send")
//        }
//    }
//}