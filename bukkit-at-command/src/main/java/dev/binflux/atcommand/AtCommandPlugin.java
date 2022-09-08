package dev.binflux.atcommand;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;
import dev.binflux.atcommand.listener.PlayerCommandPreprocessListener;
import dev.binflux.atcommand.listener.ServerCommandListener;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Log
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AtCommandPlugin extends JavaPlugin {

    BukkitCommandEnvironment environment;

    @Override
    public void onLoad() {
        environment = new BukkitCommandEnvironment(this);
        log.info("Initialized " + BukkitCommandEnvironment.class.getName() + "!");
        super.onLoad();
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(new PlayerCommandPreprocessListener(environment), this);
        Bukkit.getPluginManager().registerEvents(new ServerCommandListener(environment), this);
        environment.registerCommand(new TestCommand());
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}