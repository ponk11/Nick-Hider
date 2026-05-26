package dev.nickbypass;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NickBypassCommand implements CommandExecutor {

    private final NickBypass plugin;

    public NickBypassCommand(NickBypass plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.", NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("nickbypass.toggle")) {
            player.sendMessage(Component.text("You don't have permission to use this command.", NamedTextColor.RED));
            return true;
        }

        // Players with nickbypass.auto are always bypassing — toggling is redundant for them
        if (player.hasPermission("nickbypass.auto")) {
            player.sendMessage(Component.text("You always see real usernames (nickbypass.auto is active).", NamedTextColor.YELLOW));
            return true;
        }

        boolean nowEnabled = plugin.toggleBypass(player);

        if (nowEnabled) {
            player.sendMessage(Component.text("Nick bypass ", NamedTextColor.GRAY)
                    .append(Component.text("ENABLED", NamedTextColor.GREEN))
                    .append(Component.text(" — you will now see real usernames in chat.", NamedTextColor.GRAY)));
        } else {
            player.sendMessage(Component.text("Nick bypass ", NamedTextColor.GRAY)
                    .append(Component.text("DISABLED", NamedTextColor.RED))
                    .append(Component.text(" — you will now see nicknames in chat.", NamedTextColor.GRAY)));
        }

        return true;
    }
}
