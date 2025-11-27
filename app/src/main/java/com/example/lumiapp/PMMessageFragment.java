package com.example.lumiapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.compose.ui.platform.ComposeView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import io.getstream.chat.android.client.ChatClient;


public class PMMessageFragment extends Fragment {

    private static final String TAG = "PMMessageFragment";

    public PMMessageFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // fragment_pm_message.xml MUST contain a <androidx.compose.ui.platform.ComposeView>
        // with id @+id/compose_view
        return inflater.inflate(R.layout.fragment_pm_message, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ChatClient client = ChatClient.instance();

        // ✅ Check that the Stream user is connected
        if (client.getCurrentUser() == null) {
            Log.e(TAG, "Stream user is not connected. Cannot show channels.");
            if (getContext() != null) {
                Toast.makeText(
                        getContext(),
                        "Connecting to chat service...",
                        Toast.LENGTH_SHORT
                ).show();
            }
            return;
        }

        // 1. Find the ComposeView, which will host all Stream Chat UI
        ComposeView composeView = view.findViewById(R.id.compose_view);
        if (composeView == null) {
            Log.e(TAG, "ComposeView (R.id.compose_view) not found in layout.");
            return;
        }

        // 🔑 Delegate all Jetpack Compose / Stream UI to a Kotlin helper
        // This function PmMessageCompose.setPmMessageContent() handles ALL channel list logic,
        // including filtering, sorting, and click handling.
        PmMessageCompose.setPmMessageContent(composeView);

    }

}
