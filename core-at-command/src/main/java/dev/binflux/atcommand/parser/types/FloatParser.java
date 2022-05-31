package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

public class FloatParser extends ParameterParser<Float> {

    @Override
    public Float parse(String value) throws ParameterException {
        try {
            return Float.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterException(value + " is not a valid number (java.lang.Float)!");
        }
    }

    @Override
    public String friendlyName() {
        return "Number";
    }
}