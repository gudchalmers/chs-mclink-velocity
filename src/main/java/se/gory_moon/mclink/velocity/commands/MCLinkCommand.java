package se.gory_moon.mclink.velocity.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.permission.Tristate;
import se.gory_moon.mclink.velocity.MCLinkPlugin;

/**
 * @author Gustaf Järgren
 * @version 2020-11-28
 */
public class MCLinkCommand {

    private final MCLinkPlugin plugin;

    public MCLinkCommand(MCLinkPlugin plugin) {
        this.plugin = plugin;
    }

    public void register(CommandManager manager) {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder.
                <CommandSource>literal("mclink")
                .requires(source -> source.getPermissionValue("mclink.reload") == Tristate.TRUE)
                .executes(context -> {
                    plugin.config.reload();
                    return 1;
                })
                .build();
        manager.register(new BrigadierCommand(node));
    }
}
