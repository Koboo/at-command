package dev.binflux.atcommand;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;
import dev.binflux.atcommand.listener.PlayerCommandPreprocessListener;
import dev.binflux.atcommand.listener.ServerCommandListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AtCommandPlugin extends JavaPlugin {

    private BukkitCommandEnvironment environment;

    @Override
    public void onEnable() {
        environment = new BukkitCommandEnvironment(this);

        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(environment), this);
        Bukkit.getPluginManager().registerEvents(new ServerCommandListener(environment), this);
    }

    @Override
    public void onDisable() {
        environment = null;
    }

}