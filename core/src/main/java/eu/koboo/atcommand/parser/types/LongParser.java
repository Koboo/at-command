package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

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