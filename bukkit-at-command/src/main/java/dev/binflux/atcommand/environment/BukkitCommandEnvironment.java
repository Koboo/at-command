package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.environment.meta.CommandMeta;
import dev.binflux.atcommand.parser.GameModeParser;
import dev.binflux.atcommand.parser.WorldParser;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BukkitCommandEnvironment extends CommandEnvironment {

    final JavaPlugin plugin;
    SimpleCommandMap commandMap;

    public BukkitCommandEnvironment(JavaPlugin plugin) {
        this.plugin = plugin;

        registerParser(new GameModeParser());
        registerParser(new WorldParser());

        try {
            Field commandMapField = null;
            for (Field field : plugin.getServer().getClass().getDeclaredFields()) {
                if (!SimpleCommandMap.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                commandMapField = field;
                break;
            }
            if (commandMapField == null) {
                throw new NullPointerException("Couldn't find commandMap field of " + SimpleCommandMap.class + " in CraftServer.");
            }
            commandMapField.setAccessible(true);

            // Get the current/old command map of the server
            this.commandMap = (SimpleCommandMap) commandMapField.get(plugin.getServer());
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Class<?> getPlayerClass() {
        return Player.class;
    }

    @Override
    public Class<?> getConsoleClass() {
        return ConsoleCommandSender.class;
    }

    @Override
    public <S> void sendSenderMessage(S sender, String message) {
        if(!(sender instanceof CommandSender)){
            return;
        }
        CommandSender commandSender = (CommandSender) sender;
        commandSender.sendMessage(message);
    }

    @Override
    public <T> boolean hasNotPermission(T sender, String permission) {
        return !((CommandSender) sender).hasPermission(permission);
    }

    @Override
    public <T> void afterRegistration(T command, CommandMeta commandMeta) {
        List<String> aliasList = new ArrayList<>(commandMeta.getAliasList());
        String label = aliasList.get(0);
        aliasList.remove(0);
        BukkitRootCommand rootCommand = new BukkitRootCommand(this, label);
        commandMap.register("at-command", rootCommand);
        for (String alias : commandMeta.getAliasList()) {
            commandMap.register(alias, "at-command", rootCommand);
        }
    }
}