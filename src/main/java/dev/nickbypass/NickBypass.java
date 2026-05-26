package dev.nickbypass;

import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class NickBypass extends JavaPlugin {

    // Players who have manually toggled bypass ON via /nickbypass
    private final HashSet<UUID> bypassEnabled = new HashSet<>();

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getCommand("nickbypass").setExecutor(new NickBypassCommand(this));
        getLogger().info("NickBypass enabled.");
    }

    @Override
    public void onDisable() {
        bypassEnabled.clear();
    }

    /**
     * Returns true if this player should see real usernames.
     * True if they have nickbypass.auto permission OR have manually toggled it on.
     */
    public boolean isBypassing(org.bukkit.entity.Player player) {
        return player.hasPermission("nickbypass.auto") || bypassEnabled.contains(player.getUniqueId());
    }

    public boolean toggleBypass(org.bukkit.entity.Player player) {
        UUID uuid = player.getUniqueId();
        if (bypassEnabled.contains(uuid)) {
            bypassEnabled.remove(uuid);
            return false; // now OFF
        } else {
            bypassEnabled.add(uuid);
            return true; // now ON
        }
    }
}
