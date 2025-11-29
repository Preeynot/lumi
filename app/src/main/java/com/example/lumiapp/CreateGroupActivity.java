package com.example.lumiapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.channel.ChannelClient;
import io.getstream.chat.android.models.Channel;
import io.getstream.chat.android.models.User;
import io.getstream.result.Result;
import io.getstream.chat.android.client.errors.ChatError;



public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameInput;
    private EditText memberIdsInput;
    private Button createGroupButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        setTitle("Create Group");

        groupNameInput = findViewById(R.id.editTextGroupName);
        memberIdsInput = findViewById(R.id.editTextMemberIds);
        createGroupButton = findViewById(R.id.buttonCreateGroup);

        ChatClient client = ChatClient.instance();
        User currentUser = client.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "Chat service not connected.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        String pmId = currentUser.getId();

        createGroupButton.setOnClickListener(v -> {
            String groupName = groupNameInput.getText().toString().trim();
            String memberIdsRaw = memberIdsInput.getText().toString().trim();

            if (TextUtils.isEmpty(groupName)) {
                Toast.makeText(this, "Please enter a group name.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Parse renter IDs (comma-separated)
            List<String> memberIds = new ArrayList<>();
            if (!TextUtils.isEmpty(memberIdsRaw)) {
                String[] parts = memberIdsRaw.split(",");
                for (String p : parts) {
                    String id = p.trim();
                    if (!id.isEmpty()) {
                        memberIds.add(id);
                    }
                }
            }

            // Always include the PM as a member
            memberIds.add(pmId);

            createGroup(pmId, groupName, memberIds);
        });
    }

    private void createGroup(String pmId, String groupName, List<String> memberIds) {
        ChatClient client = ChatClient.instance();

        // Create a unique channel ID (simple example)
        String channelId = "group-" + System.currentTimeMillis();

        // Extra data for tagging this as a PM group
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", groupName);
        extraData.put("is_group", true);
        extraData.put("group_type", "pm_group");
        extraData.put("pm_id", pmId);

        ChannelClient channelClient = client.channel("messaging", channelId);

        channelClient.create(memberIds, extraData).enqueue(result -> {
            // Handle the result (success or error)
            handleCreateResult(result);
        });
    }

    private void handleCreateResult(Result<? extends Channel> result) {
        if (result.isSuccess()) {
            Channel channel = result.getOrNull();
            String cid = channel.getCid();

            Toast.makeText(this, "Group created!", Toast.LENGTH_SHORT).show();

            // Open the new channel in PM mode
            startActivity(ChannelActivity.newIntent(this, cid, true));
            finish();
        } else {
            Toast.makeText(
                    this,
                    "Failed to create group: " + result.errorOrNull().getMessage(),
                    Toast.LENGTH_LONG
            ).show();
        }
    }
}
