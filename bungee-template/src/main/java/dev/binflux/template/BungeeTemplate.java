package dev.binflux.template;

import net.md_5.bungee.api.plugin.Plugin;

public class BungeeTemplate extends Plugin {

    private static BungeeTemplate instance;

    @Override
    public void onEnable() {
        instance = this;
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    public BungeeTemplate getInstance() {
        return instance;
    }
}