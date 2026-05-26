package dev.nickbypass;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

        String nickname = getNickname(sender);
        if (nickname == null || nickname.equals(sender.getName())) return;

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
                            .matchLiteral(nickname)
                            .replacement(Component.text(sender.getName()))
                            .build()
            );

            viewer.sendMessage(replaced);
            return true; // remove from default viewers so they don't get it twice
        });
    }

    private String getNickname(Player player) {
        Essentials ess = getEssentials();
        if (ess == null) return null;

        User user = ess.getUser(player);
        if (user == null) return null;

        String nick = user.getNickname();
        if (nick == null || nick.isBlank()) return null;

        // Strip color/formatting codes to get plain text for matching
        Component nickComponent = LegacyComponentSerializer.legacyAmpersand().deserialize(nick);
        return PlainTextComponentSerializer.plainText().serialize(nickComponent);
    }
}