package dev.binflux.atcommand;

import dev.binflux.atcommand.environment.BungeeCommandEnvironment;
import dev.binflux.atcommand.listener.TabCompleteListener;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

@Log
public class AtCommandPlugin extends Plugin {

    @Getter
    private static BungeeCommandEnvironment environment;

    @Override
    public void onLoad() {
        environment = new BungeeCommandEnvironment(this);
        environment.addDependency(environment);
        log.info("Initialized " + BungeeCommandEnvironment.class.getName() + "!");
        super.onLoad();
    }

    @Override
    public void onEnable() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new TabCompleteListener(environment));
        log.info("Registered listeners for " + BungeeCommandEnvironment.class.getName() + "!");
        super.onEnable();
    }

    @Override
    public void onDisable() {
        if(environment != null) {
            environment.destroy();
        }
        log.info("Destroyed " + BungeeCommandEnvironment.class.getName() + "!");
        super.onDisable();
    }
}