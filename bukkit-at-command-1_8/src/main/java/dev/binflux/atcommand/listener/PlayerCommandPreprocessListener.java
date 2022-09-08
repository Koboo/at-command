package dev.binflux.atcommand.listener;

import dev.binflux.atcommand.environment.BukkitCommandEnvironment;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PlayerCommandPreprocessListener implements Listener {

    BukkitCommandEnvironment environment;

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPreProcess(PlayerCommandPreprocessEvent event) {
        String message = event.getMessage();
        if(message.startsWith("/")) {
            message = message.substring(1);
        }
        if(environment == null) {
            return;
        }
        if(environment.handleCommand(event.getPlayer(), message)) {
            event.setCancelled(true);
        }
    }
}