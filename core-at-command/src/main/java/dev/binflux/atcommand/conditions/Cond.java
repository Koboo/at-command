package dev.binflux.atcommand.conditions;

import dev.binflux.atcommand.exceptions.ConditionException;

public class Cond {

    public static void check(boolean conditionStatement, String message) {
        if(!conditionStatement) {
            throw new ConditionException(message);
        }
    }
}