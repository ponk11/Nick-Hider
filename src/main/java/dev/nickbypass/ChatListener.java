package dev.nickbypass;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

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

        String plainDisplayName = PlainTextComponentSerializer.plainText()
                .serialize(sender.displayName())
                .replaceAll("§[0-9a-fk-orA-FK-OR]", "")
                .trim();

        if (plainDisplayName.isEmpty() || plainDisplayName.equals(sender.getName())) return;

        // Store values for use in the renderer
        String realName = sender.getName();
        Component displayName = sender.displayName();
        var originalRenderer = event.renderer();

        // Replace the renderer with a per-viewer one
        event.renderer((msgSender, senderDisplayName, message, viewer) -> {
            // Render the message using the original renderer first
            Component rendered = originalRenderer.render(msgSender, senderDisplayName, message, viewer);

            // If the viewer is a bypassing staff member, swap the nickname for the real name
            if (viewer instanceof Player viewerPlayer && plugin.isBypassing(viewerPlayer)) {
                rendered = rendered.replaceText(
                        TextReplacementConfig.builder()
                                .matchLiteral(plainDisplayName)
                                .replacement(Component.text(realName))
                                .build()
                );
            }

            return rendered;
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