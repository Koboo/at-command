package eu.koboo.atcommand.test.commands;

import eu.koboo.atcommand.annotations.command.Label;
import eu.koboo.atcommand.annotations.command.ShowHelpWithError;
import eu.koboo.atcommand.annotations.method.Subcommand;
import eu.koboo.atcommand.annotations.method.types.Default;
import eu.koboo.atcommand.annotations.options.Access;
import eu.koboo.atcommand.conditions.Cond;
import org.bukkit.entity.Player;

@Label("testcommand")
@Label("testalias")
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

    @Subcommand("primitives")
    public void onPrimitive(Player player, int primitive) {
        player.sendMessage("It's a primitive!");
    }

    @Subcommand("noperm")
    @Access("test-plugin.noperm")
    public void hasNotPermission(Player player) {
        player.sendMessage("You've permission!");
    }
}