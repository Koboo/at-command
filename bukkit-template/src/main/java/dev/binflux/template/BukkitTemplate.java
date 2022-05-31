package dev.binflux.template;

import org.bukkit.plugin.java.JavaPlugin;

public class BukkitTemplate extends JavaPlugin {

    private static BukkitTemplate instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public static BukkitTemplate getInstance() {
        return instance;
    }
}