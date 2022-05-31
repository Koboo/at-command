package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

public class IntegerParser extends ParameterParser<Integer> {

    @Override
    public Integer parse(String value) throws ParameterException {
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterException(value + " is not a valid number (java.lang.Integer)!");
        }
    }

    @Override
    public String friendlyName() {
        return "Number";
    }
}