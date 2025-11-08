package com.example.lumiapp.features.video;

import com.google.android.gms.tasks.Task;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class AgoraTokenFunctions {

    public static Task<String> getAgoraToken(String channelName) {
        Map<String, Object> data = new HashMap<>();
        data.put("channel", channelName);
        return FirebaseFunctions.getInstance()
                .getHttpsCallable("getAgoraToken")
                .call(data)
                .continueWith(t -> (String)((Map)t.getResult().getData()).get("token"));
    }

    public static Task<String> triggerDoorbell(String propertyId, String unitId) {
        Map<String, Object> data = new HashMap<>();
        data.put("propertyId", propertyId);
        data.put("unitId", unitId);
        return FirebaseFunctions.getInstance()
                .getHttpsCallable("onDoorbellPressed")
                .call(data)
                .continueWith(t -> (String)((Map)t.getResult().getData()).get("channel"));
    }
}
