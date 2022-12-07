package eu.koboo.atcommand.parser;

import eu.koboo.atcommand.exceptions.ParameterException;
import org.bukkit.GameMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

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

    @Override
    public String friendlyName() {
        return "GameMode";
    }

    @Override
    public List<String> complete(String value) {
        if(value == null) {
            return Arrays.stream(VALUES)
                    .map(GameMode::name)
                    .collect(Collectors.toList());
        } else {
            value = value.toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (GameMode gameMode : VALUES) {
                String completion = gameMode.name().toLowerCase(Locale.ROOT);
                if(completion.startsWith(value) && !completions.contains(completion)) {
                    completions.add(value);
                }
            }
            return completions;
        }
    }

    @SuppressWarnings("deprecation")
    private boolean isGameMode(GameMode gameMode, String value) {
        if(value.equalsIgnoreCase(gameMode.name().toLowerCase(Locale.ROOT))) {
            return true;
        }
        return value.equalsIgnoreCase(String.valueOf(gameMode.getValue()));
    }
}