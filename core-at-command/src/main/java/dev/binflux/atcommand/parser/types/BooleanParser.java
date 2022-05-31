package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class BooleanParser extends ParameterParser<Boolean> {

    private static final List<String> COMPLETIONS = new ArrayList<>();

    static {
        COMPLETIONS.addAll(Arrays.asList(
                "yes", "1", "on", "true", "allow",
                "no", "0", "off", "false", "disallow"
        ));
    }

    @Override
    public Boolean parse(String value) throws ParameterException {
        if(value.equalsIgnoreCase("yes")
                || value.equalsIgnoreCase("1")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("allow")) {
            return true;
        }
        if(value.equalsIgnoreCase("no")
                || value.equalsIgnoreCase("0")
                || value.equalsIgnoreCase("off")
                || value.equalsIgnoreCase("false")
                || value.equalsIgnoreCase("disallow")) {
            return false;
        }
        throw new ParameterException(value + " is not a valid Boolean (java.lang.Boolean)!");
    }

    @Override
    public List<String> complete(String value) {
        if(value == null) {
            return new ArrayList<>(COMPLETIONS);
        } else {
            value = value.toLowerCase(Locale.ROOT);
            List<String> completions = new ArrayList<>();
            for (String completion : COMPLETIONS) {
                if(completion.startsWith(value) && !completions.contains(completion)) {
                    completions.add(value);
                }
            }
            return completions;
        }
    }

    @Override
    public String friendlyName() {
        return "true (yes/on/1/allow) | false (no/off/0/disallow)";
    }
}