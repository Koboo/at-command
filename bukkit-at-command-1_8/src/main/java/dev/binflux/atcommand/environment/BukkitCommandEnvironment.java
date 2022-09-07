package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.parser.GameModeParser;
import dev.binflux.atcommand.parser.WorldParser;

import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

@SuppressWarnings("all")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BukkitCommandEnvironment extends CommandEnvironment {

    final JavaPlugin plugin;
    BukkitCommandMap commandMap;

    public BukkitCommandEnvironment(JavaPlugin plugin) {
        this.plugin = plugin;
        this.commandMap = new BukkitCommandMap(plugin.getServer(), this);

        registerParser(new GameModeParser());
        registerParser(new WorldParser());

        try {
            // Search the commandMap field in the server class
            Field commandMapField = null;
            for (Field field : plugin.getServer().getClass().getDeclaredFields()) {
                if (!field.getType().isAssignableFrom(SimpleCommandMap.class)) {
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
            SimpleCommandMap oldCommandMap = (SimpleCommandMap) commandMapField.get(plugin.getServer());

            // Get the knownCommands Map of the commandMap
            Field knownCommandsField = null;
            for (Field field : oldCommandMap.getClass().getDeclaredFields()) {
                if (!field.getType().isAssignableFrom(Map.class)) {
                    continue;
                }
                knownCommandsField = field;
                break;
            }
            if (commandMapField == null) {
                throw new NullPointerException("Couldn't find knownCommands field in " + SimpleCommandMap.class + ".");
            }
            knownCommandsField.setAccessible(true);

            // Get the already known commands of the commandMap
            Map<String, Command> oldCommands = (Map<String, Command>) knownCommandsField.get(oldCommandMap);

            // Register the known commands to the new created commandMap
            commandMap.registerOldCommands(oldCommands);

            // Modify the final modifier of the commandMap field in the server,
            // to ensure it can be overwritten by our commandMap.
            int fieldModifiers = commandMapField.getModifiers();
            if (Modifier.isFinal(fieldModifiers)) {
                MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
                VarHandle modifiers = lookup.findVarHandle(Field.class, "modifiers", int.class);
                modifiers.set(commandMapField, fieldModifiers & ~Modifier.FINAL);
            }

            // Set the new commandMap into the server
            commandMapField.set(plugin.getServer(), commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
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
    public <T> boolean hasNotPermission(T sender, String permission) {
        return !((CommandSender) sender).hasPermission(permission);
    }
}