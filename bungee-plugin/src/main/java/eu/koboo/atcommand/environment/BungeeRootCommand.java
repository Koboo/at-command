package eu.koboo.atcommand.environment;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.plugin.Command;

public class BungeeRootCommand extends Command {

    private final BungeeCommandEnvironment environment;

    public BungeeRootCommand(BungeeCommandEnvironment environment, String name, String permission, String... aliases) {
        super(name, permission, aliases);
        this.environment = environment;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        StringBuilder argumentString = new StringBuilder();
        for (String arg : args) {
            argumentString.append(" ").append(arg);
        }
        String commandString = getName() + argumentString;
        if(environment == null) {
            return;
        }
        environment.handleCommand(sender, commandString);
    }
}