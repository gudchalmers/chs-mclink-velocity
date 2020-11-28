package se.gory_moon.mclink.velocity;

import com.google.common.io.ByteArrayDataInput;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerPreConnectEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import org.slf4j.Logger;
import se.gory_moon.mclink.velocity.commands.MCLinkCommand;
import se.gory_moon.mclink.velocity.commands.UnregisterCommand;

import java.nio.file.Path;
import java.util.Optional;

@Plugin(id = "mclink", name = "MCLink Plugin", version = "1.0.0",
        description = "Plugin to auth users with the mclink backend", authors = {"GoryMoon"})
public class MCLinkPlugin extends AbstractModule {

    private static final MinecraftChannelIdentifier CONNECT_CHANNEL = MinecraftChannelIdentifier.create("mclink", "connect");

    public final ProxyServer server;
    private final Logger logger;
    public RegisteredServer authServer;
    public final Gate gate;
    public Config config;
    public API api;

    @Inject
    @DataDirectory
    private Path configDir;

    @Inject
    public MCLinkPlugin(ProxyServer server, CommandManager manager, Logger logger) {
        this.server = server;
        this.logger = logger;
        gate = new Gate(this, server, logger);

        new UnregisterCommand(this, gate, logger).register(manager);
        new MCLinkCommand(this).register(manager);
    }

    @Subscribe
    public void onProxyClose(ProxyShutdownEvent event) {
        gate.close();
    }

    @Subscribe
    public void onProxyInit(ProxyInitializeEvent event) {
        config = new Config(configDir.toFile());
        server.getChannelRegistrar().register(CONNECT_CHANNEL);
        api = new API(config);

        authServer = server.getServer(config.getAuthServer()).orElseThrow(NoServerFoundExecption::new);
    }

    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!event.getIdentifier().equals(CONNECT_CHANNEL))
            return;

        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection))
            return;

        ServerConnection connection = (ServerConnection) event.getSource();
        ByteArrayDataInput in = event.dataAsDataStream();

        Optional<RegisteredServer> server = this.server.getServer(in.readUTF());
        server.ifPresent(serverInfo -> {
            Player player = connection.getPlayer();
            gate.register(player);
            logger.info("Got message to send player {}[{}] to {}", player.getUsername(), player.getUniqueId(), serverInfo.getServerInfo().getName());
            player.createConnectionRequest(serverInfo).fireAndForget();
        });
    }

    @Subscribe
    public void playerLogin(LoginEvent event) {
        gate.checkAuthAsync(event.getPlayer(), event.getPlayer().hasPermission(config.getPermission()));
    }

    @Subscribe
    public void playerConnect(ServerPreConnectEvent event) {
        if (!authServer.getServerInfo().getName().equals(event.getOriginalServer().getServerInfo().getName())) {
            gate.login(event.getPlayer());
        }
    }
}
