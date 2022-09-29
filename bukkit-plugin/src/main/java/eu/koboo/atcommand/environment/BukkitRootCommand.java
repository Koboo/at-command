package eu.koboo.atcommand.environment;

import eu.koboo.atcommand.parser.ParserUtility;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.java.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Log
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BukkitRootCommand extends Command {

    BukkitCommandEnvironment environment;

    public BukkitRootCommand(BukkitCommandEnvironment environment, String name) {
        super(name);
        this.environment = environment;
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        StringBuilder argumentString = new StringBuilder();
        for (String arg : args) {
            argumentString.append(" ").append(arg);
        }
        String commandString = commandLabel + argumentString;
        if (environment == null) {
            return false;
        }
        environment.handleCommand(sender, commandString);
        return false;
    }

    @NotNull
    @Override
    public List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) throws IllegalArgumentException {
        StringBuilder buffer = new StringBuilder();
        for (String arg : args) {
            buffer.append(" ").append(arg);
        }
        String cmdLine = "/" + alias + buffer;
        List<String> completions = environment.handleCompletions(sender, cmdLine, new ArrayList<>());
        completions.removeIf(comp -> comp.contains(":") || comp.equalsIgnoreCase(""));

        // Add player names as default completions (quality of life feature)
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
                                .collect(Collectors.toList())
                );
            }
        }
        return completions;
    }
}