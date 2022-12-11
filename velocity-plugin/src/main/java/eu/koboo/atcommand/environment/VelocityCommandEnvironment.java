package eu.koboo.atcommand.environment;

import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand.Invocation;
import com.velocitypowered.api.proxy.ConsoleCommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.ArrayList;
import java.util.List;

public class VelocityCommandEnvironment extends CommandEnvironment {

    Object plugin;
    ProxyServer proxyServer;

    public VelocityCommandEnvironment(Object plugin, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.proxyServer = proxyServer;
    }

    @Override
    public Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public Class<?> getConsoleClass() {
        return ConsoleCommandSource.class;
    }

    @Override
    public <S> void sendSenderMessage(S sender, String message) {
        if(!(sender instanceof Invocation)) {
            return;
        }
        Component component = LegacyComponentSerializer.legacyAmpersand().deserialize(message);
        Invocation invocation = (Invocation) sender;
        invocation.source().sendMessage(component);
    }

    @Override
    public <S> boolean hasPermission(S sender, String permission) {
        if(!(sender instanceof Invocation)) {
            return false;
        }
        Invocation invocation = (Invocation) sender;
        return invocation.source().hasPermission(permission);
    }

    @Override
    public <C> void afterRegistration(C command, eu.koboo.atcommand.environment.meta.CommandMeta commandMeta) {
        CommandManager manager = proxyServer.getCommandManager();
        List<String> aliasList = new ArrayList<>(commandMeta.getAliasList());
        String label = aliasList.get(0);
        aliasList.remove(0);
        CommandMeta velocityMeta = manager.metaBuilder(label)
                .aliases(aliasList.toArray(new String[]{}))
                .plugin(plugin)
                .build();
        VelocityRootCommand rootCommand = new VelocityRootCommand(this, proxyServer);
        manager.register(velocityMeta, rootCommand);
    }
}
