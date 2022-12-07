package eu.koboo.atcommand.parser;

import eu.koboo.atcommand.exceptions.ParameterException;
import org.bukkit.GameMode;

import java.util.List;
import java.util.Locale;

public class GameModeParser extends ParameterParser<GameMode> {

    private static final GameMode[] VALUES = GameMode.values();

    @Override
    public GameMode parse(String value) throws ParameterException {
        for (GameMode gameMode : VALUES) {
            if (isGameMode(gameMode, value)) {
                return gameMode;
            }
        }
        throw new ParameterException(value + " is not valid gameMode.");
    }

    @Override
    public String friendlyName() {
        return "GameMode";
    }

    @Override
    public List<String> complete(String value) {
        return ParserUtility.complete(value, VALUES, GameMode::name);
    }

    @SuppressWarnings("deprecation")
    private boolean isGameMode(GameMode gameMode, String value) {
        if (value.equalsIgnoreCase(gameMode.name().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return value.equalsIgnoreCase(String.valueOf(gameMode.getValue()));
    }
}