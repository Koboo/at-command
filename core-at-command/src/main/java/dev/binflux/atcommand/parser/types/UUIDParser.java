package dev.binflux.atcommand.parser.types;

import dev.binflux.atcommand.exceptions.ParameterException;
import dev.binflux.atcommand.parser.ParameterParser;

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
}