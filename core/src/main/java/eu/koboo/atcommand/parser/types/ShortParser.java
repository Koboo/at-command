package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

public class ShortParser extends ParameterParser<Short> {

    @Override
    public Class<?>[] getExtraTypes() {
        return new Class[]{short.class};
    }

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