package dev.binflux.atcommand;

import dev.binflux.atcommand.annotations.command.Alias;
import dev.binflux.atcommand.annotations.command.ShowHelpWithError;
import dev.binflux.atcommand.annotations.method.Default;
import dev.binflux.atcommand.annotations.method.Subcommand;
import org.bukkit.entity.Player;

@Alias(alias = {"test"})
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