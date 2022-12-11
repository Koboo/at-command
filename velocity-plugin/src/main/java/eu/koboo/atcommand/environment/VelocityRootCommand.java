package eu.koboo.atcommand.environment;

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import eu.koboo.atcommand.parser.ParserUtility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VelocityRootCommand implements SimpleCommand {

    VelocityCommandEnvironment environment;
    ProxyServer proxyServer;

    @Override
    public void execute(Invocation invocation) {
        CommandSource sender = invocation.source();
        StringBuilder argumentString = new StringBuilder();
        for (String arg : invocation.arguments()) {
            argumentString.append(" ").append(arg);
        }
        String commandString = invocation.alias() + argumentString;
        if (environment == null) {
            return;
        }
        environment.handleCommand(sender, commandString);
    }

    @Override
    public List<String> suggest(Invocation invocation) {

        StringBuilder buffer = new StringBuilder();
        for (String arg : invocation.arguments()) {
            buffer.append(" ").append(arg);
        }
        String cmdLine = "/" + invocation.alias() + buffer;
        List<String> completions = environment.handleCompletions(invocation.source(), cmdLine, new ArrayList<>());
        completions.removeIf(comp -> comp.contains(":") || comp.equalsIgnoreCase(""));

        // Add player names as default completions (quality of life feature)
        if (completions.isEmpty()) {
            String[] arguments = ParserUtility.getArguments(cmdLine);
            if (arguments.length > 0) {
                String lastArgument = arguments[arguments.length - 1].toLowerCase(Locale.ROOT);
                for (Player player : proxyServer.getAllPlayers()) {
                    if (!player.getUsername().toLowerCase(Locale.ROOT).startsWith(lastArgument) || completions.contains(player.getUsername())) {
                        continue;
                    }
                    completions.add(player.getUsername());
                }
            } else {
                completions.addAll(
                        proxyServer.getAllPlayers()
                                .parallelStream()
                                .map(Player::getUsername)
                                .collect(Collectors.toList())
                );
            }
        }
        return completions;
    }
}
