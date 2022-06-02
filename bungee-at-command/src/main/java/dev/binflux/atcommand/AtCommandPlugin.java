package dev.binflux.atcommand;

import dev.binflux.atcommand.environment.BungeeCommandEnvironment;
import dev.binflux.atcommand.listener.TabCompleteListener;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

public class AtCommandPlugin extends Plugin {

    private BungeeCommandEnvironment environment;

    @Override
    public void onLoad() {
        environment = new BungeeCommandEnvironment(this);
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