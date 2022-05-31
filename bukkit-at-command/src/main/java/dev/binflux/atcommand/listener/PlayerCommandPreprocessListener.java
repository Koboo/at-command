package dev.binflux.atcommand.listener;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;
import lombok.RequiredArgsConstructor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@RequiredArgsConstructor
public class PlayerCommandPreprocessListener implements Listener {

    private final BukkitCommandEnvironment environment;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreProcess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if(message.startsWith("/")) {
            message = message.substring(1);
        }

        if(environment.handleCommand(event.getPlayer(), message)) {
            event.setCancelled(true);
        }
    }
}