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
        log.info("Initialized " + BungeeCommandEnvironment.class.getName() + "!");
        super.onLoad();
    }

    @Override
    public void onEnable() {
        ProxyServer.getInstance().getPluginManager().registerListener(this, new TabCompleteListener(environment));
        super.onEnable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
    }
}