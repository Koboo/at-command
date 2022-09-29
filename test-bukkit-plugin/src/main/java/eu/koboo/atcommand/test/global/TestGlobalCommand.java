package eu.koboo.atcommand.test.global;

import eu.koboo.atcommand.annotations.command.Global;
import eu.koboo.atcommand.annotations.command.Label;
import eu.koboo.atcommand.annotations.command.ShowHelpWithError;
import eu.koboo.atcommand.annotations.method.Subcommand;
import eu.koboo.atcommand.annotations.method.types.*;
import eu.koboo.atcommand.conditions.Cond;
import eu.koboo.atcommand.environment.meta.CommandSyntax;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

@Global
public class TestGlobalCommand {

    @WrongSender
    public void wrongSender(CommandSender sender) {
        if(sender instanceof Player) {
            sender.sendMessage("You're not console!");
        } else {
            sender.sendMessage("You're not a player!");
        }
    }

    @OnHelp
    public void onHelp(CommandSender sender, String command, List<CommandSyntax> syntaxList) {
        sender.sendMessage("Help of \"" + command + "\":");
        for (CommandSyntax syntax : syntaxList) {
            sender.sendMessage("Usage: /" + command + " " + syntax.getSyntax());
        }
    }

    @OnError
    public void onError(CommandSender sender, String error) {
        String addition = error  == null ? "" : " (" + error + ")";
        sender.sendMessage("An internal error occurred." + addition);
    }

    @NoPermission
    public void noPermission(CommandSender sender, String permission, String command) {
        sender.sendMessage("You don't have permission to execute /" + command + "! (Permission: " + permission + ")");
    }
}