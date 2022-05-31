package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

public class DoubleParser extends ParameterParser<Double> {

    @Override
    public Double parse(String value) throws ParameterException {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterException(value + " is not a valid number!");
        }
    }

    @Override
    public String friendlyName() {
        return "Number";
    }
}