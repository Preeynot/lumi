package com.example.backend;

import io.getstream.chat.java.models.Channel;
import io.getstream.chat.java.models.User;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class BootstrapChannels {

    public static void main(String[] args) throws Exception {
        // 1) Configure SDK with your API key + secret (server-side ONLY)
        System.setProperty("io.getstream.chat.apiKey", "thrwrmxh3e74");       // <-- your API key
        System.setProperty("io.getstream.chat.apiSecret", "g2rqsu64smvqy6jzn83495mxbnxdbq8j3yvtud5kuapearnmkd7sdw2shnt9vbwm"); // <-- your secret

        // 2) IDs
        String pmId = "6NYktZ4jNpNNnIDMboNOSl1sBFh1"; // your PM user id
        String renter1 = "renter-101";
        String renter2 = "renter-102";

        // 3) Build user request objects (note: User.UserRequestObject, not User.builder())
        User.UserRequestObject pmUser =
                User.UserRequestObject.builder()
                        .id(pmId)
                        .name("Property Manager")
                        .build();

        User.UserRequestObject renterUser1 =
                User.UserRequestObject.builder()
                        .id(renter1)
                        .name("Renter 101")
                        .build();

        User.UserRequestObject renterUser2 =
                User.UserRequestObject.builder()
                        .id(renter2)
                        .name("Renter 102")
                        .build();

        // 4) Upsert all users in one request
        User.UserUpsertRequestData.UserUpsertRequest upsertRequest = User.upsert();
        upsertRequest.user(pmUser);
        upsertRequest.user(renterUser1);
        upsertRequest.user(renterUser2);
        upsertRequest.request();

        // 5) Define channel extra data
        String channelType = "messaging";
        String channelId = "gardening-society-building-a";

        Map<String, Object> extraData = new HashMap<>();
        extraData.put("name", "Gardening Society 🌱");
        extraData.put("is_group", true);
        extraData.put("group_type", "society");
        extraData.put("pm_id", pmId);

        // 6) Build ChannelRequestObject using the request objects above
        Channel.ChannelRequestObject.ChannelRequestObjectBuilder builder =
                Channel.ChannelRequestObject.builder()
                        .createdBy(pmUser)
                        .members(Arrays.asList(
                                Channel.ChannelMemberRequestObject.builder()
                                        .userId(pmId)
                                        .build(),
                                Channel.ChannelMemberRequestObject.builder()
                                        .userId(renter1)
                                        .build(),
                                Channel.ChannelMemberRequestObject.builder()
                                        .userId(renter2)
                                        .build()
                        ));

// Add custom fields from extraData via additionalField(...)
        for (Map.Entry<String, Object> entry : extraData.entrySet()) {
            builder.additionalField(entry.getKey(), entry.getValue());
        }

        Channel.ChannelRequestObject channelData = builder.build();

        // 7) Create or load the channel (we don't need the response object here)
        Channel.getOrCreate(channelType, channelId)
                .data(channelData)
                .request();

        System.out.println("Created / loaded channel: " + channelType + ":" + channelId);
    }
}
