package dev.binflux.atcommand.listener;

import dev.binflux.atcommand.environment.BungeeCommandEnvironment;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.TabCompleteEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.List;

@RequiredArgsConstructor
public class TabCompleteListener implements Listener {

    private final BungeeCommandEnvironment environment;

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onComplete(TabCompleteEvent event) {
        if(!(event.getSender() instanceof ProxiedPlayer)) {
            return;
        }
        if(!event.getCursor().startsWith("/")) {
            return;
        }
        List<String> completions = environment.handleCompletions(
                (ProxiedPlayer) event.getSender(), event.getCursor(), event.getSuggestions()
        );
        completions.removeIf(comp -> comp.contains(":") || comp.equalsIgnoreCase(""));
        event.getSuggestions().clear();
        event.getSuggestions().addAll(completions);
    }
}