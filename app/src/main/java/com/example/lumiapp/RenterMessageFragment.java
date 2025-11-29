package com.example.lumiapp;

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

import io.getstream.chat.android.client.ChatClient;

public class RenterMessageFragment extends Fragment {

    private static final String TAG = "RenterMessageFragment";

    public RenterMessageFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        // fragment_renter_message.xml MUST contain a ComposeView with id @+id/compose_view
        return inflater.inflate(R.layout.fragment_renter_message, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        ChatClient client = ChatClient.instance();

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

        ComposeView composeView = view.findViewById(R.id.compose_view);
        if (composeView == null) {
            Log.e(TAG, "ComposeView (R.id.compose_view) not found in layout.");
            return;
        }

        // Delegate all UI to the Kotlin Compose helper
        RenterMessageCompose.setRenterMessageContent(composeView);
    }
}
