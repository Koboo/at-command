package dev.binflux.atcommand.listener;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerCommandEvent;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerCommandListener implements Listener {

    BukkitCommandEnvironment environment;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onConsoleProcess(ServerCommandEvent event) {
        if(environment == null) {
            return;
        }
        if(environment.handleCommand(event.getSender(), event.getCommand())) {
            event.setCancelled(true);
        }
    }
}