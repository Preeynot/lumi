@file:JvmName("RenterMessageCompose")

package com.example.lumiapp

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.ui.channels.header.ChannelListHeader
import io.getstream.chat.android.compose.ui.channels.list.ChannelList
import io.getstream.chat.android.compose.ui.theme.ChatTheme
import io.getstream.chat.android.compose.viewmodel.channels.ChannelListViewModel
import io.getstream.chat.android.compose.viewmodel.channels.ChannelViewModelFactory
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.FilterObject
import io.getstream.chat.android.models.Filters
import io.getstream.chat.android.models.querysort.QuerySortByField
import io.getstream.chat.android.models.querysort.QuerySorter

// Entry point from RenterMessageFragment (Java)
fun setRenterMessageContent(composeView: ComposeView) {
    composeView.setContent {
        ChatTheme {
            val client = ChatClient.instance()
            val currentUser = client.getCurrentUser()

            if (currentUser == null) {
                Toast
                    .makeText(
                        composeView.context,
                        "Chat service not connected.",
                        Toast.LENGTH_LONG
                    )
                    .show()
            } else {
                RenterInboxScreen(renterId = currentUser.id)
            }
        }
    }
}

@Composable
private fun RenterInboxScreen(renterId: String) {
    val context = LocalContext.current
    val client = ChatClient.instance()

    // 1) Filter: only "messaging" channels where this renter is a member
    val filters: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(renterId))
    )

    // 2) Sort: newest activity first
    val sort: QuerySorter<Channel> = QuerySortByField.descByName("last_updated")

    // 3) ViewModel factory (v6-style)
    val factory = ChannelViewModelFactory(
        chatClient = client,
        filters = filters,
        querySort = sort
    )

    // 4) ChannelListViewModel from the factory
    val listViewModel: ChannelListViewModel = viewModel(
        key = "renter-channel-list",
        factory = factory
    )

    // Current user for the header avatar
    val userState by listViewModel.user.collectAsState(initial = null)
    // Connection state required by ChannelListHeader
    val connectionState by listViewModel.connectionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp, bottom = 5.dp)
    ) {
        // Header (same Stream header as PM side, but renter-specific actions)
        ChannelListHeader(
            modifier = Modifier.fillMaxSize(),
            currentUser = userState,
            title = "Messages",
            connectionState = connectionState,
            onAvatarClick = {
                Toast.makeText(
                    context,
                    "Profile & settings coming soon",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onHeaderActionClick = {
                // For renters, we don't actually create groups yet.
                Toast.makeText(
                    context,
                    "Starting new chats will be available soon.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )

        // Optional: small text / hint (you can remove this if you don't like it)
        Text(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            text = "Chats with your Property Manager and building groups will appear here.",
            style = ChatTheme.typography.footnote
        )

        // Main channel list
        ChannelList(
            modifier = Modifier.fillMaxSize(),
            viewModel = listViewModel,
            onChannelClick = { channel ->
                // IMPORTANT: isPMUser = false for renter side
                val intent = ChannelActivity.newIntent(
                    context,
                    channel.cid,
                    false
                )
                context.startActivity(intent)
            }
        )
    }
}
