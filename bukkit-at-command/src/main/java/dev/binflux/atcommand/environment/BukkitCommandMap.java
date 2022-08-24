package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.parser.ParserUtility;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Locale;

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
        if (completions.isEmpty()) {
            String[] arguments = ParserUtility.getArguments(cmdLine);
            if (arguments.length > 0) {
                String lastArgument = arguments[arguments.length - 1].toLowerCase(Locale.ROOT);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if(player.getName().toLowerCase(Locale.ROOT).startsWith(lastArgument) && !completions.contains(player.getName())) {
                        completions.add(player.getName());
                    }
                }
            } else {
                completions.addAll(
                        Bukkit.getOnlinePlayers()
                                .parallelStream()
                                .map(Player::getName)
                                .toList()
                );
            }
        }
        return completions;
    }
}