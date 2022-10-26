package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

public class DoubleParser extends ParameterParser<Double> {

    @Override
    public Class<?>[] getExtraTypes() {
        return new Class[]{double.class};
    }

    @Override
    public Double parse(String value) throws ParameterException {
        try {
            return Double.valueOf(value);
        } catch (NumberFormatException e) {
            throw new ParameterException(value + " is not a valid number (java.lang.Double)!");
        }
    }

    @Override
    public String friendlyName() {
        return "Number";
    }
}