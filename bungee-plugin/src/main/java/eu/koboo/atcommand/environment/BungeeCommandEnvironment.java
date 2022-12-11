package eu.koboo.atcommand.environment;

import eu.koboo.atcommand.environment.meta.CommandMeta;
import eu.koboo.atcommand.listener.TabCompleteListener;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BungeeCommandEnvironment extends CommandEnvironment {

    Plugin plugin;

    public BungeeCommandEnvironment(Plugin plugin) {
        this.plugin = plugin;

        // Adding dependencies
        addDependency(this);

        // Register listeners
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, new TabCompleteListener(this));
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
    public <S> void sendSenderMessage(S sender, String message) {
        if (!(sender instanceof CommandSender)) {
            return;
        }
        CommandSender commandSender = (CommandSender) sender;
        commandSender.sendMessage(TextComponent.fromLegacyText(message));
    }

    @Override
    public <S> boolean hasPermission(S sender, String permission) {
        return !((CommandSender) sender).hasPermission(permission);
    }

    @Override
    public <T> void afterRegistration(T command, CommandMeta commandMeta) {
        List<String> aliasList = new ArrayList<>(commandMeta.getAliasList());
        String label = aliasList.get(0);
        aliasList.remove(0);
        BungeeRootCommand rootCommand = new BungeeRootCommand(this, label,
                commandMeta.getRootPermission(), aliasList.toArray(new String[]{}));
        ProxyServer.getInstance().getPluginManager().registerCommand(plugin, rootCommand);
    }
}