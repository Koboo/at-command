package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.parser.ParameterParser;

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