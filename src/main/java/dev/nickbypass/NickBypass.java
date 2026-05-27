package dev.nickbypass;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashSet;
import java.util.UUID;

public class NickBypass extends JavaPlugin {

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