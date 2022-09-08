package dev.binflux.atcommand.parser;

import dev.binflux.atcommand.exceptions.ParameterException;
import org.bukkit.GameMode;

import java.util.Locale;

//TODO: Completion
public class GameModeParser extends ParameterParser<GameMode> {

    private static final GameMode[] VALUES = GameMode.values();

    @Override
    public GameMode parse(String value) throws ParameterException {
        for (GameMode gameMode : VALUES) {
            if(isGameMode(gameMode, value)) {
                return gameMode;
            }
        }
        throw new ParameterException(value + " is not valid gameMode.");
    }

    private boolean isGameMode(GameMode gameMode, String value) {
        if(value.equalsIgnoreCase(gameMode.name().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return value.equalsIgnoreCase(String.valueOf(gameMode.getValue()));
    }
}