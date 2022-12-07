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
        return ParserUtility.complete(value, Bukkit.getWorlds(), World::getName);
    }

    @Override
    public String friendlyName() {
        return "World";
    }
}