package se.gory_moon.mclink.velocity.gson;

import se.gory_moon.mclink.velocity.API;

import java.util.UUID;

public class UUIDRequest {

    @SuppressWarnings("FieldCanBeLocal")
    private String uuid;

    private String t;

    private UUIDRequest(String uuid, String t) {
        this.uuid = uuid;
        this.t = t;
    }

    public static String toGson(UUID uuid, String token) {
        return API.GSON.toJson(new UUIDRequest(uuid.toString(), token));
    }
}
