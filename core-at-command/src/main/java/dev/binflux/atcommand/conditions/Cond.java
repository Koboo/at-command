package dev.binflux.atcommand.conditions;

import dev.binflux.atcommand.exceptions.ConditionException;

public class Cond {

    public static void check(boolean throwIfTrue, String message) {
        if(throwIfTrue) {
            throw new ConditionException(message);
        }
    }
}