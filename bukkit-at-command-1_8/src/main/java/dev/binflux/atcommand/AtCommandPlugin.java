package dev.binflux.atcommand;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;
import dev.binflux.atcommand.listener.PlayerCommandPreprocessListener;
import dev.binflux.atcommand.listener.ServerCommandListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class AtCommandPlugin extends JavaPlugin {

    private BukkitCommandEnvironment environment;

    @Override
    public void onLoad() {
        environment = new BukkitCommandEnvironment(this);
        super.onLoad();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(environment), this);
        Bukkit.getPluginManager().registerEvents(new ServerCommandListener(environment), this);
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }

}