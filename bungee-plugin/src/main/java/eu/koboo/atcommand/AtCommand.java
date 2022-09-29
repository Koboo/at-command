package eu.koboo.atcommand;

import eu.koboo.atcommand.environment.BungeeCommandEnvironment;
import eu.koboo.atcommand.listener.TabCompleteListener;
import lombok.Getter;
import lombok.extern.java.Log;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

@Log
public class AtCommand extends Plugin {

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