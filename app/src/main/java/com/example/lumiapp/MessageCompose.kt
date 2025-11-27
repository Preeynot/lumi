@file:JvmName("MessageCompose")

package com.example.lumiapp

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.messages.MessagesScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

// 1. Entry point from Java ChannelActivity
fun setMessageContent(composeView: ComposeView, channelCid: String, isPMUser: Boolean) {
    composeView.setContent {
        MessageScreen(channelCid, isPMUser, composeView)
    }
}

// 2. The main Composable function
@Composable
private fun MessageScreen(channelCid: String, isPMUser: Boolean, composeView: ComposeView) {
    val factory = MessagesViewModelFactory(
        context = composeView.context,
        channelId = channelCid,
        chatClient = ChatClient.instance()
    )

    ChatTheme {
        MessagesScreen(
            viewModelFactory = factory,
            onBackPressed = {
                (composeView.context as? ChannelActivity)?.finish()
            },


            // You can also add custom message bubble styles or composer customization here
        )
    }
}