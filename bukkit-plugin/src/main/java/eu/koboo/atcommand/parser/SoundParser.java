package eu.koboo.atcommand.parser;

import eu.koboo.atcommand.exceptions.ParameterException;
import org.bukkit.Sound;

import java.util.List;

public class SoundParser extends ParameterParser<Sound> {

    private static final Sound[] VALUES = Sound.values();

    @Override
    public Sound parse(String value) throws ParameterException {
        for (Sound sound : VALUES) {
            if (sound.getKey().getKey().equalsIgnoreCase(value) || sound.name().equalsIgnoreCase(value)) {
                return sound;
            }
        }
        throw new ParameterException(value + " is not valid sound.");
    }

    @Override
    public String friendlyName() {
        return "Sound";
    }

    @Override
    public List<String> complete(String value) {
        return ParserUtility.complete(value, VALUES, Sound::name);
    }
}