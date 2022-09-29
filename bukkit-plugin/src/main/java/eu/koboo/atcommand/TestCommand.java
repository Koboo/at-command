package eu.koboo.atcommand;

import eu.koboo.atcommand.annotations.command.Label;
import eu.koboo.atcommand.annotations.command.ShowHelpWithError;
import eu.koboo.atcommand.annotations.method.Subcommand;
import eu.koboo.atcommand.annotations.method.types.Default;
import eu.koboo.atcommand.conditions.Cond;
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
        Cond.check(player.getName().equalsIgnoreCase("Koboo"), "Your name is Koboo, so the condition is true.");
        player.sendMessage("Information");
    }
}