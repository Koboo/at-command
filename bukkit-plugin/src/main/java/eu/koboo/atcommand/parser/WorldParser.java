package eu.koboo.atcommand.parser;

import eu.koboo.atcommand.exceptions.ParameterException;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class WorldParser extends ParameterParser<World> {

    @Override
    public World parse(String value) throws ParameterException {
        for (World world : Bukkit.getWorlds()) {
            if(value.equalsIgnoreCase(world.getName())) {
                return world;
            }
        }
        throw new ParameterException("The world " + value + " could not be found.");
    }

    @Override
    public List<String> complete(String value) {
        if(value == null) {
            return Bukkit.getWorlds().stream()
                    .map(World::getName)
                    .collect(Collectors.toList());
        } else {
            value = value.toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (World world : Bukkit.getWorlds()) {
                String completion = world.getName().toLowerCase(Locale.ROOT);
                if(completion.startsWith(value) && !completions.contains(completion)) {
                    completions.add(value);
                }
            }
            return completions;
        }
    }

    @Override
    public String friendlyName() {
        return "World";
    }
}