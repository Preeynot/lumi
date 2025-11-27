package com.example.lumiapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.platform.ComposeView;

import io.getstream.chat.android.client.ChatClient;


public class ChannelActivity extends AppCompatActivity {

    private final static String CID_KEY = "key:cid";
    private final static String IS_PM_KEY = "key:is_pm";
    private static final String TAG = "ChannelActivity";


    public static Intent newIntent(Context context, String channelCid, boolean isPM) {
        Intent intent = new Intent(context, ChannelActivity.class);
        intent.putExtra(CID_KEY, channelCid);
        intent.putExtra(IS_PM_KEY, isPM);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (ChatClient.instance().getCurrentUser() == null) {
            Toast.makeText(this, "Chat service not connected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String channelCid = getIntent().getStringExtra(CID_KEY);
        boolean isPMUser = getIntent().getBooleanExtra(IS_PM_KEY, false);

        if (channelCid == null) {
            Log.e(TAG, "Channel CID is missing.");
            Toast.makeText(this, "Could not load chat.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 1. Set the layout containing only the ComposeView
        setContentView(R.layout.activity_channel);

        ComposeView composeView = findViewById(R.id.compose_view);
        if (composeView != null) {
            // 2. Delegate UI rendering to the Kotlin Compose helper
            MessageCompose.setMessageContent(composeView, channelCid, isPMUser);
        } else {
            Log.e(TAG, "ComposeView (R.id.compose_view) not found in layout.");
            Toast.makeText(this, "UI error.", Toast.LENGTH_LONG).show();
            finish();
        }
    }
}