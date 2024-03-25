package se.gory_moon.mclink.velocity;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import se.gory_moon.mclink.velocity.gson.APIResponse;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Gate {

    private final Cache<UUID, APIResponse> CACHE = CacheBuilder.newBuilder().expireAfterWrite(24, TimeUnit.HOURS).build();
    private final ConcurrentHashMap<UUID, Status> UUID_STATUS = new ConcurrentHashMap<>();

    private final MCLinkPlugin plugin;
    private final ProxyServer server;
    private final Logger logger;

    public Gate(MCLinkPlugin plugin, ProxyServer server, Logger logger) {
        this.plugin = plugin;
        this.server = server;
        this.logger = logger;
    }

    public void checkAuthAsync(Player player, boolean privileged) {
        Consumer<Runnable> runner = r -> server.getScheduler().buildTask(plugin, r).schedule();

        if (privileged) {
            logger.info("Player {}[{}] is privileged so they are allowed in.", player.getUsername(), player.getUniqueId());
            UUID_STATUS.put(player.getUniqueId(), Status.ALLOWED);
            runner.accept(() -> check(player, false));
            return;
        }

        UUID_STATUS.put(player.getUniqueId(), Status.IN_PROGRESS);
        logger.info("Player {}[{}] is being checked...", player.getUsername(), player.getUniqueId());
        runner.accept(() -> check(player, true));
    }

    private void check(Player player, boolean auth) {
        try {
            Optional<APIResponse> response = Optional.ofNullable(CACHE.getIfPresent(player.getUniqueId()));
            if (!response.isPresent())
                response = plugin.api.authorize(player.getUniqueId(), plugin.config.getToken(), logger);
            if ((!response.isPresent() || !response.get().isSuccess()) && auth) {
                logger.info("Player {}[{}] was denied by authorization.", player.getUsername(), player.getUniqueId());
                if (UUID_STATUS.put(player.getUniqueId(), Status.DENIED_AUTH) == null) {
                    UUID_STATUS.remove(player.getUniqueId());
                    authComplete(player, Status.DENIED_AUTH);
                }
            } else {
                response.filter(APIResponse::isSuccess).ifPresent(apiResponse -> CACHE.put(player.getUniqueId(), apiResponse));
                logger.info("Player {}[{}] was authorized!", player.getUsername(), player.getUniqueId());
                if (UUID_STATUS.put(player.getUniqueId(), Status.ALLOWED) == null) {
                    UUID_STATUS.remove(player.getUniqueId());
                }
            }
        } catch (Exception e) {
            logger.error("Player {}[{}] was denied because of an exception.", player.getUsername(), player.getUniqueId());
            logger.error("Check Exception", e);
            if (UUID_STATUS.put(player.getUniqueId(), Status.DENIED_ERROR) == null) {
                UUID_STATUS.remove(player.getUniqueId());
                authComplete(player, Status.DENIED_ERROR);
            }
        }
    }

    public void login(Player player) {
        Status status = UUID_STATUS.remove(player.getUniqueId());
        if (status == null) {
            if (CACHE.getIfPresent(player.getUniqueId()) == null) {
                authComplete(player, Status.DENIED_AUTH);
            }
            return;
        }
        switch (status) {
            case ALLOWED:
            case IN_PROGRESS:
                break;
            default:
                authComplete(player, status);
        }
    }

    private void authComplete(Player player, Status status) {
        server.getScheduler().buildTask(plugin, () -> {
            if (status != Status.ALLOWED && player != null) {
                player.disconnect(Component.text("You need to register your account before you can join. Login to ", NamedTextColor.RED)
                        .append(Component.text("mc.chs.se", NamedTextColor.GOLD)));
            }
        }).schedule();
    }

    public boolean isPlayerAuthed(Player player) {
        return CACHE.getIfPresent(player.getUniqueId()) != null;
    }

    public void register(Player player) {
        CACHE.put(player.getUniqueId(), new APIResponse("success"));
    }

    public void unregisterPlayer(Player player) {
        CACHE.invalidate(player.getUniqueId());
    }

    public void close() {
        CACHE.invalidateAll();
        CACHE.cleanUp();
        UUID_STATUS.clear();
    }

    public enum Status {
        ALLOWED,
        IN_PROGRESS,
        DENIED_AUTH,
        DENIED_ERROR
    }
}
