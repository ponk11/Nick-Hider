package dev.nickbypass;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final NickBypass plugin;
    private Essentials essentials;

    public ChatListener(NickBypass plugin) {
        this.plugin = plugin;
    }

    private Essentials getEssentials() {
        if (essentials == null) {
            var ess = Bukkit.getPluginManager().getPlugin("Essentials");
            if (ess instanceof Essentials api) {
                essentials = api;
            }
        }
        return essentials;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        if (!hasNickname(sender)) return;

        // Get the plain text of whatever the display name renders as (strips all formatting)
        String plainDisplayName = PlainTextComponentSerializer.plainText()
                .serialize(sender.displayName())
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "") // strip any legacy § codes just in case
                .trim();

        if (plainDisplayName.isEmpty() || plainDisplayName.equals(sender.getName())) return;

        Component realName = Component.text(sender.getName());

        event.viewers().removeIf(audience -> {
            if (!(audience instanceof Player viewer)) return false;
            if (!plugin.isBypassing(viewer)) return false;

            Component rendered = event.renderer().render(
                    sender,
                    sender.displayName(),
                    event.message(),
                    viewer
            );

            Component replaced = rendered.replaceText(
                    TextReplacementConfig.builder()
                            .matchLiteral(plainDisplayName)
                            .replacement(realName)
                            .build()
            );

            viewer.sendMessage(replaced);
            return true;
        });
    }

    private boolean hasNickname(Player player) {
        Essentials ess = getEssentials();
        if (ess == null) return false;

        User user = ess.getUser(player);
        if (user == null) return false;

        String nick = user.getNickname();
        return nick != null && !nick.isBlank() && !nick.equals(player.getName());
    }
}