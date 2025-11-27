@file:JvmName("PmMessageCompose")

package com.example.lumiapp

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import io.getstream.chat.android.compose.ui.channels.ChannelsScreen
import io.getstream.chat.android.compose.ui.theme.ChatTheme

// Called from PMMessageFragment (Java)
fun setPmMessageContent(composeView: ComposeView) {
    composeView.setContent {
        PmChannelsScreen()
    }
}

@Composable
private fun PmChannelsScreen() {
    val context = LocalContext.current

    ChatTheme {
        ChannelsScreen(
            title = "Messages",
            onChannelClick = { channel ->
                // For now we treat this as PM user; you can wire real role logic later
                val isPMUser = true
                context.startActivity(
                    ChannelActivity.newIntent(
                        context,
                        channel.cid,
                        isPMUser
                    )
                )
            },
            onBackPressed = {
                (context as? FragmentActivity)
                    ?.onBackPressedDispatcher
                    ?.onBackPressed()
            }
        )
    }
}
