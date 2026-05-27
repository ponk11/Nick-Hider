package dev.nickbypass;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.earth2me.essentials.Essentials;
import com.earth2me.essentials.User;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;

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

    // Run at MONITOR (after everyone including LPC is done)
    // Send a separately-rendered message to bypassing staff
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        Player sender = event.getPlayer();

        if (!hasNickname(sender)) return;

        // Find bypassing staff in the viewer list
        List<Player> bypassViewers = new ArrayList<>();
        for (Audience audience : event.viewers()) {
            if (audience instanceof Player viewer && plugin.isBypassing(viewer)) {
                bypassViewers.add(viewer);
            }
        }

        if (bypassViewers.isEmpty()) return;

        // Remove bypassing staff from the normal viewer set so they don't get the nicknamed version
        event.viewers().removeAll(bypassViewers);

        // Re-render the message with the real username as display name, and send manually
        Component realDisplayName = Component.text(sender.getName());
        Component message = event.message();
        var renderer = event.renderer();

        for (Player viewer : bypassViewers) {
            Component rendered = renderer.render(sender, realDisplayName, message, viewer);
            viewer.sendMessage(rendered);
        }
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