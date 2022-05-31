package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

public class LongParser extends ParameterParser<Long> {

    @Override
    public Long parse(String value) throws ParameterException {
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterException(value + " is not a valid number (java.lang.Long)!");
        }
    }

    @Override
    public String friendlyName() {
        return "Number";
    }
}