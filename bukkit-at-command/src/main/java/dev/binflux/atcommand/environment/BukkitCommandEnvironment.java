package dev.binflux.atcommand.environment;

import dev.binflux.atcommand.parser.GameModeParser;
import dev.binflux.atcommand.parser.WorldParser;

import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

@SuppressWarnings("all")
public class BukkitCommandEnvironment extends CommandEnvironment {

    private final JavaPlugin plugin;
    private BukkitCommandMap commandMap;

    public BukkitCommandEnvironment(JavaPlugin plugin) {
        this.plugin = plugin;
        registerParser(new GameModeParser());
        registerParser(new WorldParser());

        commandMap = new BukkitCommandMap(plugin.getServer(), this);

        try {
            Field commandMapField = plugin.getServer().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);

            Object oldCommandMap = commandMapField.get(plugin.getServer());

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);


            MethodHandles.Lookup lookup = MethodHandles.privateLookupIn(Field.class, MethodHandles.lookup());
            VarHandle modifiers = lookup.findVarHandle(Field.class, "modifiers", int.class);
            int mods = knownCommandsField.getModifiers();
            if (Modifier.isFinal(mods)) {
                modifiers.set(knownCommandsField, mods & ~Modifier.FINAL);
            }

            knownCommandsField.set(commandMap, knownCommandsField.get(oldCommandMap));
            commandMapField.set(plugin.getServer(), commandMap);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
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