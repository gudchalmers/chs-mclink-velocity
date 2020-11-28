package se.gory_moon.mclink.velocity;

import com.google.gson.Gson;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.slf4j.Logger;
import se.gory_moon.mclink.velocity.gson.APIResponse;
import se.gory_moon.mclink.velocity.gson.UUIDRequest;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

public class API {

    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private static final OkHttpClient client = new OkHttpClient();
    private static final String CHECK = "check";
    private static final String UNREGISTER = "unregister";
    private final Config config;

    public API(Config config) {
        this.config = config;
    }

    public static final Gson GSON = new Gson();

    private static RequestBody getUUIDRequest(UUID uuid, String token) {
        return RequestBody.create(UUIDRequest.toGson(uuid, token), JSON);
    }

    public Optional<APIResponse> authorize(UUID uuid, String token, Logger logger) throws IOException {
        Request request = new Request.Builder()
                .url(config.getMcLinkBackend() + CHECK)
                .post(getUUIDRequest(uuid, token))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(APIResponse.fromGson(response.body().string(), logger));
        }
    }

    public boolean unregister(UUID uuid, String token, Logger logger) throws IOException {
        Request request = new Request.Builder()
                .url(config.getMcLinkBackend() + UNREGISTER)
                .post(getUUIDRequest(uuid, token))
                .build();
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful() || response.body() == null) {
                return false;
            }
            return APIResponse.fromGson(response.body().string(), logger).isSuccess();
        }
    }
}
