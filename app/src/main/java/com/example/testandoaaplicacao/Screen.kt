package com.example.testandoaaplicacao

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun WebSocketChatScreen(
    viewModel: MainViewModel = viewModel()
) {
    val status by viewModel.socketStatus.collectAsState(false)
    val messages by viewModel.messages.collectAsState(emptyList())

    val listState = rememberLazyListState()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                isConnected = status,
                onConnect = viewModel::connect,
                onDisconnect = viewModel::disconnect
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                items (messages) {item ->
                    MessageItem(item = item)
                }
            }
            BottomPanel(onSend = viewModel::send)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopAppBar(
    isConnected: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit,
) {
    val statusText = if (isConnected) "Connected" else "Disconnected"
    val contentDesc = if (!isConnected) "Connect" else "Disconnect"
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
    Text(
        text = "${if (iAmTheSender) "You: " else "Other: "} $message",
        modifier = Modifier.padding(8.dp)
    )
}

@Composable
private fun BottomPanel(
    onSend: (String) -> Unit
) {
    var text by remember {
        mutableStateOf("")
    }
    Row(Modifier.padding(8.dp)) {
        OutlinedTextField(
            value = text,
            onValueChange = { s -> text = s },
            modifier = Modifier.weight(1f),
        )
        TextButton(
            onClick = {
                onSend(text)
                text = ""
            },
            enabled = text.isNotBlank(),
        ) {
            Text("Send")
        }
    }
}