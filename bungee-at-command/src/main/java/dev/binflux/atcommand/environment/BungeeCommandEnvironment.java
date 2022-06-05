package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.AtCommandPlugin;
import dev.binflux.atcommand.environment.meta.CommandMeta;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class BungeeCommandEnvironment extends CommandEnvironment {

    private final AtCommandPlugin plugin;

    public BungeeCommandEnvironment(AtCommandPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public Class<?> getPlayerClass() {
        return ProxiedPlayer.class;
    }

    @Override
    public Class<?> getConsoleClass() {
        return CommandSender.class;
    }

    @Override
    public <T> boolean hasNotPermission(T sender, String permission) {
        return !((CommandSender) sender).hasPermission(permission);
    }

    @Override
    public <T> void afterRegistration(T command, CommandMeta commandMeta) {
        if(commandMeta.isGlobalCommand()) {
            return;
        }
        List<String> aliasList = new ArrayList<>(commandMeta.getAliasList());
        String label = aliasList.get(0);
        aliasList.remove(0);
        BungeeRootCommand rootCommand = new BungeeRootCommand(this, label,
                commandMeta.getRootPermission(), aliasList.toArray(new String[]{}));
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, rootCommand);
    }
}