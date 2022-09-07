package dev.binflux.atcommand.environment;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BukkitCommandMap extends SimpleCommandMap {

    BukkitCommandEnvironment environment;

    public BukkitCommandMap(Server server, BukkitCommandEnvironment environment) {
        super(server);
        this.environment = environment;
    }

    protected void registerOldCommands(Map<String, Command> oldCommands) {
        this.knownCommands.putAll(oldCommands);
    }

    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String cmdLine) {
        cmdLine = cmdLine.startsWith("/") ? cmdLine : "/" + cmdLine;
        List<String> completions = environment.handleCompletions(sender, cmdLine, super.tabComplete(sender, cmdLine));
        completions.removeIf(comp -> comp.contains(":") || comp.equalsIgnoreCase(""));
        return completions;
    }
}