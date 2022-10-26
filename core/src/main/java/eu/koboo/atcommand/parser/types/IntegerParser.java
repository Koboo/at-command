package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

public class IntegerParser extends ParameterParser<Integer> {

    @Override
    public Class<?>[] getExtraTypes() {
        return new Class[]{int.class};
    }

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