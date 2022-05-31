package dev.binflux.atcommand.environment;

import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;

import java.util.List;

public class BukkitCommandMap extends SimpleCommandMap {

    private final BukkitCommandEnvironment environment;

    public BukkitCommandMap(Server server, BukkitCommandEnvironment environment) {
        super(server);
        this.environment = environment;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String cmdLine) {
        cmdLine = cmdLine.startsWith("/") ? cmdLine : "/" + cmdLine;
        List<String> completions = environment.handleCompletions(sender, cmdLine, super.tabComplete(sender, cmdLine));
        completions.removeIf(comp -> comp.contains(":") || comp.equalsIgnoreCase(""));
        return completions;
    }
}