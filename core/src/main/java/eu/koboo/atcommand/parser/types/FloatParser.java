package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

public class FloatParser extends ParameterParser<Float> {

    @Override
    public Class<?>[] getExtraTypes() {
        return new Class[]{float.class};
    }

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