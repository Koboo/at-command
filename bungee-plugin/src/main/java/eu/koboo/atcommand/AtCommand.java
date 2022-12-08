package eu.koboo.atcommand;

import eu.koboo.atcommand.environment.BungeeCommandEnvironment;
import lombok.Getter;
import lombok.extern.java.Log;
import net.md_5.bungee.api.plugin.Plugin;

@Log
public class AtCommand extends Plugin {

    @Getter
    private static BungeeCommandEnvironment environment;

    @Override
    public void onLoad() {
        environment = new BungeeCommandEnvironment(this);
        log.info("Initialized " + BungeeCommandEnvironment.class.getName() + "!");
        super.onLoad();
    }

    @Override
    public void onDisable() {
        if (environment != null) {
            environment.destroy();
        }
        log.info("Destroyed " + BungeeCommandEnvironment.class.getName() + "!");
        super.onDisable();
    }
}