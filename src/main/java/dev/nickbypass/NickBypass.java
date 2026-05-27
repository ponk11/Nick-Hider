package dev.nickbypass;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class NickBypass extends JavaPlugin {

    private final HashSet<UUID> bypassEnabled = new HashSet<>();

    @Override
    public void onEnable() {
        // Create plugin folder and default config
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getCommand("nickbypass").setExecutor(new NickBypassCommand(this));
        getLogger().info("NickBypass enabled successfully.");

        // Verify Essentials is loaded
        if (getServer().getPluginManager().getPlugin("Essentials") == null) {
            getLogger().severe("Essentials not found! NickBypass will not work.");
        } else {
            getLogger().info("Essentials hooked successfully.");
        }
    }

    @Override
    public void onDisable() {
        bypassEnabled.clear();
        getLogger().info("NickBypass disabled.");
    }

    public boolean isBypassing(Player player) {
        return player.hasPermission("nickbypass.auto") || bypassEnabled.contains(player.getUniqueId());
    }

    public boolean toggleBypass(Player player) {
        UUID uuid = player.getUniqueId();
        if (bypassEnabled.contains(uuid)) {
            bypassEnabled.remove(uuid);
            return false;
        } else {
            bypassEnabled.add(uuid);
            return true;
        }
    }
}