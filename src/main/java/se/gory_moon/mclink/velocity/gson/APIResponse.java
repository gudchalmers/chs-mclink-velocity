package se.gory_moon.mclink.velocity.gson;

import org.slf4j.Logger;
import se.gory_moon.mclink.velocity.API;

public class APIResponse {

    private String status;

    public APIResponse(String status) {
        this.status = status;
    }

    public static APIResponse denied() {
        return new APIResponse("denied");
    }

    public boolean isSuccess() {
        return "success".equals(status);
    }

    public static APIResponse fromGson(String response, Logger logger) {
        try {
            return API.GSON.fromJson(response, APIResponse.class);
        } catch (Exception e) {
            logger.error(response);
            logger.error("APIResponse error: ", e);
            return null;
        }
    }
}
