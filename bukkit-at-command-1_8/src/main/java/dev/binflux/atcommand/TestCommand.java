package dev.binflux.atcommand;

import dev.binflux.atcommand.annotations.command.Label;
import dev.binflux.atcommand.annotations.command.ShowHelpWithError;
import dev.binflux.atcommand.annotations.method.types.Default;
import dev.binflux.atcommand.annotations.method.Subcommand;
import org.bukkit.entity.Player;

@Label("test")
@Label("secondtest")
@ShowHelpWithError
public class TestCommand {

    @Default
    public void onDefault(Player player) {
        player.sendMessage("Default");
    }

    @Subcommand("info")
    public void onInfo(Player player) {
        player.sendMessage("Info");
    }

    @Subcommand("information")
    public void onInformation(Player player) {
        player.sendMessage("Information");
    }
}