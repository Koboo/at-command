package eu.koboo.atcommand.listener;

import eu.koboo.atcommand.environment.BungeeCommandEnvironment;
import eu.koboo.atcommand.parser.ParserUtility;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class TabCompleteListener implements Listener {

    private final BungeeCommandEnvironment environment;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onComplete(TabCompleteEvent event) {
        if(!event.getCursor().startsWith("/")) {
            return;
        }
        List<String> completions = environment.handleCompletions(event.getSender(), event.getCursor(), event.getSuggestions());
        completions.removeIf(comp -> comp.contains(":") || comp.equalsIgnoreCase(""));

        // Add player names as default completions (quality of life feature)
        if (completions.isEmpty()) {
            String[] arguments = ParserUtility.getArguments(event.getCursor());
            if (arguments.length > 0) {
                String lastArgument = arguments[arguments.length - 1].toLowerCase(Locale.ROOT);
                for (ProxiedPlayer player : ProxyServer.getInstance().getPlayers()) {
                    if(player.getName().toLowerCase(Locale.ROOT).startsWith(lastArgument) && !completions.contains(player.getName())) {
                        completions.add(player.getName());
                    }
                }
            } else {
                completions.addAll(
                        ProxyServer.getInstance().getPlayers()
                                .parallelStream()
                                .map(ProxiedPlayer::getName)
                                .collect(Collectors.toList())
                );
            }
        }

        event.getSuggestions().clear();
        event.getSuggestions().addAll(completions);
    }
}