package com.example.lumiapp.features.video;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import io.agora.rtc2.ChannelMediaOptions;
import io.agora.rtc2.Constants;
import io.agora.rtc2.IRtcEngineEventHandler;
import io.agora.rtc2.RtcEngine;
import io.agora.rtc2.video.VideoCanvas;

import com.example.lumiapp.R;

public class CallActivity extends AppCompatActivity {

    private RtcEngine engine;
    // Dev convenience only — keep App Certificate in Cloud Functions. Token still required to join.
    private String appId = "1fc5df3af19645b09e82f1894c3938eb";

    private String channel;
    private FrameLayout remoteC, localC;
    private SurfaceView remoteView, localView;

    private final IRtcEngineEventHandler handler = new IRtcEngineEventHandler() {
        @Override public void onUserJoined(int uid, int elapsed) {
            runOnUiThread(() -> setupRemoteVideo(uid));
        }
        @Override public void onUserOffline(int uid, int reason) {
            runOnUiThread(() -> finish());
        }
    };

    private final ActivityResultLauncher<String[]> permReq =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), r -> {
                boolean cam = Boolean.TRUE.equals(r.getOrDefault(Manifest.permission.CAMERA, false));
                boolean mic = Boolean.TRUE.equals(r.getOrDefault(Manifest.permission.RECORD_AUDIO, false));
                if (cam && mic) initAndJoin();
                else { Toast.makeText(this, "Camera/Mic required", Toast.LENGTH_LONG).show(); finish(); }
            });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call);

        remoteC = findViewById(R.id.remote_video_container);
        localC  = findViewById(R.id.local_video_container);
        ImageButton btnEnd = findViewById(R.id.btnEndCall);
        ImageButton btnFlip = findViewById(R.id.btnFlip);

        btnEnd.setOnClickListener(v -> quit());
        btnFlip.setOnClickListener(v -> { if (engine != null) engine.switchCamera(); });

        channel = getIntent().getStringExtra("channelName");
        if (channel == null || channel.isEmpty()) {
            Toast.makeText(this, "Missing channel", Toast.LENGTH_SHORT).show();
            finish(); return;
        }

        if (!has(Manifest.permission.CAMERA) || !has(Manifest.permission.RECORD_AUDIO)) {
            permReq.launch(new String[]{Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO});
        } else {
            initAndJoin();
        }
    }

    private boolean has(String p) {
        return ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED;
    }

    private void initAndJoin() {
        try {
            engine = RtcEngine.create(getApplicationContext(), appId, handler);
            engine.enableVideo();

            // Local preview
            localView = new SurfaceView(getApplicationContext());
            localView.setZOrderMediaOverlay(true);
            engine.setupLocalVideo(new VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0));
            localC.addView(localView);

            // Fetch token from Functions then join
            AgoraTokenFunctions.getAgoraToken(channel)
                    .addOnSuccessListener(token -> {
                        ChannelMediaOptions opts = new ChannelMediaOptions();
                        opts.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER;
                        opts.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION;
                        engine.joinChannel(token, channel, 0, opts);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Token error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    });

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Init error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupRemoteVideo(int uid) {
        if (remoteView != null) return;
        remoteView = new SurfaceView(getApplicationContext());
        engine.setupRemoteVideo(new VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid));
        remoteC.addView(remoteView);
    }

    private void quit() {
        if (engine != null) {
            engine.leaveChannel();
            RtcEngine.destroy();
            engine = null;
        }
        finish();
    }

    @Override protected void onDestroy() { super.onDestroy(); quit(); }
}
