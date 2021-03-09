package se.gory_moon.mclink.velocity.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ServerConnection;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.slf4j.Logger;
import se.gory_moon.mclink.velocity.Gate;
import se.gory_moon.mclink.velocity.MCLinkPlugin;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class UnregisterCommand {

    private final MCLinkPlugin plugin;
    private final Gate gate;
    private final Logger logger;

    public UnregisterCommand(MCLinkPlugin plugin, Gate gate, Logger logger) {
        this.plugin = plugin;
        this.gate = gate;
        this.logger = logger;
    }

    public int unregister(CommandContext<CommandSource> ctx) {
        final CommandSource source = ctx.getSource();
        if (source instanceof Player) {
            try {
                if (plugin.api.unregister(((Player) source).getUniqueId(), plugin.config.getToken(), logger)) {
                    gate.unregisterPlayer((Player) source);
                    Component msg = Component.text("You are now unregistered, to play on the server again you need to re register", NamedTextColor.GREEN);

                    boolean send = false;
                    Optional<ServerConnection> server = ((Player) source).getCurrentServer();
                    if (server.isPresent() && !plugin.config.getIgnoredServers().contains(server.get().getServerInfo().getName())) {
                        send = true;
                        msg = msg.append(Component.newline().append(Component.text("Disconnecting you in 5 seconds...", NamedTextColor.YELLOW)));
                    }

                    source.sendMessage(Identity.nil(), msg);
                    if (send) {
                        plugin.server.getScheduler().buildTask(plugin, () -> {
                            if (((Player) source).isActive()) {
                                ((Player) source).disconnect(Component.text("You need to register your account before you can join. Login to ", NamedTextColor.RED)
                                        .append(Component.text("auth.mc.chs.se", NamedTextColor.GOLD)));
                            }
                        }).delay(5, TimeUnit.SECONDS).schedule();
                    }
                } else {
                    source.sendMessage(Component.text("An error occurred when unregistering, please contact an admin!", NamedTextColor.RED));
                }
            } catch (IOException e) {
                source.sendMessage(Component.text("An error occurred when unregistering, please contact an admin!", NamedTextColor.RED));
            }
            return 1;
        }
        return -1;
    }

    public void register(CommandManager manager) {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder.
                <CommandSource>literal("unregister")
                .requires(source -> source instanceof Player && gate.isPlayerAuthed((Player) source))
                .executes(this::unregister)
                .build();

        manager.register(new BrigadierCommand(node));
    }
}
