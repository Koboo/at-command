package eu.koboo.atcommand.parser.types;

import eu.koboo.atcommand.exceptions.ParameterException;
import eu.koboo.atcommand.parser.ParameterParser;

import java.util.UUID;

public class UUIDParser extends ParameterParser<UUID> {

    @Override
    public UUID parse(String value) throws ParameterException {
        try {
            return UUID.fromString(value);
        } catch (Exception e) {
            throw new ParameterException(value + " is not a valid UUID.");
        }
    }

    @Override
    public String friendlyName() {
        return "UUID";
    }
}