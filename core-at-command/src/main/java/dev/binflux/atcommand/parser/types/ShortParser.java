package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

public class ShortParser extends ParameterParser<Short> {

    @Override
    public Short parse(String value) throws ParameterException {
        try {
            return Short.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterException(value + " is not a valid number (java.lang.Short)!");
        }
    }

    @Override
    public String friendlyName() {
        return "Number";
    }
}