package dev.binflux.atcommand.parser;

import dev.binflux.atcommand.exceptions.ParameterException;

import org.bukkit.Bukkit;
import org.bukkit.World;

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
    public String friendlyName() {
        return "World";
    }
}