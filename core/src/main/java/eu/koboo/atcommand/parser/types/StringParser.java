package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.parser.ParameterParser;

public class StringParser extends ParameterParser<String> {

    @Override
    public String parse(String value) {
        return value;
    }

    @Override
    public String friendlyName() {
        return "Text";
    }
}