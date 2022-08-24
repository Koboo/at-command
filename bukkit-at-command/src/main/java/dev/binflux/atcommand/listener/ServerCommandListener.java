package dev.binflux.atcommand.listener;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

@RequiredArgsConstructor
public class ServerCommandListener implements Listener {

    private final BukkitCommandEnvironment environment;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onConsoleProcess(ServerCommandEvent event) {
        if(environment.handleCommand(event.getSender(), event.getCommand())) {
            event.setCancelled(true);
        }
    }
}