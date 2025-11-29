@file:JvmName("PmMessageCompose")

package com.example.lumiapp

import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

// Entry point from PMMessageFragment (Java)
fun setPmMessageContent(composeView: ComposeView) {
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
                PmInboxScreen(pmUserId = currentUser.id)
            }
        }
    }
}

@Composable
private fun PmInboxScreen(pmUserId: String) {
    val context = LocalContext.current
    val client = ChatClient.instance()

    // 1) Filter: only "messaging" channels where this PM is a member
    val filters: FilterObject = Filters.and(
        Filters.eq("type", "messaging"),
        Filters.`in`("members", listOf(pmUserId))
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
        key = "pm-channel-list",
        factory = factory
    )

    // Current user for the header avatar
    val userState by listViewModel.user.collectAsState(initial = null)
    // Connection state required by ChannelListHeader in this version
    val connectionState by listViewModel.connectionState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 5.dp, bottom = 5.dp) // breathing room for status / nav bars
    ) {

        // --- STREAM HEADER (original style) ---
        ChannelListHeader(
            modifier = Modifier.fillMaxWidth(),
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
                val intent = Intent(context, CreateGroupActivity::class.java)
                context.startActivity(intent)
            }
        )

        // --- GROUPS BANNER (Instagram notes vibe) ---
        GroupsBannerRow(
            pmUserId = pmUserId,
            onGroupClick = { cid ->
                try {
                    val intent = ChannelActivity.newIntent(
                        context,
                        cid,
                        true   // PM mode
                    )
                    context.startActivity(intent)
                } catch (t: Throwable) {
                    Log.e("PmInboxScreen", "Failed to open group channel: $cid", t)
                    Toast.makeText(
                        context,
                        "Could not open group chat.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )

        // --- MAIN CHANNEL LIST ---
        ChannelList(
            modifier = Modifier
                .fillMaxSize(),
            viewModel = listViewModel,
            onChannelClick = { channel ->
                val intent = ChannelActivity.newIntent(
                    context,
                    channel.cid,
                    true   // PM mode
                )
                context.startActivity(intent)
            }
        )
    }
}

/**
 * Simple, static "groups" row for now.
 *
 * For the moment we:
 *  - Show a note-style bubble for your seeded channel:
 *      messaging:gardening-society-building-a
 *  - On click, navigate straight into that channel.
 *
 * Later you can:
 *  - Query all channels with extraData["is_group"] = true && extraData["pm_id"] = pmUserId
 *  - Render each as a chip in a LazyRow.
 */
@Composable
private fun GroupsBannerRow(
    pmUserId: String,
    onGroupClick: (String) -> Unit
) {
    val context = LocalContext.current

    // CID for your seeded channel
    val gardeningCid = "messaging:gardening-society-building-a"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 12.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // One “note-style” chip for Gardening Society
        Column(
            modifier = Modifier
                .padding(end = 16.dp)
                .clickable { onGroupClick(gardeningCid) },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle icon (static for now)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(ChatTheme.colors.primaryAccent)
            ) {
                Text(
                    modifier = Modifier.align(Alignment.Center),
                    text = "GS",
                    style = ChatTheme.typography.bodyBold,
                    color = ChatTheme.colors.textHighEmphasis
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Label below the circle
            Text(
                text = "Gardening\nSociety",
                style = ChatTheme.typography.footnoteBold
            )
        }

        // Later: add more group chips here (Gym, Amenities, etc.)
    }
}
