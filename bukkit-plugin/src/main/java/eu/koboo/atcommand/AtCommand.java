package eu.koboo.atcommand;

import eu.koboo.atcommand.environment.BukkitCommandEnvironment;
import eu.koboo.atcommand.listener.PlayerCommandPreprocessListener;
import eu.koboo.atcommand.listener.ServerCommandListener;
import eu.koboo.atcommand.parser.GameModeParser;
import eu.koboo.atcommand.parser.SoundParser;
import eu.koboo.atcommand.parser.WorldParser;
import lombok.Getter;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

@Log
public class AtCommand extends JavaPlugin {

    @Getter
    private static BukkitCommandEnvironment environment;

    @Override
    public void onLoad() {
        environment = new BukkitCommandEnvironment(this);
        log.info("Initialized " + BukkitCommandEnvironment.class.getName() + "!");
        super.onLoad();
    }

    @Override
    public void onDisable() {
        if (environment != null) {
            environment.destroy();
        }
        log.info("Destroyed " + BukkitCommandEnvironment.class.getName() + "!");
        super.onDisable();
    }
}