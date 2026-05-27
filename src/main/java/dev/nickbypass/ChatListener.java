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

    // HIGHEST — runs after LPC has already set its renderer, so we wrap theirs
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        if (!hasNickname(sender)) return;

        // Get the plain-text nickname from Essentials directly — most reliable source
        String plainNick = getPlainNickname(sender);
        if (plainNick == null || plainNick.equals(sender.getName())) return;

        String realName = sender.getName();

        // At HIGHEST, LPC's renderer is already set — we wrap it here
        var lpcRenderer = event.renderer();

        event.renderer((msgSender, senderDisplayName, message, viewer) -> {
            // Let LPC (or whoever) render it first
            Component rendered = lpcRenderer.render(msgSender, senderDisplayName, message, viewer);

            // Only swap for bypassing staff
            if (!(viewer instanceof Player viewerPlayer) || !plugin.isBypassing(viewerPlayer)) {
                return rendered;
            }

            // Replace the plain nickname text anywhere it appears in the rendered line
            return rendered.replaceText(
                    TextReplacementConfig.builder()
                            .matchLiteral(plainNick)
                            .replacement(Component.text(realName))
                            .build()
            );
        });
    }

    private String getPlainNickname(Player player) {
        Essentials ess = getEssentials();
        if (ess == null) return null;

        User user = ess.getUser(player);
        if (user == null) return null;

        String nick = user.getNickname();
        if (nick == null || nick.isBlank()) return null;

        // Strip legacy color/format codes to get the raw visible text
        return nick.replaceAll("(?i)§[0-9a-fk-or]", "").trim();
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